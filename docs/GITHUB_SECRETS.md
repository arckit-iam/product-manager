# Configuration des Secrets GitHub pour OVH avec GitHub Container Registry

Pour utiliser les workflows de déploiement automatique sur OVH avec GitHub Container Registry, vous devez configurer les secrets suivants dans votre repository GitHub.

## Comment ajouter des secrets GitHub

1. Aller sur votre repository GitHub
2. Cliquer sur **Settings** 
3. Dans la sidebar, cliquer sur **Secrets and variables** → **Actions**
4. Cliquer sur **New repository secret**
5. Ajouter les secrets listés ci-dessous

## 🔐 Secrets requis

### Accès OVH VPS
```
OVH_SSH_PRIVATE_KEY
```
**Valeur**: Votre clé privée SSH (contenu du fichier ~/.ssh/id_rsa)
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
...
-----END OPENSSH PRIVATE KEY-----
```

```
OVH_SSH_USER
```
**Valeur**: Nom d'utilisateur SSH (ex: `ubuntu`, `debian`, `root`)

```
OVH_SERVER_IP
```
**Valeur**: Adresse IP publique de votre VPS OVH (ex: `51.68.123.456`)

### GitHub Container Registry
```
GHCR_REGISTRY_URL
```
**Valeur**: URL du GitHub Container Registry
- Format: `ghcr.io/your-github-username`
- Exemple: `ghcr.io/etien-dev`

```
GITHUB_TOKEN
```
**Valeur**: Token GitHub avec permissions `packages:write`
- Peut être généré automatiquement par GitHub Actions
- Ou un Personal Access Token (PAT) avec scope `write:packages`

### Base de données
```
DB_PASSWORD
```
**Valeur**: Mot de passe MySQL pour l'utilisateur application (ex: `MySecurePassword123!`)

```
ROOT_DB_PASSWORD
```
**Valeur**: Mot de passe MySQL root (ex: `MyRootPassword456!`)

### Sécurité application
```
API_SECURITY_ACCESS
```
**Valeur**: Mot de passe pour l'accès à l'API Spring Security (ex: `ApiSecurePass789!`)

## 📋 Exemple de configuration

### 1. Génération de clé SSH
```bash
# Sur votre machine locale
ssh-keygen -t rsa -b 4096 -C "github-actions@yourdomain.com"

# Copier la clé publique sur le VPS OVH
ssh-copy-id -i ~/.ssh/id_rsa.pub user@YOUR_VPS_IP

# Récupérer la clé privée pour GitHub (garder les sauts de ligne)
cat ~/.ssh/id_rsa
```

### 2. Configuration GitHub Container Registry
1. GHCR est automatiquement disponible avec votre repository GitHub
2. Aucune configuration supplémentaire requise
3. L'authentification se fait automatiquement via GITHUB_TOKEN
4. URL format: `ghcr.io/your-github-username/repository-name`

### 3. Configuration VPS OVH
```bash
# Se connecter au VPS
ssh user@YOUR_VPS_IP

# Installer Docker (si nécessaire)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Ajouter l'utilisateur au groupe docker
sudo usermod -aG docker $USER

# Installer Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Créer le répertoire de l'application
sudo mkdir -p /opt/ppm1
sudo chown $USER:$USER /opt/ppm1
```

## ✅ Vérification des secrets

Avant de lancer le déploiement, vérifiez que :

- [ ] Tous les secrets sont configurés dans GitHub
- [ ] La clé SSH fonctionne : `ssh user@YOUR_VPS_IP`
- [ ] Le registry Docker est accessible
- [ ] Le VPS OVH a Docker et Docker Compose installés
- [ ] Le port 8080 est ouvert sur le VPS

## 🚀 Déclenchement du déploiement

Une fois les secrets configurés :

### Automatique (sur tag)
```bash
git tag v1.0.0
git push origin v1.0.0
```

### Manuel
1. Aller dans **Actions** sur GitHub
2. Sélectionner **pmp1-integration-ovh**
3. Cliquer **Run workflow**

## 🔧 Dépannage

### Test de connexion SSH
```bash
ssh -o StrictHostKeyChecking=no user@YOUR_VPS_IP "echo 'SSH OK'"
```

### Test du registry
```bash
echo "PASSWORD" | docker login REGISTRY_URL -u USERNAME --password-stdin
```

### Vérification des secrets GitHub
Les secrets ne sont pas visibles une fois créés. Si un déploiement échoue, vérifiez :
1. Les noms des secrets (respect de la casse)
2. Les valeurs (pas d'espaces en trop)
3. Les permissions du repository