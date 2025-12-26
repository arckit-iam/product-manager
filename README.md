# PPM1 - Profile & Permissions Management

[![Build Status](https://github.com/EtiennSalle/profile/actions/workflows/integration.yml/badge.svg)](https://github.com/EtiennSalle/profile/actions/workflows/integration.yml)
[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Description

PPM1 est une application Spring Boot pour la gestion des profils et permissions. Elle implémente une architecture hexagonale avec des adaptateurs pour l'accès aux données et l'exposition REST.

## 🏗️ Architecture

```
src/
├── main/java/secop/ppm1/
│   ├── domain/                 # Logique métier
│   │   ├── model/             # Entités du domaine
│   │   ├── port/              # Interfaces (ports)
│   │   └── service/           # Services métier
│   └── adapters/              # Adaptateurs
│       ├── in/                # Adaptateurs entrants (JPA)
│       └── out/               # Adaptateurs sortants (REST)
└── test/                      # Tests unitaires et d'intégration
```

## 🚀 Démarrage Rapide

### Prérequis

- **Java 21** ou supérieur
- **Maven 3.8+**
- **Docker** (optionnel, pour MySQL)
- **MySQL 8.0** (production)

### Installation

```bash
# Cloner le repository
git clone https://github.com/EtiennSalle/profile.git
cd profile

# Copier la configuration locale
cp .env.local .env

# Lancer les tests
./mvnw test

# Démarrer l'application
./mvnw spring-boot:run
```

### Base de Données

#### Développement (H2)
```properties
# Configuration automatique pour les tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Production (MySQL)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ppm1
spring.datasource.username=ppm1_user
spring.datasource.password=${DB_PASSWORD}
```

## 🧪 Tests et Qualité

### Couverture de Tests

Le projet utilise **JaCoCo** pour mesurer la couverture de tests :

```bash
# Génération du rapport de couverture
./mvnw clean test jacoco:report

# Script personnalisé (Windows)
.\scripts\coverage-simple.ps1

# Script personnalisé (Linux/macOS)  
./scripts/coverage.sh

# Ouverture du rapport HTML
start target\site\jacoco\index.html  # Windows
open target/site/jacoco/index.html   # macOS
```

### Métriques Actuelles

- **Tests unitaires** : 116 tests
- **Couverture globale** : ~52% (en cours d'amélioration)
- **Classes métier** : >80% (priorité haute)

### Objectifs de Qualité

| Métrique | Cible | Statut |
|----------|-------|--------|
| Instructions | ≥ 80% | 🔄 En cours |
| Lignes | ≥ 80% | 🔄 En cours |
| Méthodes | ≥ 90% | ✅ Atteint |
| Branches | ≥ 70% | 🔄 En cours |

## 🐳 Docker

### Build Local

```bash
# Construction de l'image
docker build -t profile-app .

# Exécution
docker run -p 8080:8080 profile-app
```

### Docker Compose (avec MySQL)

```bash
# Démarrage complet
docker-compose up -d

# Arrêt
docker-compose down
```

## 🚀 Déploiement

### GitHub Container Registry

Le projet utilise **GitHub Container Registry (GHCR)** pour le stockage des images :

- **Registry** : `ghcr.io/etiennesalle/profile`
- **Tags** : Versions sémantiques (1.0.0) + `latest`
- **Authentification** : GitHub Token automatique

### Déploiement OVH

```bash
# Script de déploiement (Windows)
.\scripts\deploy-ovh.ps1

# Script de déploiement (Linux/macOS)
./scripts/deploy-ovh.sh
```

Voir [DEPLOYMENT_OVH.md](docs/DEPLOYMENT_OVH.md) pour la configuration complète.

## 🔧 Développement

### Structure des Packages

```
secop.ppm1/
├── ProfileApplication.java        # Point d'entrée Spring Boot
├── domain/                        # Couche domaine (métier)
│   ├── model/                    # Entités métier
│   ├── port/                     # Interfaces (contrats)
│   └── service/                  # Logique applicative
└── adapters/                     # Couche infrastructure
    ├── in/                       # Persistance (JPA)
    └── out/                      # Exposition (REST)
```

### Technologies Utilisées

- **Framework** : Spring Boot 3.3.5
- **Persistance** : Spring Data JPA + MySQL
- **Tests** : JUnit 5 + Mockito
- **Couverture** : JaCoCo
- **Build** : Maven
- **Containerisation** : Docker
- **CI/CD** : GitHub Actions

### Scripts de Développement

| Script | Description | Plateforme |
|--------|-------------|------------|
| `coverage-simple.ps1` | Rapport de couverture rapide | Windows |
| `coverage.ps1` | Rapport de couverture complet | Windows |
| `coverage.sh` | Rapport de couverture complet | Linux/macOS |
| `deploy-ovh.ps1` | Déploiement OVH | Windows |
| `deploy-ovh.sh` | Déploiement OVH | Linux/macOS |

## 📊 Métriques et Monitoring

### Endpoints Actuator

- **Health** : `http://localhost:8080/actuator/health`
- **Metrics** : `http://localhost:8080/actuator/metrics`
- **Info** : `http://localhost:8080/actuator/info`

### Rapports de Qualité

- **Couverture JaCoCo** : `target/site/jacoco/index.html`
- **Rapports Surefire** : `target/surefire-reports/`

## 🤝 Contribution

### Workflow de Développement

1. **Fork** le repository
2. **Créer** une branche feature (`git checkout -b feature/awesome-feature`)
3. **Commit** les changements (`git commit -m 'Add awesome feature'`)
4. **Push** vers la branche (`git push origin feature/awesome-feature`)
5. **Ouvrir** une Pull Request

### Standards de Code

- **Tests** : Couverture ≥ 80% pour les nouvelles features
- **Documentation** : JavaDoc pour les APIs publiques
- **Convention** : Google Java Style Guide
- **Commits** : Messages conventionnels (Conventional Commits)

## 📚 Documentation

- [Guide de Déploiement OVH](docs/DEPLOYMENT_OVH.md)
- [Configuration GitHub Secrets](docs/GITHUB_SECRETS.md)
- [Guide de Couverture de Tests](docs/TEST_COVERAGE.md)
- [Résumé de Migration GHCR](MIGRATION_SUMMARY.md)

## 📞 Support

- **Issues** : [GitHub Issues](https://github.com/EtiennSalle/profile/issues)
- **Discussions** : [GitHub Discussions](https://github.com/EtiennSalle/profile/discussions)

## 📝 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

**Développé avec ❤️ par l'équipe PPM1**