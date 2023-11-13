FROM amazoncorretto:21

RUN mkdir -p /server/logs /server/dist /server/work
ADD build/install/control /server/dist/
COPY src/main/resources/docker-control-config.yaml /server/config.yaml
WORKDIR /server

ENTRYPOINT [ "/server/dist/bin/control", "server", "/server/config.yaml"]

