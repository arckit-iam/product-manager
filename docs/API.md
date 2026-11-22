# Product Manager — API Documentation

## Vue d'ensemble

**product-manager** est un service de gestion de produits applicatifs et de leurs profils de sécurité.
Chaque profil est associé à un groupe AD via le service **arc-publisher** : la création ou la suppression d'un profil déclenche automatiquement un job de provisioning.

---

## Authentification

Jeton JWT émis par [arc-auth](https://github.com/arckit-iam/arc-auth) (`POST /token`), à présenter sur chaque requête :

```
Authorization: Bearer <jwt>
```

| Endpoints | Rôle requis |
|-----------|-------------|
| `GET /products`, `GET /products/{productName}` | `ARC_READER`, `ARC_PUBLISHER` ou `ARC_ADMIN` |
| `POST`, `PUT`, `DELETE` sur `/products/**` (dont `POST /products/{productName}/profiles`) | `ARC_PUBLISHER` ou `ARC_ADMIN` |
| `/actuator/health` | public |

Sans jeton valide : `401`. Avec un rôle insuffisant : `403`. Tout autre chemin est refusé.

---

## Endpoints

### Produits

#### GET /products
Retourne la liste de tous les produits.

**Réponse 200**
```json
[
  {
    "name": "mon-application",
    "description": "Application métier",
    "profiles": []
  }
]
```

---

#### GET /products/{productName}
Retourne un produit par son nom.

**Paramètres**
| Paramètre | Type | Description |
|-----------|------|-------------|
| productName | path | Nom unique du produit |

**Réponse 200**
```json
{
  "name": "mon-application",
  "description": "Application métier",
  "profiles": [
    {
      "name": "GRP_APP_ADMIN",
      "description": "Administrateurs",
      "scope": "TIER1"
    }
  ]
}
```

---

#### POST /products
Crée un nouveau produit.

**Body**
```json
{
  "name": "mon-application",
  "description": "Application métier"
}
```

**Réponse 201** — Location: `/products/mon-application`
```json
{
  "name": "mon-application",
  "description": "Application métier",
  "profiles": []
}
```

---

#### PUT /products/{productName}
Met à jour un produit existant.

**Body**
```json
{
  "name": "mon-application",
  "description": "Nouvelle description"
}
```

**Réponse 200** — produit mis à jour.

---

#### DELETE /products/{productName}
Supprime un produit.

**Réponse 200**

---

### Profils

#### POST /products/{productName}/profiles
Crée un profil pour un produit. **Déclenche la création d'un groupe AD via arc-publisher.**

**Paramètres**
| Paramètre | Type | Description |
|-----------|------|-------------|
| productName | path | Nom du produit parent |

**Body**
```json
{
  "name": "GRP_APP_ADMIN",
  "description": "Administrateurs de l'application",
  "scope": "TIER1"
}
```

| Champ | Obligatoire | Description |
|-------|-------------|-------------|
| name | ✅ | Nom technique du groupe → `sAMAccountName` et `CN` dans l'AD |
| description | ❌ | Description → `displayName` dans l'AD |
| scope | ❌ | OU cible dans l'AD : `TIER0`, `TIER1`, `TIER2` (défaut : TIER2) |

**Réponse 200** — produit avec la liste de profils mise à jour.
```json
{
  "name": "mon-application",
  "description": "Application métier",
  "profiles": [
    {
      "name": "GRP_APP_ADMIN",
      "description": "Administrateurs de l'application",
      "scope": "TIER1"
    }
  ]
}
```

---

### Types de compte

#### GET /account-types
Types de compte définis par la configuration du déploiement.

```json
[
  {
    "name": "service-account",
    "kind": "NON_USER",
    "pattern": "svc-(?<product>[a-z0-9]+)-[a-z]+",
    "metadata": [{ "name": "owner", "type": "STRING", "required": true }]
  }
]
```

### Comptes non utilisateurs

Lecture : `ARC_READER`, `ARC_PUBLISHER` ou `ARC_ADMIN`. Écriture : `ARC_PUBLISHER` ou `ARC_ADMIN`.

| Endpoint | Description | Codes |
|----------|-------------|-------|
| `GET /accounts?product=&type=&uncorrelated=` | Liste filtrable | 200 |
| `GET /products/{productName}/accounts` | Comptes d'un produit | 200, 404 |
| `GET /accounts/{identifier}` | Un compte | 200, 404 |
| `POST /accounts` | Création | 201, 400, 409 |
| `PUT /accounts/{identifier}/metadata` | Remplace les métadonnées | 200, 400, 404 |
| `PUT /accounts/{identifier}/product` | Corrélation manuelle `{"product":"compta"}` | 200, 404 |
| `POST /accounts/correlate` | Rejoue les regex ; `{"correlated": n}` | 200 |
| `DELETE /accounts/{identifier}` | Suppression + job DELETE | 204, 404 |

**POST /accounts**
```json
{ "identifier": "svc-compta-batch", "metadata": { "owner": "alice" } }
```

Le type et le produit sont déduits de l'identifiant par les regex des types configurés (`type` peut aussi être fourni). Réponse 201 :
```json
{
  "identifier": "svc-compta-batch",
  "type": "service-account",
  "product": "compta",
  "source": "API",
  "metadata": { "owner": "alice" },
  "jobId": "7f8422ec-9ad4-48b2-95ef-36f8a3d0a220",
  "createdAt": "2026-01-01T12:00:00Z"
}
```

`400` : identifiant ne correspondant à aucun type, ou métadonnées invalides (champ inconnu, obligatoire manquant, mauvais type). `409` : identifiant déjà existant.

La création publie un job `CREATE`/`IDENTITY` auprès d'arc-publisher avec les attributs `metadata.<champ>`, `product` et `accountType` ; la suppression publie un job `DELETE`.

---

## Intégration arc-publisher

Lors de la création d'un profil, product-manager envoie à arc-publisher :

```json
{
  "action": "CREATE",
  "targetType": "ENTITLEMENT",
  "targetSystem": "ad://AD01",
  "entitlementId": "<profile.name>",
  "entitlementName": "<profile.description>",
  "scope": "<profile.scope>",
  "identityId": "<productName>",
  "identityName": "<productName>",
  "executeAt": "<now>",
  "correlationId": "<uuid>"
}
```

arc-publisher publie ensuite l'event sur le topic Kafka `arc.provisioning.entitlement`, qui est consommé par **arc-agent-ad** pour créer le groupe dans l'Active Directory.

L'appel est authentifié : product-manager obtient auprès d'arc-auth, avec son compte de service (membre de `arc-publishers`), un jeton qu'il conserve jusqu'à peu avant son expiration. Si arc-publisher le refuse (`401`), un nouveau jeton est demandé et l'appel est retenté une fois.

---

## Configuration

| Propriété | Variable d'environnement | Défaut | Description |
|-----------|--------------------------|--------|-------------|
| `arc.publisher.url` | `ARC_PUBLISHER_URL` | `http://arc-publisher:8081` | URL du service arc-publisher |
| `arc.publisher.target-system` | `ARC_PUBLISHER_TARGET_SYSTEM` | `ad://AD01` | Identifiant du système AD cible |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `ARC_SECURITY_ISSUER_URI` | aucun (obligatoire) | Valeur attendue du claim `iss` |
| `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | `ARC_SECURITY_JWK_SET_URI` | `http://arc-auth:8082/.well-known/jwks.json` | Clés publiques d'arc-auth |
| `spring.security.oauth2.resourceserver.jwt.audiences` | `ARC_SECURITY_AUDIENCE` | `arc` | Valeur attendue du claim `aud` |
| `arc.auth.url` | `ARC_AUTH_URL` | `http://arc-auth:8082` | URL d'arc-auth |
| `arc.auth.username` / `arc.auth.password` | `ARC_AUTH_USERNAME` / `ARC_AUTH_PASSWORD` | aucun (obligatoires) | Compte de service pour appeler arc-publisher |

---

## Flux complet — Créer un groupe AD

```
POST /products/{productName}/profiles
        │
        ▼
product-manager
        │  POST /jobs
        ▼
arc-publisher ──► Kafka: arc.provisioning.entitlement
                          │
                          ▼
                   arc-agent-ad ──► Active Directory (création du groupe)
```
