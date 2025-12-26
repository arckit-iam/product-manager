# Guide de Couverture de Tests - Projet PPM1

## 📊 Vue d'ensemble

Ce projet utilise **JaCoCo** (Java Code Coverage) pour mesurer et analyser la couverture de tests. JaCoCo est l'outil standard pour Java qui fournit des métriques précises sur l'exécution du code lors des tests.

## 🛠️ Configuration

### Maven Plugin JaCoCo

Le plugin JaCoCo est configuré dans `pom.xml` pour :

- **Instrumenter automatiquement** le code lors des tests
- **Générer des rapports** en formats HTML, XML et CSV
- **Vérifier les seuils** de couverture minimum
- **Intégrer avec les workflows CI/CD**

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>CLASS</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 🚀 Utilisation

### 1. Commandes Maven

```bash
# Génération complète du rapport
./mvnw clean test jacoco:report

# Génération rapide (sans clean)
./mvnw jacoco:report

# Vérification des seuils de couverture
./mvnw jacoco:check

# Tests avec couverture en une commande
./mvnw clean test
```

### 2. Scripts Personnalisés

Le projet inclut des scripts pour faciliter l'analyse :

#### Script PowerShell (Windows)
```powershell
# Rapport complet avec analyse
.\scripts\coverage.ps1

# Rapport simple et rapide
.\scripts\coverage-simple.ps1
```

#### Script Bash (Linux/macOS)
```bash
# Rapport complet avec analyse
./scripts/coverage.sh
```

### 3. Ouverture du Rapport HTML

```bash
# Windows
start target\site\jacoco\index.html

# Linux/macOS
open target/site/jacoco/index.html

# Ou directement depuis le navigateur
file:///path/to/project/target/site/jacoco/index.html
```

## 📈 Métriques de Couverture

### Types de Couverture

| Métrique | Description | Objectif |
|----------|-------------|----------|
| **Instructions** | Bytecode Java exécuté | ≥ 80% |
| **Lignes** | Lignes de code source executées | ≥ 80% |
| **Branches** | Conditions (if/switch) testées | ≥ 70% |
| **Méthodes** | Méthodes appelées | ≥ 90% |
| **Classes** | Classes instanciées | ≥ 80% |

### Interprétation des Couleurs

- 🟢 **Vert** : Couverture ≥ 80% (Excellent)
- 🟡 **Jaune** : Couverture 60-79% (Bon, à améliorer)
- 🔴 **Rouge** : Couverture < 60% (Insuffisant, priorité haute)

## 📋 Formats de Rapports

### 1. Rapport HTML
- **Localisation** : `target/site/jacoco/index.html`
- **Avantages** : Interface interactive, détails ligne par ligne
- **Usage** : Analyse détaillée en développement

### 2. Rapport CSV  
- **Localisation** : `target/site/jacoco/jacoco.csv`
- **Avantages** : Données structurées pour scripts
- **Usage** : Intégration CI/CD, métriques automatisées

### 3. Rapport XML
- **Localisation** : `target/site/jacoco/jacoco.xml`
- **Avantages** : Format standard, parsing facile
- **Usage** : Intégration outils de qualité (SonarQube, etc.)

## 🎯 Bonnes Pratiques

### Classes à Prioriser

1. **Classes métier** (`domain.service.*`) → Couverture ≥ 90%
2. **Logique applicative** (`adapters.in.*`) → Couverture ≥ 85%
3. **Controllers REST** (`adapters.out.*`) → Couverture ≥ 80%
4. **Entités/DTOs** → Couverture ≥ 70%

### Classes à Exclure

```xml
<!-- Configuration pour exclure certaines classes -->
<configuration>
    <excludes>
        <exclude>**/ProfileApplication.class</exclude>
        <exclude>**/*MapperImpl.class</exclude>
        <exclude>**/dto/*.class</exclude>
        <exclude>**/entity/*.class</exclude>
    </excludes>
</configuration>
```

### Types de Tests

1. **Tests Unitaires** → Coverage des méthodes individuelles
2. **Tests d'Intégration** → Coverage des workflows complets  
3. **Tests de Repository** → Coverage de l'accès aux données
4. **Tests de Controller** → Coverage des endpoints REST

## 🔧 Dépannage

### Problèmes Courants

#### "No execution data file found"
```bash
# Solution : Réexécuter les tests avec l'agent JaCoCo
./mvnw clean test jacoco:report
```

#### "Classes not instrumented"
```bash
# Vérifier que prepare-agent s'exécute avant compile
./mvnw clean compile test
```

#### "Coverage trop faible"
```bash
# Identifier les classes non couvertes
./mvnw jacoco:check -Djacoco.minimum=0.1
```

### Configuration Debug

```bash
# Activer les logs JaCoCo détaillés
./mvnw test -Djacoco.debug=true
```

## 📊 État Actuel du Projet

```
Dernière Mesure : 26/12/2025
=====================================
Instructions : 51.4% (460/895)
Lignes       : 52.8% (195/369) 
Méthodes     : 61.5% (75/122)
```

### Classes Nécessitant Attention

- `ProductEntityMapperImpl` : 43.8%
- `ProfileDtoMapperImpl` : 3.1%  
- `ProductDtoMapperImpl` : 1.5%
- `RestProductController` : 13.8%

## 🚀 Plan d'Amélioration

1. **Phase 1** : Couverture des services métier → 90%
2. **Phase 2** : Couverture des controllers REST → 80%
3. **Phase 3** : Couverture des providers JPA → 85%
4. **Phase 4** : Tests d'intégration bout-en-bout

## 🔗 Intégration CI/CD

### GitHub Actions

```yaml
- name: Test Coverage
  run: ./mvnw clean test jacoco:report

- name: Upload Coverage Reports
  uses: codecov/codecov-action@v3
  with:
    file: target/site/jacoco/jacoco.xml
```

### Seuils de Qualité

```yaml
- name: Check Coverage Thresholds
  run: ./mvnw jacoco:check
```

---

**📝 Note** : Ce guide est maintenu automatiquement. Pour toute question sur la couverture de tests, consultez les rapports générés ou contactez l'équipe de développement.