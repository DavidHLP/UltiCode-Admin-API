# 使用 Eclipse Temurin 17 JDK (基于 Ubuntu 22.04 LTS)，这是一个高质量的 OpenJDK 发行版。
FROM eclipse-temurin:17-jdk-jammy
LABEL maintainer="sentinel-user"
LABEL version="1.8.8"
LABEL description="Sentinel Dashboard 1.8.8 on JDK 17"
WORKDIR /app
COPY ./sentinel-dashboard-1.8.8.jar sentinel-dashboard.jar
ENTRYPOINT [ "java", "-Dserver.port=8080", "-Dcsp.sentinel.dashboard.server=localhost:8080", "-Dproject.name=sentinel-dashboard", "-jar", "sentinel-dashboard.jar" ]