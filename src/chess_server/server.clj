(ns chess-server.server
  (:require [clojure.core.async :as async])
  (:require [websocket-layer.network :as net])
  (:require [websocket-layer.core :as wl])
  (:require [ring.adapter.jetty9 :as jetty])
  (:gen-class))

(def state (atom (list)))
(def listeners (atom #{}))

(defn web-handler
  [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body></body></html>"})

(defmethod wl/handle-push :client-state [{:keys [board]}]
  (swap! state conj board))

(defmethod wl/handle-push :undo-move [_]
  (swap! state pop))

(defn notify-listeners
  [state]
  (let [closed-clients (atom (list))]
    (doseq [listener (seq @listeners)]
      (when-not (async/put! listener state)
        (swap! closed-clients conj listener)))
    (doseq [closed-client @closed-clients]
      (swap! listeners disj closed-client))))

(add-watch state :notify-listeners (fn [_ _ old new]
                                     (when-not (= old new) (notify-listeners (first new)))))

(defmethod wl/handle-subscription :subscribe-to-game
  [{:keys [board]}]
  (println "New subscription!")
  (let [results (async/chan)
        initial-state @state]
    (swap! listeners conj results)
    (if-not (empty? initial-state)
      (async/put! results (first initial-state))
      (swap! state conj board))
    results))

(def ws-endpoints
  {"/ws" (net/websocket-handler {:encoding :edn})})

(defn -main 
  [& args]
  (println "Starting server")
  (jetty/run-jetty web-handler {:port                (Integer/parseInt (first args))
                               :join?                false
                               :async?               true
                               :websockets           ws-endpoints
                               :allow-null-path-info true}))
