# Product Manager (profile)

Service de gestion des **produits applicatifs**, de leurs **profils de sécurité** (ressources, permissions) et de leurs **comptes non utilisateurs** (comptes de service, techniques) de la plateforme [Instant Access Provider](https://github.com/arckit-iam/InstantAccessPublisher).

Chaque profil est associé à un groupe Active Directory : la création ou la suppression d'un profil déclenche un **ordre** de provisioning, déposé dans la file `arc.orders` consommée par [arc-publisher](https://github.com/arckit-iam/arc-publisher) (ou envoyé à son API), qui publie la commande sur Kafka à la date d'effet pour [arc-agent-ad](https://github.com/arckit-iam/arc-agent-ad). Les dates de début et de fin planifient la création et la suppression ; un ordre non encore exécuté peut être annulé.

```text
Client ──► POST /products/{productName}/profiles ──► product-manager (PostgreSQL)
                                                          │ ordre ENTITLEMENT / CREATE (Kafka arc.orders, ou POST /orders)
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
    model/      Product, Profile, Ressource, Permission, Account, AccountType, MetadataField...
    port/       ProductProvider, ProfileProvider, RessourceProvider, OrderPublisher,
                AccountProvider, AccountTypeCatalog
    service/    ProfileApplicaManager, AccountManager (logique métier)
    exception/  exceptions métier
  adapters/
    expose/     contrôleurs REST, DTO et mappers
    provide/    persistance JPA (entités, repositories, mappers, providers)
    publish/    KafkaOrderPublisher (canal par défaut, file arc.orders) et ArcPublisherOrderAdapter (canal API)
    config/     types de compte définis par la configuration du déploiement
    consume/    AccountDiscoveryListener : découverte via Kafka (arc.sync.identity)
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
| `PUT` | `/products/{productName}/profiles/{profileName}/dates` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Modifie `startDate` / `endDate` du profil ; l'ordre en attente est annulé puis replanifié |
| `POST` | `/products/{productName}/profiles/{profileName}/cancel-pending` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Annule l'ordre planifié non encore exécuté (`409` si aucun) |
| `DELETE` | `/products/{productName}/profiles/{profileName}` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Supprime le profil (ordre DELETE du groupe, ou ordre en attente annulé) |

| `GET` | `/account-types` | `ARC_READER`, `ARC_PUBLISHER` ou `ARC_ADMIN` | Types de compte configurés |
| `GET` | `/accounts` | idem | Liste (filtres `product`, `type`, `uncorrelated=true`) |
| `GET` | `/products/{productName}/accounts` | idem | Comptes d'un produit |
| `GET` | `/accounts/{identifier}` | idem | Retourne un compte |
| `POST` | `/accounts` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Crée un compte (type et produit déduits par regex, ordre CREATE, différé à `startDate` si elle est future) |
| `PUT` | `/accounts/{identifier}/dates` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Modifie `startDate` / `endDate` (dernier jour inclus) ; l'ordre en attente est annulé puis replanifié |
| `POST` | `/accounts/{identifier}/cancel-pending` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Annule l'ordre planifié non encore exécuté (`409` si aucun) |
| `PUT` | `/accounts/{identifier}/metadata` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Remplace les métadonnées |
| `PUT` | `/accounts/{identifier}/product` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Corrèle manuellement le compte à un produit |
| `POST` | `/accounts/correlate` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Rejoue les regex sur les comptes non corrélés |
| `DELETE` | `/accounts/{identifier}` | `ARC_PUBLISHER` ou `ARC_ADMIN` | Supprime le compte (ordre DELETE, planifié au lendemain de `endDate` si elle est définie ; un ordre en attente est annulé) |

Tout autre chemin est refusé.

## Comptes non utilisateurs

Un **type de compte** est défini par le déploiement (Helm/variables d'environnement), sans modifier le code :

- `name` : nom du type ;
- `kind` : `NON_USER` (géré ici) ou `USER` (géré par le futur identity-manager, ignoré par product-manager) ;
- `pattern` : regex à correspondance complète, avec un groupe nommé `product` (corrélation au produit) et/ou `identity` (corrélation à l'identité humaine) ;
- `metadata` : champs autorisés (`name`, `type` = `STRING`/`NUMBER`/`BOOLEAN`, `required`).

Les métadonnées sont stockées en `JSONB` (colonne `account.metadata`, index GIN) : ajouter un champ ne nécessite ni migration ni changement de modèle ; un champ inconnu, manquant (obligatoire) ou mal typé est refusé (`400`).

Un compte arrive de deux façons : par l'API (`source=API`, un ordre de provisioning est publié) ou par **découverte** (`source=DISCOVERED`) à partir des événements `arc.sync.identity` du consolidateur, classés par regex (`ARC_DISCOVERY_ENABLED=true`). Un compte dont le produit n'existe pas encore reste non corrélé jusqu'à `POST /accounts/correlate`.

Exemple :

```yaml
ARC_ACCOUNTS_TYPES_0_NAME: service-account
ARC_ACCOUNTS_TYPES_0_KIND: NON_USER
ARC_ACCOUNTS_TYPES_0_PATTERN: 'svc-(?<product>[a-z0-9]+)-[a-z]+'
ARC_ACCOUNTS_TYPES_0_METADATA_0_NAME: owner
ARC_ACCOUNTS_TYPES_0_METADATA_0_TYPE: STRING
ARC_ACCOUNTS_TYPES_0_METADATA_0_REQUIRED: 'true'
```

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
| `ARC_AUTH_USERNAME` / `_PASSWORD` | Compte de service (membre de `arc-publishers`) avec lequel product-manager appelle arc-publisher (canal `api` uniquement) |
| `ARC_ORDERS_CHANNEL` | Canal des ordres : `kafka` (défaut, file `arc.orders`) ou `api` (REST vers arc-publisher) |
| `ARC_ORDERS_TOPIC` | Topic des ordres (défaut `arc.orders`) |
| `ARC_ORDERS_ZONE` | Fuseau qui définit les jours de `startDate` / `endDate` (défaut `UTC`) |
| `ARC_PUBLISHER_URL` | URL d'arc-publisher, canal `api` uniquement (défaut `http://arc-publisher:8081`) |
| `ARC_PUBLISHER_TARGET_SYSTEM` | Système cible des ordres (défaut `ad://DC01`) |
| `ARC_PUBLISHER_SOURCE` | Valeur du champ `source` des ordres (défaut `product-manager`) |
| `ARC_ACCOUNTS_TYPES_<n>_*` | Types de compte (voir ci-dessus) |
| `ARC_DISCOVERY_ENABLED` | Active la découverte Kafka (défaut `false`) |
| `ARC_DISCOVERY_TOPIC` | Topic des identités consolidées (défaut `arc.sync.identity`) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka (défaut `localhost:9092`) |

Aucune valeur sensible n'est versionnée : copier [.env.example](.env.example) vers `.env` et adapter les valeurs.

## Démarrage local

Prérequis : Docker.

```bash
cp .env.example .env     # puis éditer les mots de passe
docker compose up --build
```

L'API est exposée sur `http://localhost:8080`. En canal `kafka`, les ordres attendent dans `arc.orders` si `arc-publisher` est arrêté ; en canal `api`, la création d'un profil échoue tant qu'il n'est pas joignable.

## Développement

Prérequis : JDK 25 et Maven 3.9 (ou `./mvnw`).

```bash
mvn test                           # tests unitaires et de contexte (H2)
mvn clean test jacoco:report       # avec rapport de couverture (target/site/jacoco)
```

## Déploiement

Le déploiement GitOps (Helm, FluxCD) est géré dans un dépôt privé. L'intégration et la mise en production de la plateforme sont proposées par Dédale Solutions en **service payant** : pour en discuter, contactez-nous à [etiennsalle@gmail.com](mailto:etiennsalle@gmail.com). Les workflows GitHub Actions (`build.yml`, `promote.yml`) construisent et publient l'image sur GHCR.

## Contribuer et sécurité

Voir [CONTRIBUTING.md](CONTRIBUTING.md) et [SECURITY.md](SECURITY.md).

## Licence

Copyright © 2026 Dédale Solutions

Ce projet est distribué sous la licence [EUPL-1.2](LICENSE) (European Union Public Licence v. 1.2).
