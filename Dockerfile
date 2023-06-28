FROM openjdk:17
COPY . /usr/src/newsApp
WORKDIR /usr/src/newsApp

EXPOSE 8080

RUN microdnf install findutils

CMD ["/bin/bash", "-c", "./gradlew update;cd build/libs;java -jar news-0.0.1-SNAPSHOT.jar"]
