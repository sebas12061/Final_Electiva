# ===============================
# Etapa 1: Compilación del proyecto
# ===============================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiar el contenido del microservicio (pom.xml y código fuente)
COPY . .

# Compilar con Maven (salta los tests)
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# ===============================
# Etapa 2: Imagen final
# ===============================
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiar el JAR resultante desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto de ejecución del microservicio
EXPOSE 8081

# Comando que arranca el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]

# ===============================
# FIN DEL DOCKERFILE - USERS
# ===============================
