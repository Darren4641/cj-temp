
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
ENV ELASTICSEARCH_KEYSTORE_LOCATION=""
ENV ELASTICSEARCH_KEYSTORE_PASSWORD=""
ENV ELASTICSEARCH_TRUSTSTORE_LOCATION=""
ENV ELASTICSEARCH_TRUSTSTORE_PASSWORD=""

# BOOTJAR BUILD
ARG JAR_FILE=build/libs/CJ-Dashboard-0.0.1-SNAPSHOT.jar


COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Duser.timezone=${TZ}", "-jar", "app.jar"]


## docker build --platform linux/amd64 -t jayutest.best:56789/cj-dashboard/cj-dashboard:4.7 .
## docker run -d --name cj-dashboard jayutest.best:56789/cj-dashboard/cj-dashboard:4.6