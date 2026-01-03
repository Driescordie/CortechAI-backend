# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Kopieer de shaded jar (de enige die een Main-Class bevat)
COPY --from=build /app/target/*-shaded.jar app.jar

EXPOSE 10000
CMD ["java", "-jar", "app.jar"]
