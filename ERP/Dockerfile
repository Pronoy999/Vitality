FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY vitality-common ./vitality-common
COPY vitality-database ./vitality-database
COPY vitality-api ./vitality-api

RUN ls -l /app && ls -l /app/

RUN apt-get update && apt-get install -y maven

RUN mvn clean install

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/vitality-api/target/vitality-api-1.0-SNAPSHOT.jar .
COPY --from=build /app/vitality-common/target/vitality-common-1.0-SNAPSHOT.jar .
COPY --from=build /app/vitality-database/target/vitality-database-1.0-SNAPSHOT.jar .
RUN ls -l /app && ls -l /app/

EXPOSE 8080

ENTRYPOINT ["java","-jar","vitality-api-1.0-SNAPSHOT.jar"]