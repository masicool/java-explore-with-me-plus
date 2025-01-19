FROM amazoncorretto:21-alpine
COPY target/*.jar main-service.jar
ENTRYPOINT ["java", "-jar", "/main-service.jar"]