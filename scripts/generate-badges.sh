#!/bin/bash

# Script pour générer des badges de couverture

JACOCO_CSV="target/site/jacoco/jacoco.csv"
BADGES_DIR="docs/badges"

# Créer le répertoire des badges s'il n'existe pas
mkdir -p "$BADGES_DIR"

if [ ! -f "$JACOCO_CSV" ]; then
    echo "❌ Fichier JaCoCo CSV introuvable. Exécutez d'abord les tests."
    echo "   Commande: ./mvnw test jacoco:report"
    exit 1
fi

echo "📊 Génération des badges de couverture..."

# Extraction des métriques de la dernière ligne (totaux)
LAST_LINE=$(tail -1 "$JACOCO_CSV")
IFS=',' read -ra FIELDS <<< "$LAST_LINE"

INSTRUCTION_MISSED=${FIELDS[3]}
INSTRUCTION_COVERED=${FIELDS[4]}
LINE_MISSED=${FIELDS[7]}
LINE_COVERED=${FIELDS[8]}
METHOD_MISSED=${FIELDS[11]}
METHOD_COVERED=${FIELDS[12]}

# Calculs
INSTRUCTION_TOTAL=$((INSTRUCTION_MISSED + INSTRUCTION_COVERED))
LINE_TOTAL=$((LINE_MISSED + LINE_COVERED))
METHOD_TOTAL=$((METHOD_MISSED + METHOD_COVERED))

if [ $INSTRUCTION_TOTAL -gt 0 ]; then
    INSTRUCTION_PERCENT=$(echo "scale=1; $INSTRUCTION_COVERED * 100 / $INSTRUCTION_TOTAL" | bc)
fi

if [ $LINE_TOTAL -gt 0 ]; then
    LINE_PERCENT=$(echo "scale=1; $LINE_COVERED * 100 / $LINE_TOTAL" | bc)
fi

if [ $METHOD_TOTAL -gt 0 ]; then
    METHOD_PERCENT=$(echo "scale=1; $METHOD_COVERED * 100 / $METHOD_TOTAL" | bc)
fi

# Fonction pour déterminer la couleur du badge
get_color() {
    local percent=$1
    if (( $(echo "$percent >= 80" | bc -l) )); then
        echo "brightgreen"
    elif (( $(echo "$percent >= 60" | bc -l) )); then
        echo "yellow"
    else
        echo "red"
    fi
}

# Génération des URL de badges shields.io
INSTRUCTION_COLOR=$(get_color $INSTRUCTION_PERCENT)
LINE_COLOR=$(get_color $LINE_PERCENT)
METHOD_COLOR=$(get_color $METHOD_PERCENT)

INSTRUCTION_BADGE="https://img.shields.io/badge/Instructions-${INSTRUCTION_PERCENT}%25-${INSTRUCTION_COLOR}"
LINE_BADGE="https://img.shields.io/badge/Lines-${LINE_PERCENT}%25-${LINE_COLOR}"
METHOD_BADGE="https://img.shields.io/badge/Methods-${METHOD_PERCENT}%25-${METHOD_COLOR}"

# Génération du fichier markdown avec les badges
cat > "$BADGES_DIR/coverage.md" << EOF
# Couverture de Tests

[![Instructions Coverage](${INSTRUCTION_BADGE})](target/site/jacoco/index.html)
[![Lines Coverage](${LINE_BADGE})](target/site/jacoco/index.html)
[![Methods Coverage](${METHOD_BADGE})](target/site/jacoco/index.html)

## Métriques Détaillées

| Métrique | Couverture | Détail |
|----------|------------|--------|
| Instructions | ${INSTRUCTION_PERCENT}% | ${INSTRUCTION_COVERED}/${INSTRUCTION_TOTAL} |
| Lignes | ${LINE_PERCENT}% | ${LINE_COVERED}/${LINE_TOTAL} |
| Méthodes | ${METHOD_PERCENT}% | ${METHOD_COVERED}/${METHOD_TOTAL} |

📊 **Rapport complet** : [target/site/jacoco/index.html](target/site/jacoco/index.html)

🕒 **Dernière mise à jour** : $(date)
EOF

# Génération des badges JSON pour intégration
cat > "$BADGES_DIR/coverage.json" << EOF
{
    "schemaVersion": 1,
    "label": "coverage",
    "message": "${INSTRUCTION_PERCENT}%",
    "color": "${INSTRUCTION_COLOR}",
    "namedLogo": "java",
    "logoColor": "white"
}
EOF

echo "✅ Badges générés avec succès !"
echo "   📁 Markdown: $BADGES_DIR/coverage.md"
echo "   📁 JSON: $BADGES_DIR/coverage.json"
echo ""
echo "📊 Résumé :"
echo "   Instructions: ${INSTRUCTION_PERCENT}%"
echo "   Lignes:       ${LINE_PERCENT}%"
echo "   Méthodes:     ${METHOD_PERCENT}%"