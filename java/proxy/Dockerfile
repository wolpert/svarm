FROM amazoncorretto:17

RUN mkdir -p /server/logs /server/dist /server/work
ADD build/install/proxy /server/dist/
COPY src/main/resources/docker-proxy-config.yaml /server/config.yaml
WORKDIR /server

ENTRYPOINT [ "/server/dist/bin/proxy", "server", "/server/config.yaml"]

