# Clojure-ChessServer
Chess server written in [Clojure](https://clojure.org/).

# Client
https://github.com/Antracen/ClojureScript-Chess

# Build

## Configure
Modify server port in ´src/server.clj´.

## Docker build
`docker build -t chess-server .`

## Heroku build
* Download Heroku CLI
* Login with `heroku login`
* Start Docker
* Login to Docker/Container with `heroku container:login`
* Build and push image with `heroku container:push web -a chess-server`
* Release image with `heroku container:release web -a chess-server`