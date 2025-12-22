FROM eclipse-temurin:17-jdk-jammy
ARG JAR_FILE=target/carpool-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app_carpool.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app_carpool.jar"]