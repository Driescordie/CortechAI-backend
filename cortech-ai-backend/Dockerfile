# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Kopieer alle jars
COPY --from=build /app/target/*.jar /app/

# Hernoem de shaded jar naar app.jar
RUN mv /app/*shaded*.jar /app/app.jar

EXPOSE 10000
CMD ["java", "-jar", "app.jar"]
