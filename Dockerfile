FROM eclipse-temurin:23
COPY target/trabajo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]