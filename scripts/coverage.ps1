# Script PowerShell pour analyser la couverture de tests avec JaCoCo

Write-Host "Génération du rapport de couverture de tests..." -ForegroundColor Yellow

# Nettoyage et exécution des tests avec JaCoCo
& ./mvnw clean test jacoco:report

if ($LASTEXITCODE -eq 0) {
    Write-Host "Rapport généré avec succès !" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "Résumé de la couverture :" -ForegroundColor Cyan
    Write-Host ("=" * 50) -ForegroundColor Gray
    
    # Calcul de la couverture globale
    if (Test-Path "target\site\jacoco\jacoco.csv") {
        $csvContent = Get-Content "target\site\jacoco\jacoco.csv"
        $lastLine = $csvContent[-1]
        $fields = $lastLine -split ','
        
        if ($fields.Length -ge 13) {
            $instructionMissed = [int]$fields[3]
            $instructionCovered = [int]$fields[4]
            $lineMissed = [int]$fields[7]
            $lineCovered = [int]$fields[8]
            $methodMissed = [int]$fields[11]
            $methodCovered = [int]$fields[12]
            
            $instructionTotal = $instructionMissed + $instructionCovered
            $lineTotal = $lineMissed + $lineCovered
            $methodTotal = $methodMissed + $methodCovered
            
            if ($instructionTotal -gt 0) {
                $instructionPercent = ($instructionCovered / $instructionTotal) * 100
                Write-Host "Instructions : $([math]::Round($instructionPercent, 1))% ($instructionCovered/$instructionTotal)" -ForegroundColor White
            }
            
            if ($lineTotal -gt 0) {
                $linePercent = ($lineCovered / $lineTotal) * 100
                Write-Host "Lignes      : $([math]::Round($linePercent, 1))% ($lineCovered/$lineTotal)" -ForegroundColor White
            }
            
            if ($methodTotal -gt 0) {
                $methodPercent = ($methodCovered / $methodTotal) * 100
                Write-Host "Méthodes    : $([math]::Round($methodPercent, 1))% ($methodCovered/$methodTotal)" -ForegroundColor White
            }
        }
    }
    
    Write-Host ("=" * 50) -ForegroundColor Gray
    Write-Host ""
    Write-Host "Rapport détaillé disponible : target\site\jacoco\index.html" -ForegroundColor Green
    Write-Host "Pour ouvrir le rapport : start target\site\jacoco\index.html" -ForegroundColor Green
    Write-Host ""
    
    # Classes avec faible couverture (< 80%)
    Write-Host "Classes nécessitant plus de tests :" -ForegroundColor Yellow
    Write-Host ("=" * 40) -ForegroundColor Gray
    
    $csvLines = Get-Content "target\site\jacoco\jacoco.csv" | Select-Object -Skip 1
    foreach ($line in $csvLines) {
        $fields = $line -split ','
        if ($fields.Length -ge 5) {
            $package = $fields[1]
            $class = $fields[2]
            $instructionMissed = [int]$fields[3]
            $instructionCovered = [int]$fields[4]
            
            $total = $instructionMissed + $instructionCovered
            if ($total -gt 0) {
                $coverage = ($instructionCovered / $total) * 100
                if ($coverage -lt 80) {
                    Write-Host "FAIBLE: $package.$class : $([math]::Round($coverage, 1))% de couverture" -ForegroundColor Red
                }
            }
        }
    }
    
    Write-Host ""
    Write-Host "Conseils :" -ForegroundColor Cyan
    Write-Host "   • Visez au moins 80% de couverture pour les classes métier" -ForegroundColor White
    Write-Host "   • Les mappers générés automatiquement peuvent avoir une couverture plus faible" -ForegroundColor White
    Write-Host "   • Concentrez-vous sur les classes de services et du domaine" -ForegroundColor White
    
} else {
    Write-Host "Erreur lors de la génération du rapport" -ForegroundColor Red
    exit 1
}