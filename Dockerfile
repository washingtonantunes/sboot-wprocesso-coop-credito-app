# Multi-stage build.
# Stage 1: compila o jar com Maven dentro do container (sem depender de
# target/ pre-existente no host).
# Stage 2: runtime enxuto com JRE, so com o jar final.
#
# skipTests + spotless.check.skip: build do ambiente Docker eh so pra
# empacotar rapidamente. Testes e formatacao devem ser validados no CI/dev
# antes do commit.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
COPY config config
RUN mvn -B -DskipTests -Dspotless.check.skip=true package \
    && cp target/*.jar /app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-Xmx512m","-jar","app.jar"]
