FROM gradle:9.7-jdk25 AS build
WORKDIR /workspace
COPY . .
RUN gradle test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/build/classes/java/main /app/classes
ENTRYPOINT ["java", "-cp", "/app/classes"]
