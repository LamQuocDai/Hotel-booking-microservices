FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

EXPOSE 3002 50051

CMD ["java", "-jar", "target/account-service-0.0.1-SNAPSHOT.jar"]
