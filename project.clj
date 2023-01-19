(defproject chess-server "0.1.0-SNAPSHOT"
  :description "Chess server written for a ClojureScript chess client. Basically no logic."
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring "1.9.6"]
                 [org.clojars.rutledgepaulv/websocket-layer "0.1.11"]
                 [environ "1.1.0"]]
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "clojure-chess-server.jar"
  :repl-options {:init-ns chess-server.core})
