# ===============================
# Etapa 1: Compilación del proyecto
# ===============================
FROM eclipse-temurin:21-jdk AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos todos los archivos del proyecto (pom.xml + src/)
COPY . .

# Ejecutamos Maven para compilar el proyecto y generar el .jar
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# ===============================
# Etapa 2: Imagen final
# ===============================
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copiamos el JAR compilado desde la etapa "build"
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto por el que escucha el microservicio
EXPOSE 8081

# Comando de inicio del microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]
