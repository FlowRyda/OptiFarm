FROM maven:3-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY checkstyle*.xml .

RUN mvn -B clean package -DskipTests -Dgit.commit.id.skip=true

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]