#!/bin/bash

# Script de déploiement manuel pour OVH avec GitHub Container Registry
# Usage: ./deploy-ovh.sh [version]

set -e

VERSION=${1:-latest}
REGISTRY_URL="ghcr.io"
GITHUB_OWNER=${GITHUB_REPOSITORY_OWNER:-$(git remote get-url origin | sed 's/.*github.com[/:]\([^/]*\).*/\1/')}
SERVER_IP=${OVH_SERVER_IP}
SSH_USER=${OVH_SSH_USER}

echo "🚀 Déploiement PPM1 version $VERSION sur OVH avec GitHub Container Registry..."

# Vérifier les variables d'environnement
if [ -z "$GITHUB_OWNER" ] || [ -z "$SERVER_IP" ] || [ -z "$SSH_USER" ]; then
    echo "❌ Variables manquantes. Définissez:"
    echo "   export OVH_SERVER_IP=192.168.1.100"
    echo "   export OVH_SSH_USER=ubuntu"
    echo "   GITHUB_OWNER sera détecté automatiquement depuis git"
    exit 1
fi

echo "📦 Build de l'image Docker..."
docker build -t ppm1:$VERSION .

echo "🏷️  Tag pour GitHub Container Registry..."
docker tag ppm1:$VERSION $REGISTRY_URL/$GITHUB_OWNER/ppm1:$VERSION
docker tag ppm1:$VERSION $REGISTRY_URL/$GITHUB_OWNER/ppm1:latest

echo "📤 Push vers GitHub Container Registry..."
# Utilise le GitHub Token pour l'auth
echo $GITHUB_TOKEN | docker login $REGISTRY_URL -u $GITHUB_OWNER --password-stdin
docker push $REGISTRY_URL/$GITHUB_OWNER/ppm1:$VERSION
docker push $REGISTRY_URL/$GITHUB_OWNER/ppm1:latest

echo "🔄 Déploiement sur le serveur OVH..."
ssh -o StrictHostKeyChecking=no $SSH_USER@$SERVER_IP << EOF
    cd /opt/ppm1
    
    echo "🛑 Arrêt des conteneurs..."
    docker-compose down || true
    
    echo "🧹 Nettoyage..."
    docker system prune -f
    
    echo "📥 Récupération de la nouvelle image..."
    docker pull $REGISTRY_URL/ppm1:$VERSION
    
    echo "▶️  Démarrage des services..."
    docker-compose up -d
    
    echo "⏳ Attente du démarrage..."
    sleep 30
    
    echo "🔍 Vérification de santé..."
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Déploiement réussi!"
        echo "🌐 Application disponible sur http://$SERVER_IP:8080"
    else
        echo "❌ Échec du health check"
        echo "📋 Logs de l'application:"
        docker-compose logs --tail=20 ppm1-api
        exit 1
    fi
EOF

echo "🎉 Déploiement terminé avec succès!"
echo "🌐 Application: http://$SERVER_IP:8080"
echo "❤️  Health: http://$SERVER_IP:8080/actuator/health"