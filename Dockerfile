# 微服务通用镜像：构建时通过 --build-arg JAR_FILE 指定各服务 jar
FROM openjdk:8-jdk-alpine

MAINTAINER hms-team

ARG JAR_FILE
ENV TZ=Asia/Shanghai

RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 容器内部统一使用 8080，由 docker-compose 做端口映射
EXPOSE 8080

COPY ${JAR_FILE} app.jar

# 支持通过环境变量覆盖端口、注册中心地址
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8080
ENV EUREKA_URL="http://hms-eureka-server:8761/eureka/"
ENV CONFIG_URL="http://hms-config-server:8888"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar \
--server.port=$SERVER_PORT \
--eureka.client.service-url.defaultZone=$EUREKA_URL \
--spring.cloud.config.uri=$CONFIG_URL"]
