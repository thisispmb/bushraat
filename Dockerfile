FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn

RUN mvn -B -DskipTests dependency:go-offline

COPY src src

RUN mvn -B -DskipTests package


FROM tomcat:11.0.25-jre21-temurin-noble

ENV CATALINA_OPTS="-Dfile.encoding=UTF-8"

WORKDIR /usr/local/tomcat

RUN rm -rf webapps/*

COPY --from=build /app/target/bushraat-1.0-SNAPSHOT.war webapps/ROOT.war

COPY docker/start.sh /usr/local/bin/bushraat-start.sh

RUN chmod +x /usr/local/bin/bushraat-start.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/bushraat-start.sh"]