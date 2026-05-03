(ns chess-server.server
    (:require [clojure.core.async :as async])
    (:require [websocket-layer.network :as net])
    (:require [websocket-layer.core :as wl])
    (:require [ring.adapter.jetty9 :as jetty])
    (:gen-class))
 
(defn web-handler [_]
    ; Handler for the / route
    {:status     200
     :headers    {"Content-Type" "text/html"}
     :body       "<html><head></head><body></body></html>"})
 
(def ws-endpoints
    {"/ws" (net/websocket-handler {:encoding :edn})})
 
(def game-state (atom (list)))
(def subscriptions (atom #{}))
 
(defmethod wl/handle-push :client-state [{:keys [board]}]
    (swap! game-state conj board))
 
(defmethod wl/handle-push :undo-move [_]
    (swap! game-state pop))
 
(defn send-message-to-subscribers [message]
    ; When sending the message, also accumulate list of active subscriptions
    (let [active-subscriptions (reduce
                           (fn [acc subscriber]
                               (if (async/put! subscriber message)
                                   (conj acc subscriber)
                                   acc))
                           #{}
                           @subscriptions)]
        ; Update subscriptions list to only include the open subscriptions
        (reset! subscriptions active-subscriptions)))
 
(add-watch game-state :send-message-to-subscriptions
    ; Whenever game-state changes, send the new state to all subscribers
    (fn [_ _ old-game-state new-game-state]
        (when (not= old-game-state new-game-state) (send-message-to-subscribers (first new-game-state)))))
 
(defmethod wl/handle-subscription :subscribe-to-game [{:keys [board]}]
    (println "New subscription!")
    (let [subscriber-socket (async/chan)
          initial-state     @game-state]
        (swap! subscriptions conj subscriber-socket)
        (when (seq initial-state)
              (async/put! subscriber-socket (first initial-state)))
        (swap! game-state conj board)
        subscriber-socket))
 
(defn -main []
    (jetty/run-jetty web-handler {:port                    5000
                                  :join?                   false
                                  :async?                  true
                                  :websockets              ws-endpoints
                                  :allow-null-path-info    true}))
