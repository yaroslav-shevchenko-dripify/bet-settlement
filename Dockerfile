FROM eclipse-temurin:25-jdk-alpine AS build

ARG SERVICE
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon :${SERVICE}:bootJar

FROM eclipse-temurin:25-jre-alpine

ARG SERVICE
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/${SERVICE}/build/libs/*.jar app.jar
USER app
ENTRYPOINT ["java", "--sun-misc-unsafe-memory-access=allow", "-Drocketmq.log.root=/tmp/rocketmqlogs", "-jar", "app.jar"]
