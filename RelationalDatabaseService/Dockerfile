FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -q -e -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S nais && adduser -S nais -G nais
COPY --from=build /src/target/RelationalDatabaseService-*.jar app.jar
USER nais
EXPOSE 9030
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
