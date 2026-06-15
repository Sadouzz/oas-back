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

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]