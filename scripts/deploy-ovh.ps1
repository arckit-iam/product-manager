# Script de déploiement OVH pour Windows PowerShell avec GitHub Container Registry
# Usage: .\deploy-ovh.ps1 [-Version "1.0.0"]

param(
    [string]$Version = "latest"
)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Déploiement PPM1 version $Version sur OVH avec GitHub Container Registry..." -ForegroundColor Green

# Détecter automatiquement le propriétaire GitHub
try {
    $gitRemote = git remote get-url origin
    $GITHUB_OWNER = ($gitRemote -replace '.*github\.com[/:]([^/]*)/.*', '$1')
} catch {
    $GITHUB_OWNER = $env:GITHUB_REPOSITORY_OWNER
}

$REGISTRY_URL = "ghcr.io"
$SERVER_IP = $env:OVH_SERVER_IP
$SSH_USER = $env:OVH_SSH_USER
$GITHUB_TOKEN = $env:GITHUB_TOKEN

if (-not $GITHUB_OWNER -or -not $SERVER_IP -or -not $SSH_USER) {
    Write-Host "❌ Variables manquantes. Définissez:" -ForegroundColor Red
    Write-Host "   `$env:OVH_SERVER_IP = '192.168.1.100'"
    Write-Host "   `$env:OVH_SSH_USER = 'ubuntu'"
    Write-Host "   `$env:GITHUB_TOKEN = 'your-github-token'"
    Write-Host "   GITHUB_OWNER sera détecté automatiquement"
    exit 1
}

try {
    Write-Host "📦 Build de l'image Docker..." -ForegroundColor Blue
    docker build -t "pmp1:$Version" .
    
    Write-Host "🏷️  Tag pour GitHub Container Registry..." -ForegroundColor Blue
    docker tag "ppm1:$Version" "$REGISTRY_URL/$GITHUB_OWNER/ppm1:$Version"
    docker tag "ppm1:$Version" "$REGISTRY_URL/$GITHUB_OWNER/ppm1:latest"
    
    Write-Host "📤 Push vers GitHub Container Registry..." -ForegroundColor Blue
    echo $GITHUB_TOKEN | docker login $REGISTRY_URL -u $GITHUB_OWNER --password-stdin
    docker push "$REGISTRY_URL/$GITHUB_OWNER/ppm1:$Version"
    docker push "$REGISTRY_URL/$GITHUB_OWNER/ppm1:latest"
    
    Write-Host "🔄 Déploiement sur le serveur OVH..." -ForegroundColor Blue
    
    # Créer le script SSH
    $sshScript = @"
cd /opt/ppm1
echo "🛑 Arrêt des conteneurs..."
docker-compose down || true
echo "🧹 Nettoyage..."
docker system prune -f
echo "📥 Récupération de la nouvelle image..."
docker pull $REGISTRY_URL/ppm1:$Version
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
"@
    
    # Exécuter le script SSH
    $sshScript | ssh -o StrictHostKeyChecking=no "$SSH_USER@$SERVER_IP" 'bash -s'
    
    Write-Host "🎉 Déploiement terminé avec succès!" -ForegroundColor Green
    Write-Host "🌐 Application: http://$SERVER_IP:8080" -ForegroundColor Cyan
    Write-Host "❤️  Health: http://$SERVER_IP:8080/actuator/health" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Erreur lors du déploiement: $_" -ForegroundColor Red
    exit 1
}