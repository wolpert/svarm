FROM amazoncorretto:21

RUN mkdir -p /server/logs /server/dist /server/work
ADD build/install/control /server/dist/
COPY src/main/resources/docker-control-config.yaml /server/config.yaml
COPY ../config/keystore.jks /server/keystore.jks
COPY ../config/cacerts.jks /server/cacerts.jks
WORKDIR /server

ENTRYPOINT [ "/server/dist/bin/control", "server", "/server/config.yaml"]

