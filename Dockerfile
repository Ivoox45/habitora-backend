# ---- Etapa 1: Compilar ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
# Opción A: usar wrapper (mvnw)
RUN ./mvnw -version && ./mvnw clean package -DskipTests
# Si NO vas a commitear el wrapper, usa la Opción B más abajo

# ---- Etapa 2: Ejecutar ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
