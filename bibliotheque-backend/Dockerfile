###########################################################
# Étape 1 — Cache des dépendances Maven
# (ne se reconstruit que si pom.xml change)
###########################################################
FROM maven:3.9-eclipse-temurin-21 AS deps
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

###########################################################
# Étape 2 — Build de l'application
###########################################################
FROM deps AS build
COPY src ./src
RUN mvn -B clean package -DskipTests

###########################################################
# Étape 3 — Runtime JRE Alpine, utilisateur non-root
###########################################################
FROM eclipse-temurin:21-jre-alpine AS runtime

# Utilisateur applicatif sans privilèges
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

USER spring
EXPOSE 8080

# Mémoire tunable via docker-compose (JAVA_OPTS)
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
