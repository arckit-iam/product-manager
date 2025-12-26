
# Utiliser une image de base officielle de Java 21
FROM eclipse-temurin:21-jdk-alpine

# Installer Maven
RUN apk update && apk add --no-cache maven

# Définir le répertoire de travail dans le conteneur
WORKDIR /app

# Copier le fichier pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le reste du code source
COPY src ./src

# Compiler le projet
RUN mvn package -DskipTests

# Exposer le port sur lequel l'application s'exécute
EXPOSE 8080

# Démarrer l'application
CMD ["java", "-jar", "target/ppm1-0.0.1-SNAPSHOT.jar"]