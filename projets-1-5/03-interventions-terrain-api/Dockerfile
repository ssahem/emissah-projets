# syntax=docker/dockerfile:1

# ---- Étape 1 : compilation (Maven + JDK) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Les dépendances d'abord : cette couche reste en cache tant que le pom ne change pas.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
  && cp target/*.jar application.jar \
  && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---- Étape 2 : exécution (JRE seul, utilisateur non-root) ----
FROM eclipse-temurin:21-jre
WORKDIR /application

RUN useradd --system --uid 10001 --no-create-home --shell /usr/sbin/nologin app

COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER app
EXPOSE 8083

# La JVM dimensionne le heap selon la mémoire allouée au conteneur.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75" \
    SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "application.jar"]
