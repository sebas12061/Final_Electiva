# ===============================
# Etapa 1: Compilación del proyecto
# ===============================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiar todos los archivos del proyecto
COPY . .

# Compilar el código y generar el JAR (sin ejecutar tests)
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# ===============================
# Etapa 2: Imagen final
# ===============================
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto del microservicio
EXPOSE 8082

# Iniciamos el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]

# ===============================
# FIN DEL DOCKERFILE - LUGARES
# ===============================
