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
- MapStruct, Spring Security (jetons JWT émis par arc-auth)
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

Toutes les routes exigent un jeton `Authorization: Bearer <jwt>` émis par [arc-auth](https://github.com/arckit-iam/arc-auth), sauf `/actuator/health` (public, sondes). Sans jeton valide : `401`. Avec un rôle insuffisant : `403`. Le détail des endpoints, des payloads et des codes de réponse est dans [docs/API.md](docs/API.md).

| Méthode | Endpoint | Rôle requis | Description |
|---|---|---|---|
| `GET` | `/products` | `ARC_READER`, `ARC_PUBLISHER` ou `ARC_ADMIN` | Liste les produits |
| `GET` | `/products/{productName}` | `ARC_READER`, `ARC_PUBLISHER` ou `ARC_ADMIN` | Retourne un produit |
| `POST` | `/products` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Crée un produit |
| `PUT` | `/products/{productName}` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Met à jour un produit |
| `DELETE` | `/products/{productName}` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Supprime un produit |
| `POST` | `/products/{productName}/profiles` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Ajoute un profil (et déclenche la création du groupe AD) |

Tout autre chemin est refusé.

## Configuration

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (défaut) ou `prod` |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Connexion PostgreSQL |
| `SPRING_FLYWAY_URL` / `_USER` / `_PASSWORD` / `_ENABLED` | Migrations Flyway (désactivées par défaut en `dev`) |
| `ARC_SECURITY_ISSUER_URI` | Valeur attendue du claim `iss` (obligatoire ; égale à `ARC_AUTH_JWT_ISSUER` d'arc-auth) |
| `ARC_SECURITY_JWK_SET_URI` | Clés publiques d'arc-auth (défaut `http://arc-auth:8082/.well-known/jwks.json`) |
| `ARC_SECURITY_AUDIENCE` | Valeur attendue du claim `aud` (défaut `arc`) |
| `ARC_AUTH_URL` | URL d'arc-auth (défaut `http://arc-auth:8082`) |
| `ARC_AUTH_USERNAME` / `_PASSWORD` | Compte de service (membre de `arc-publishers`) avec lequel product-manager appelle arc-publisher (obligatoires) |
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

Le déploiement GitOps (Helm, FluxCD) est géré dans un dépôt privé. L'intégration et la mise en production de la plateforme sont proposées par Dédale Solutions en **service payant** : pour en discuter, contactez-nous à [etiennsalle@gmail.com](mailto:etiennsalle@gmail.com). Les workflows GitHub Actions (`build.yml`, `promote.yml`, `deploy.yml`) construisent et publient l'image sur GHCR.

## Contribuer et sécurité

Voir [CONTRIBUTING.md](CONTRIBUTING.md) et [SECURITY.md](SECURITY.md).

## Licence

Copyright © 2026 Dédale Solutions

Ce projet est distribué sous la licence [EUPL-1.2](LICENSE) (European Union Public Licence v. 1.2).
