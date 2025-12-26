# Déploiement PPM1 sur OVH

Ce guide explique comment déployer automatiquement l'application PPM1 sur OVH via GitHub Actions.

## 🛠️ Configuration OVH requise

### 1. VPS OVH
- Un VPS avec Ubuntu 20.04+ ou Debian 11+
- Au moins 2GB RAM et 20GB d'espace disque
- Accès SSH configuré

### 2. GitHub Container Registry
- GHCR est automatiquement disponible avec GitHub
- Authentification via GitHub Token
- URL: `ghcr.io/your-github-username`

## 🔧 Configuration GitHub Secrets

Ajoutez ces secrets dans votre repository GitHub (Settings > Secrets and variables > Actions) :

### Secrets OVH
```
OVH_SSH_PRIVATE_KEY     # Clé privée SSH pour accéder au VPS
OVH_SSH_USER            # Nom d'utilisateur SSH (ex: ubuntu, root)
OVH_SERVER_IP           # Adresse IP publique du VPS
GHCR_REGISTRY_URL       # URL du GitHub Container Registry (ex: ghcr.io/your-username)
GITHUB_TOKEN            # Token GitHub avec permissions packages:write
```

### Secrets Application
```
DB_PASSWORD             # Mot de passe MySQL pour l'application
ROOT_DB_PASSWORD        # Mot de passe root MySQL
API_SECURITY_ACCESS     # Mot de passe pour l'API Spring Security
```

## 🚀 Processus de déploiement

### 1. Setup initial (une seule fois)
```bash
# Exécuter le workflow "ppm1-setup-ovh" depuis GitHub Actions
# Cela installe Docker, configure l'environnement sur le VPS
```

### 2. Déploiement automatique

#### Via Tags Git (recommandé)
```bash
git tag v1.0.0
git push origin v1.0.0
```

#### Via GitHub Actions (manuel)
- Aller dans Actions > ppm1-integration-ovh > Run workflow

### 3. Vérification
- L'application sera disponible sur `http://VOTRE_IP_VPS:8080`
- Health check: `http://VOTRE_IP_VPS:8080/actuator/health`

## 🐳 Commandes Docker utiles sur le VPS

```bash
# Se connecter au VPS
ssh user@YOUR_VPS_IP

# Voir les conteneurs
cd /opt/ppm1
docker-compose ps

# Voir les logs
docker-compose logs -f ppm1-api

# Redémarrer l'application
docker-compose restart ppm1-api

# Mise à jour manuelle
docker-compose down
docker-compose pull
docker-compose up -d
```

## 🔧 Développement local

```bash
# Utiliser le docker-compose de développement
docker-compose -f docker-compose.dev.yml up -d

# Ou déploiement standard
docker-compose up -d
```

## 📂 Structure des fichiers

```
.github/workflows/
├── integration.yml     # Build et push des images
├── delivery.yml        # Déploiement sur OVH
└── build-cloud.yml     # Setup initial OVH

docker-compose.yml      # Production (tire l'image du registry)
docker-compose.dev.yml  # Développement (build local)
```

## 🔍 Troubleshooting

### Problème de connexion SSH
```bash
# Vérifier la clé SSH
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
cat ~/.ssh/id_rsa.pub # Ajouter au VPS

# Tester la connexion
ssh -o StrictHostKeyChecking=no user@YOUR_VPS_IP
```

### Problème de déploiement
```bash
# Sur le VPS, vérifier les logs
cd /opt/ppm1
docker-compose logs

# Vérifier l'espace disque
df -h

# Nettoyer Docker
docker system prune -a
```

### Base de données
```bash
# Accéder à MySQL
docker-compose exec ppm1-db mysql -u ppm1_api -p ppm1

# Backup de la DB
docker-compose exec ppm1-db mysqldump -u ppm1_api -p ppm1 > backup.sql
```

## 🔒 Sécurité

- Changez les mots de passe par défaut
- Configurez un firewall (ufw)
- Utilisez HTTPS avec Let's Encrypt (Certbot)
- Limitez l'accès SSH par clé uniquement

## 🌐 Nom de domaine (optionnel)

Pour utiliser un nom de domaine :

1. Pointer le domaine vers l'IP du VPS
2. Configurer un reverse proxy (Nginx)
3. Ajouter SSL avec Let's Encrypt

```bash
# Exemple avec Nginx
sudo apt install nginx certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```