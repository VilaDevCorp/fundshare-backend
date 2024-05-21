FROM openjdk:17-jdk-alpine
COPY target/fundshare-0.0.1-SNAPSHOT.jar fundshare-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/fundshare-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]
