(ns chess-server.web
  (:require [clojure.core.async :as async])
  (:require [websocket-layer.network :as net])
  (:require [websocket-layer.core :as wl])
  (:require [ring.adapter.jetty9 :as jetty]))

(def state (atom {}))
(def num (atom 1))

(defn web-handler
  [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body></body></html>"})

(defmethod wl/handle-push :client-state [{:keys [board history]}]
  (reset! state {:board board
                 :history history}))

(defmethod wl/handle-subscription :subscribe-to-game [a]
  (let [results (async/chan)
        init-state @state]
    (async/go
      (when-not (empty? init-state)
        (async/>! results init-state))
      (loop []
        (let [p (promise)
              key (swap! num inc)]
          (add-watch state (keyword (str "key" key)) (fn [_ _ old new]
                                                       (when-not (= old new) (deliver p :changed))))
          (deref p)
          (when (async/>! results @state)
            (recur)))))
    results))

(def ws-endpoints
  {"/ws" (net/websocket-handler {:encoding :edn})})

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty web-handler {:port                 port
                                  :join?                false
                                  :async?               true
                                  :websockets           ws-endpoints
                                  :allow-null-path-info true})))
