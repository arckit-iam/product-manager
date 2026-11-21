# PPM1 - Profile & Permissions Management

[![Build Status](https://github.com/arckit-iam/product-manager/actions/workflows/integration.yml/badge.svg)](https://github.com/arckit-iam/product-manager/actions/workflows/integration.yml)
[![Test Coverage](https://img.shields.io/badge/Coverage-100%25-brightgreen)](docs/TEST_COVERAGE.md)
[![Tests](https://img.shields.io/badge/Tests-130%20passing-green)](src/test)
[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-EUPL--1.2-blue.svg)](LICENSE)

## 📋 Description

PPM1 est une application Spring Boot dédiée à la gestion des profils et des permissions. Elle repose sur une **architecture hexagonale**, avec des adaptateurs pour la persistance des données et l'exposition d'une API REST.

### ✨ Caractéristiques principales

- 🏗️ **Architecture hexagonale** propre et maintenable
- ✅ **Couverture de tests à 100 %** avec 130 tests
- 🔧 **MapStruct** pour le mapping automatique
- 🐳 **Prise en charge de Docker** avec MySQL
- 🚀 **Pipeline CI/CD** avec GitHub Actions

## 🏗️ Architecture

```text
src/
├── main/
│   └── java/
│       └── secop/
│           └── ppm1/
│               ├── ProfileApplication.java
│               ├── domain/
│               │   ├── model/
│               │   ├── port/
│               │   └── service/
│               └── adapters/
│                   ├── in/       # Adaptateurs entrants, par exemple REST
│                   └── out/      # Adaptateurs sortants, par exemple JPA
└── test/
    └── java/                     # Tests unitaires et d'intégration
```

## 🚀 Démarrage rapide

### Prérequis

- **Java 21** ou supérieur
- **Maven 3.8+**, ou le Maven Wrapper fourni
- **Docker** facultatif, pour lancer MySQL
- **MySQL 8.0** pour l'environnement de production

### Installation

```bash
# Cloner le dépôt
git clone https://github.com/arckit-iam/product-manager.git
cd product-manager

# Copier la configuration locale
cp .env.local .env

# Lancer les tests
./mvnw test

# Démarrer l'application
./mvnw spring-boot:run
```

> Sous Windows, utilisez `mvnw.cmd` à la place de `./mvnw`.

### Base de données

#### Développement et tests avec H2

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Production avec MySQL

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ppm1
spring.datasource.username=ppm1_user
spring.datasource.password=${DB_PASSWORD}
```

## 🧪 Tests et qualité

### Couverture des tests

Le projet utilise **JaCoCo** pour mesurer la couverture des tests. Les classes générées par MapStruct sont automatiquement exclues du rapport.

```bash
# Générer le rapport de couverture
./mvnw clean test jacoco:report

# Script rapide sous Windows
.\scripts\coverage-simple.ps1

# Script détaillé sous Windows
.\scripts\coverage.ps1

# Script sous Linux ou macOS
./scripts/coverage.sh
```

Pour ouvrir le rapport HTML sous Windows :

```powershell
start target\site\jacoco\index.html
```

Le rapport est disponible à l'emplacement suivant :

```text
target/site/jacoco/index.html
```

### 🏆 Métriques actuelles

- **Tests** : 130
- **Couverture globale** : 100 %
- **Instructions** : 100 % (21/21)
- **Lignes** : 100 % (4/4)
- **Méthodes** : 100 % (1/1)

### 📋 Répartition des tests

| Type de test | Nombre | Description |
|---|---:|---|
| **Tests unitaires** | 73 | Tests des classes du domaine |
| **Tests d'intégration** | 33 | Tests des adaptateurs JPA et des services |
| **Tests de contrôleurs** | 10 | Tests REST avec `@WebMvcTest` |
| **Tests d'exceptions** | 14 | Tests de la gestion des erreurs |
| **Total** | **130** | Ensemble des tests automatisés |

### ✅ Objectifs de qualité

| Métrique | Cible | Résultat |
|---|---:|---:|
| Instructions | ≥ 80 % | **100 %** |
| Lignes | ≥ 80 % | **100 %** |
| Méthodes | ≥ 90 % | **100 %** |
| Classes métier | ≥ 80 % | **100 %** |

### 🎯 Configuration de JaCoCo

- **Classes exclues** : `*MapperImpl.class`, générées par MapStruct
- **Seuil minimal** : 80 %
- **Formats des rapports** : HTML, XML et CSV
- **CI/CD** : intégration avec GitHub Actions

## 🐳 Docker

### Construction locale

```bash
docker build -t profile-app .
```

### Exécution du conteneur

```bash
docker run --rm -p 8080:8080 profile-app
```

### Docker Compose avec MySQL

```bash
# Démarrer les services
docker compose up -d

# Arrêter les services
docker compose down
```

## 🚀 Déploiement

### GitHub Container Registry

Le projet utilise **GitHub Container Registry**, ou GHCR, pour stocker les images Docker.

- **Registre** : `ghcr.io/etiennesalle/profile`
- **Tags** : versions sémantiques, par exemple `1.0.0`, et `latest`
- **Authentification** : jeton GitHub configuré dans GitHub Actions

### Déploiement sur OVH

Sous Windows :

```powershell
.\scripts\deploy-ovh.ps1
```

Sous Linux ou macOS :

```bash
./scripts/deploy-ovh.sh
```

Consultez le [guide de déploiement OVH](docs/DEPLOYMENT_OVH.md) pour obtenir la configuration complète.

## 🔧 Développement

### Structure des packages

```text
secop.ppm1/
├── ProfileApplication.java       # Point d'entrée Spring Boot
├── domain/                       # Couche métier
│   ├── model/                    # Entités du domaine
│   ├── port/                     # Interfaces et contrats
│   └── service/                  # Services métier
└── adapters/                     # Couche d'infrastructure
    ├── in/                       # Adaptateurs entrants, par exemple REST
    └── out/                      # Adaptateurs sortants, par exemple JPA
```

### Technologies utilisées

- **Framework** : Spring Boot 3.3.5
- **Langage** : Java 21
- **Persistance** : Spring Data JPA et MySQL
- **Mapping** : MapStruct
- **Tests** : JUnit 5, Mockito et `@WebMvcTest`
- **Couverture** : JaCoCo
- **Build** : Maven
- **Conteneurisation** : Docker
- **CI/CD** : GitHub Actions

### Scripts de développement

| Script | Description | Plateforme |
|---|---|---|
| `coverage-simple.ps1` | Génération rapide du rapport de couverture | Windows |
| `coverage.ps1` | Génération complète du rapport de couverture | Windows |
| `coverage.sh` | Génération complète du rapport de couverture | Linux/macOS |
| `deploy-ovh.ps1` | Déploiement sur OVH | Windows |
| `deploy-ovh.sh` | Déploiement sur OVH | Linux/macOS |

## 📊 Métriques et supervision

### Endpoints Spring Boot Actuator

- **État de l'application** : `[localhost](http://localhost:8080/actuator/health)`
- **Métriques** : `[localhost](http://localhost:8080/actuator/metrics)`
- **Informations** : `[localhost](http://localhost:8080/actuator/info)`

### Rapports de qualité

- **Rapport JaCoCo** : `target/site/jacoco/index.html`
- **Rapports Surefire** : `target/surefire-reports/`

## 🤝 Contribution

### Workflow de développement

1. Forker le dépôt.
2. Créer une branche :

   ```bash
   git checkout -b feature/awesome-feature
   ```

3. Enregistrer les changements :

   ```bash
   git commit -m "feat: add awesome feature"
   ```

4. Envoyer la branche :

   ```bash
   git push origin feature/awesome-feature
   ```

5. Ouvrir une Pull Request.

### Standards de code

- **Tests** : maintenir une couverture minimale de 80 % pour les nouvelles fonctionnalités
- **Architecture** : respecter la séparation des couches de l'architecture hexagonale
- **MapStruct** : exclure les classes générées des rapports de couverture
- **Documentation** : ajouter de la JavaDoc aux API publiques
- **Style** : respecter le Google Java Style Guide
- **Commits** : suivre la convention Conventional Commits

## 📚 Documentation

- [Guide de déploiement OVH](docs/DEPLOYMENT_OVH.md)
- [Configuration des secrets GitHub](docs/GITHUB_SECRETS.md)
- [Guide de couverture des tests](docs/TEST_COVERAGE.md)
- [Résumé de la migration vers GHCR](MIGRATION_SUMMARY.md)
- [Intégration arc-publisher](docs/API.md)
- [Documentation de la plateforme (arc-docs)](https://github.com/arckit-iam/arc-docs) : modèle abstrait, architecture globale, publication et messaging
- [Déploiement (arc-kubeorchestrator)](https://github.com/arckit-iam/arc-kubeorchestrator/tree/main/docs) : Helm, Flux et CI/CD

## 📞 Support

- [GitHub Issues](https://github.com/arckit-iam/product-manager/issues)
- [GitHub Discussions](https://github.com/arckit-iam/product-manager/discussions)

## 📝 Licence

Copyright © 2026 Dédale Solutions

Ce projet est distribué sous la licence [EUPL-1.2](LICENSE) (European Union Public Licence v. 1.2).

---

**Développé avec ❤️ par l'équipe PPM1**
