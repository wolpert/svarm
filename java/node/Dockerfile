FROM amazoncorretto:17

RUN mkdir -p /server/logs /server/dist /server/work
ADD build/install/node /server/dist/
COPY src/main/resources/docker-node-config.yaml /server/config.yaml
WORKDIR /server

ENTRYPOINT [ "/server/dist/bin/node", "server", "/server/config.yaml"]

