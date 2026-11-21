# Product Manager (profile)

Service de gestion des **produits applicatifs** et de leurs **profils de sécurité** (ressources, permissions) de la plateforme [Instant Access Provider](https://github.com/arckit-iam/InstantAccessPublisher).

Chaque profil est associé à un groupe Active Directory : la création ou la suppression d'un profil déclenche un job de provisioning auprès d'[arc-publisher](https://github.com/arckit-iam/arc-publisher), qui publie la commande sur Kafka pour [arc-agent-ad](https://github.com/arckit-iam/arc-agent-ad).

```text
Client ──► POST /products/{productName}/profiles ──► product-manager (PostgreSQL)
                                                          │ POST /jobs (ENTITLEMENT / CREATE)
                                                          ▼
                                                    arc-publisher ──► Kafka ──► arc-agent-ad ──► AD
```

## Stack

- Java 25, Spring Boot 3.5
- PostgreSQL, migrations Flyway
- MapStruct, Spring Security (Basic Auth)
- Maven, Docker

## Architecture hexagonale

```text
secop.ppm1/
  domain/
    model/      Product, Profile, Ressource, Permission, TypePermission, TypeRessource
    port/       ProductProvider, ProfileProvider, RessourceProvider, JobPublisher
    service/    ProfileApplicaManager (logique métier)
    exception/  exceptions métier
  adapters/
    expose/     contrôleur REST, DTO et mappers
    provide/    persistance JPA (entités, repositories, mappers, providers)
    publish/    ArcPublisherJobAdapter : client REST vers arc-publisher
```

Les adapters implémentent les ports de sortie définis dans le domaine.

## API REST

Authentification Basic sur tous les endpoints. Le détail des endpoints, des payloads et des codes de réponse est dans [docs/API.md](docs/API.md).

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/products` | Liste les produits |
| `GET` | `/products/{productName}` | Retourne un produit |
| `POST` | `/products` | Crée un produit |
| `PUT` | `/products/{productName}` | Met à jour un produit |
| `DELETE` | `/products/{productName}` | Supprime un produit |
| `POST` | `/products/{productName}/profiles` | Ajoute un profil (et déclenche la création du groupe AD) |

## Configuration

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (défaut) ou `prod` |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Connexion PostgreSQL |
| `SPRING_FLYWAY_URL` / `_USER` / `_PASSWORD` / `_ENABLED` | Migrations Flyway (désactivées par défaut en `dev`) |
| `SPRING_SECURITY_USER_NAME` / `_PASSWORD` | Compte Basic Auth de l'API |
| `ARC_PUBLISHER_URL` | URL d'arc-publisher (défaut `http://arc-publisher:8081`) |
| `ARC_PUBLISHER_TARGET_SYSTEM` | Système cible des jobs (défaut `ad://DC01`) |
| `ARC_PUBLISHER_SOURCE` | Valeur du champ `source` des jobs (défaut `product-manager`) |

Aucune valeur sensible n'est versionnée : copier [.env.example](.env.example) vers `.env` et adapter les valeurs.

## Démarrage local

Prérequis : Docker.

```bash
cp .env.example .env     # puis éditer les mots de passe
docker compose up --build
```

L'API est exposée sur `http://localhost:8080`. Sans `arc-publisher` joignable, la création d'un profil échoue au moment de l'appel à `/jobs`.

## Développement

Prérequis : JDK 25 et Maven 3.9 (ou `./mvnw`).

```bash
mvn test                           # tests unitaires et de contexte (H2)
mvn clean test jacoco:report       # avec rapport de couverture (target/site/jacoco)
```

## Déploiement

Le chart Helm et les manifestes FluxCD sont dans le dépôt [arc-kubeorchestrator](https://github.com/arckit-iam/arc-kubeorchestrator). Les workflows GitHub Actions (`build.yml`, `promote.yml`, `deploy.yml`) construisent et publient l'image sur GHCR.

## Contribuer et sécurité

Voir [CONTRIBUTING.md](CONTRIBUTING.md) et [SECURITY.md](SECURITY.md).

## Licence

Copyright © 2026 Dédale Solutions

Ce projet est distribué sous la licence [EUPL-1.2](LICENSE) (European Union Public Licence v. 1.2).
