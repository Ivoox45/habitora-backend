# ---- Etapa 1: Compilación ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiar Maven wrapper y configuración primero
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargar dependencias en caché
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Compilar el proyecto
RUN ./mvnw clean package -DskipTests

# ---- Etapa 2: Ejecución ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
