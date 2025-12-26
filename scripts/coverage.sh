#!/bin/bash

# Script pour analyser la couverture de tests avec JaCoCo

echo "🔄 Génération du rapport de couverture de tests..."

# Nettoyage et exécution des tests avec JaCoCo
./mvnw clean test jacoco:report

if [ $? -eq 0 ]; then
    echo "✅ Rapport généré avec succès !"
    
    # Affichage du résumé depuis le fichier CSV
    echo ""
    echo "📊 Résumé de la couverture :"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    # Calcul de la couverture globale
    if [ -f "target/site/jacoco/jacoco.csv" ]; then
        # Extraction des totaux (dernière ligne du CSV contient les totaux)
        tail -1 target/site/jacoco/jacoco.csv | awk -F',' '
        {
            instruction_total = $4 + $5
            instruction_covered = $5
            line_total = $8 + $9
            line_covered = $9
            method_total = $12 + $13
            method_covered = $13
            
            if (instruction_total > 0) {
                instruction_percent = (instruction_covered / instruction_total) * 100
                printf "📋 Instructions : %.1f%% (%d/%d)\n", instruction_percent, instruction_covered, instruction_total
            }
            
            if (line_total > 0) {
                line_percent = (line_covered / line_total) * 100
                printf "📏 Lignes      : %.1f%% (%d/%d)\n", line_percent, line_covered, line_total
            }
            
            if (method_total > 0) {
                method_percent = (method_covered / method_total) * 100
                printf "🔧 Méthodes    : %.1f%% (%d/%d)\n", method_percent, method_covered, method_total
            }
        }'
    fi
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📁 Rapport détaillé disponible : target/site/jacoco/index.html"
    echo "🌐 Pour ouvrir le rapport : open target/site/jacoco/index.html"
    echo ""
    
    # Classes avec faible couverture (< 80%)
    echo "⚠️  Classes nécessitant plus de tests :"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    awk -F',' 'NR > 1 && $4 + $5 > 0 {
        instruction_total = $4 + $5
        instruction_covered = $5
        if (instruction_total > 0) {
            coverage = (instruction_covered / instruction_total) * 100
            if (coverage < 80) {
                printf "🔴 %s.%s : %.1f%% de couverture\n", $2, $3, coverage
            }
        }
    }' target/site/jacoco/jacoco.csv
    
else
    echo "❌ Erreur lors de la génération du rapport"
    exit 1
fi