# Script simple pour afficher la couverture de tests

Write-Host "=== COUVERTURE DE TESTS ===" -ForegroundColor Green

# Génération rapide du rapport JaCoCo
Write-Host "Calcul de la couverture..." -ForegroundColor Yellow
& ./mvnw -q jacoco:report

if ($LASTEXITCODE -eq 0 -and (Test-Path "target\site\jacoco\jacoco.csv")) {
    
    # Lecture des métriques globales (dernière ligne du CSV)
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
        
        Write-Host ""
        Write-Host "RESULTATS GLOBAUX:" -ForegroundColor Cyan
        Write-Host "=================" -ForegroundColor Cyan
        
        if ($instructionTotal -gt 0) {
            $instructionPercent = [math]::Round(($instructionCovered / $instructionTotal) * 100, 1)
            $color = if ($instructionPercent -ge 80) { "Green" } elseif ($instructionPercent -ge 60) { "Yellow" } else { "Red" }
            Write-Host "Instructions: $instructionPercent% ($instructionCovered/$instructionTotal)" -ForegroundColor $color
        }
        
        if ($lineTotal -gt 0) {
            $linePercent = [math]::Round(($lineCovered / $lineTotal) * 100, 1)
            $color = if ($linePercent -ge 80) { "Green" } elseif ($linePercent -ge 60) { "Yellow" } else { "Red" }
            Write-Host "Lignes:       $linePercent% ($lineCovered/$lineTotal)" -ForegroundColor $color
        }
        
        if ($methodTotal -gt 0) {
            $methodPercent = [math]::Round(($methodCovered / $methodTotal) * 100, 1)
            $color = if ($methodPercent -ge 80) { "Green" } elseif ($methodPercent -ge 60) { "Yellow" } else { "Red" }
            Write-Host "Methodes:     $methodPercent% ($methodCovered/$methodTotal)" -ForegroundColor $color
        }
        
        Write-Host ""
        Write-Host "Classes a ameliorer (< 80%):" -ForegroundColor Yellow
        Write-Host "============================" -ForegroundColor Yellow
        
        $lowCoverageCount = 0
        $csvLines = Get-Content "target\site\jacoco\jacoco.csv" | Select-Object -Skip 1 | Select-Object -SkipLast 1
        foreach ($line in $csvLines) {
            $fields = $line -split ','
            if ($fields.Length -ge 5) {
                $class = $fields[2]
                $instructionMissed = [int]$fields[3]
                $instructionCovered = [int]$fields[4]
                
                $total = $instructionMissed + $instructionCovered
                if ($total -gt 10) {  # Ignorer les très petites classes
                    $coverage = [math]::Round(($instructionCovered / $total) * 100, 1)
                    if ($coverage -lt 80) {
                        Write-Host "- $class : $coverage%" -ForegroundColor Red
                        $lowCoverageCount++
                    }
                }
            }
        }
        
        if ($lowCoverageCount -eq 0) {
            Write-Host "Toutes les classes principales ont une bonne couverture!" -ForegroundColor Green
        }
        
        Write-Host ""
        Write-Host "Rapport HTML: target\site\jacoco\index.html" -ForegroundColor Blue
        Write-Host "Pour ouvrir: start target\site\jacoco\index.html" -ForegroundColor Blue
    }
} else {
    Write-Host "Erreur lors de la generation du rapport" -ForegroundColor Red
}