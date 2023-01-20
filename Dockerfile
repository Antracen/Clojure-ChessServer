# BUILD APP
FROM clojure as BUILD_IMAGE

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY . /usr/src/app
RUN lein uberjar
COPY target/clojure-chess-server.jar app-standalone.jar

# CREATE SMALL JRE
FROM openjdk:17-alpine AS jre-build
WORKDIR /app

COPY --from=BUILD_IMAGE /usr/src/app/app-standalone.jar build/app.jar

RUN jdeps \
--ignore-missing-deps \
-q \
--multi-release 17 \
--print-module-deps \
--class-path build/lib/* \
build/app.jar > jre-deps.info

RUN jlink --verbose \
--compress 2 \
--strip-java-debug-attributes \
--no-header-files \
--no-man-pages \
--output jre \
--add-modules $(cat jre-deps.info)

# CREATE EXECUTABLE CONTAINER
FROM alpine:latest
WORKDIR /deployment
COPY --from=jre-build /app/jre jre
COPY --from=jre-build /app/build/app.jar app.jar
ENTRYPOINT jre/bin/java -jar app.jar
