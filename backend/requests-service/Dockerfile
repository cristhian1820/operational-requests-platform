FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY backend/pom.xml backend/pom.xml
COPY backend/requests-service/pom.xml backend/requests-service/pom.xml
COPY backend/indicators-service/pom.xml backend/indicators-service/pom.xml
COPY backend/requests-service/src backend/requests-service/src
RUN mvn -B -pl backend/requests-service -am package -DskipTests
FROM eclipse-temurin:21.0.8_9-jre
RUN useradd --system --uid 10001 appuser
WORKDIR /app
COPY --from=build /workspace/backend/requests-service/target/requests-service-*.jar app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
