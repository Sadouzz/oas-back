# Etape 1: Build avec Maven
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copier les fichiers Maven wrapper et POM en premier
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copier le code source et build
COPY src src
RUN ./mvnw package -DskipTests -B

# Etape 2: Runtime léger
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copier le JAR buildé
COPY --from=build /app/target/*.jar app.jar

ENV PORT=9090
EXPOSE 9090 10000 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-9090} -jar app.jar"]