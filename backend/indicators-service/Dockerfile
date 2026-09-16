FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY backend/pom.xml backend/pom.xml
COPY backend/requests-service/pom.xml backend/requests-service/pom.xml
COPY backend/indicators-service/pom.xml backend/indicators-service/pom.xml
COPY backend/indicators-service/src backend/indicators-service/src
RUN mvn -B -pl backend/indicators-service -am package -DskipTests
FROM eclipse-temurin:21.0.8_9-jre
RUN useradd --system --uid 10001 appuser
WORKDIR /app
COPY --from=build /workspace/backend/indicators-service/target/indicators-service-*.jar app.jar
USER appuser
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
