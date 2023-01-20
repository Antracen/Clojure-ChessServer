(defproject chess-server "0.1.0-SNAPSHOT"
  :description "Chess server written for a ClojureScript chess client. Very little logic, basically just an echo-server."
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring "1.9.6"]
                 [org.clojars.rutledgepaulv/websocket-layer "0.1.11"]] 
  :uberjar-name "clojure-chess-server.jar"
  :repl-options {:init-ns chess-server.server}
  :main chess-server.server
  :aot [chess-server.server])
