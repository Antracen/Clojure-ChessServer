(ns chess-server.server
  (:require [clojure.core.async :as async])
  (:require [websocket-layer.network :as net])
  (:require [websocket-layer.core :as wl])
  (:require [ring.adapter.jetty9 :as jetty])
  (:gen-class))

(def state (atom {}))
(def listeners (atom #{}))

(defn web-handler
  [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body></body></html>"})

(defmethod wl/handle-push :client-state [{:keys [board history]}]
  (reset! state {:board board
                 :history history}))

(defn notify-listeners
  [state]
  (let [closed-clients (atom (list))]
    (doseq [listener (seq @listeners)]
      (when-not (async/put! listener state)
        (swap! closed-clients conj listener)))
    (doseq [closed-client @closed-clients]
      (swap! listeners disj closed-client))))

(add-watch state :notify-listeners (fn [_ _ old new]
                                     (when-not (= old new) (notify-listeners new))))

(defmethod wl/handle-subscription :subscribe-to-game
  [_]
  (println "New subscription!")
  (let [results (async/chan)
        initial-state @state]
    (swap! listeners conj results)
    (when-not (empty? initial-state)
      (async/put! results initial-state))
    results))

(def ws-endpoints
  {"/ws" (net/websocket-handler {:encoding :edn})})

(defn -main []
  (jetty/run-jetty web-handler {:port                5000
                               :join?                false
                               :async?               true
                               :websockets           ws-endpoints
                               :allow-null-path-info true}))
