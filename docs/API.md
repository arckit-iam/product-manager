# Product Manager — API Documentation

## Vue d'ensemble

**product-manager** est un service de gestion de produits applicatifs et de leurs profils de sécurité.
Chaque profil est associé à un groupe AD via le service **arc-publisher** : la création ou la suppression d'un profil déclenche automatiquement un job de provisioning.

---

## Authentification

Basic Authentication sur tous les endpoints.

```
Authorization: Basic <base64(username:password)>
```

Les credentials sont configurés via le secret Kubernetes `product-manager-api-secret`.

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

---

## Configuration

| Propriété | Variable d'environnement | Défaut | Description |
|-----------|--------------------------|--------|-------------|
| `arc.publisher.url` | `ARC_PUBLISHER_URL` | `http://arc-publisher:8081` | URL du service arc-publisher |
| `arc.publisher.target-system` | `ARC_PUBLISHER_TARGET_SYSTEM` | `ad://AD01` | Identifiant du système AD cible |

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
