
FROM openjdk:11
ARG PROJECT_VERSION=1.0.0
RUN mkdir -p /home/app
WORKDIR /home/app
ENV TZ=Asia/Seoul
ENV MODE=test
ENV ELASTICSEARCH_USERNAME=elastic
ENV ELASTICSEARCH_PASSWORD=elasticstack
ENV ELASTICSEARCH_URIS=http://10.0.3.254:30001
ENV ELASTICSEARCH_INDEX_NAME=cj-log
ENV ELASTICSEARCH_API_KEY=UGN0QUZKQUJENm5BNzFabHhFcFE6RzhGVFBudUNTNS1tSU5FMHFoTlc3QQ==
ENV ELASTICSEARCH_SSL_ENABLED=false
ENV CORS_URI=http://localhost:6001
ENV SESSION_CHECK=false
ENV GRAFANA_HOST=http://10.0.1.205:3000
ENV TOP_COUNT=10
ENV LANG=ko_KR.UTF-8
ENV LANGUAGE=ko_KR:ko
ENV LC_ALL=ko_KR.UTF-8

# BOOTJAR BUILD
ARG JAR_FILE=build/libs/CJ-Dashboard-0.0.1-SNAPSHOT.jar


COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Duser.timezone=${TZ}", "-jar", "app.jar"]


## docker build --platform linux/amd64 -t jayutest.best:56789/cj-dashboard/cj-dashboard:3.0.52 .
## docker run -d --name cj-dashboard jayutest.best:56789/cj-dashboard/cj-dashboard:4.6
## docker save -o cj-dashboard.tar jayutest.best:56789/cj-dashboard/cj-dashboard:1.0.0
## docker load -i tar파일명
## docker import <파일 or URL> - [image name[:tag name]]