
# Utiliser une image de base officielle de Java 21
FROM openjdk:21-jdk-slim

# Installer Maven
RUN apt-get update && apt-get install -y maven

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