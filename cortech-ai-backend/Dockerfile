# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Kopieer ALLE jars uit target
COPY --from=build /app/target/*.jar /app/

# Zoek de shaded jar en hernoem hem naar app.jar
RUN JAR_FILE=$(ls /app | grep shaded | head -n 1) && mv "/app/$JAR_FILE" /app/app.jar

EXPOSE 10000
CMD ["java", "-jar", "app.jar"]
