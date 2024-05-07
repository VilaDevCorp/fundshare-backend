FROM openjdk:17-jdk-alpine
COPY target/backtemplate-0.0.1-SNAPSHOT.jar backtemplate-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/backtemplate-0.0.1-SNAPSHOT.jar"]
