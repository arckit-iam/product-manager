# .copilot-instructions.md

## Contexte du projet
Ce projet suit l'architecture hexagonale (Hexagonal Architecture / Ports & Adapters). Le code est organisé pour séparer la logique métier (domaine) des détails d'implémentation (infrastructure, frameworks, etc.).

## Directives pour GitHub Copilot

### Génération de code
- **Respecter l'architecture hexagonale** :
  - Place la logique métier dans le domaine (ex : `src/main/java/secop/domain/`).
  - Les interfaces (ports) doivent être définies dans le domaine.
  - Les implémentations concrètes (adapters) doivent être placées dans l'infrastructure (ex : `src/main/java/secop/infrastructure/`).
- **Aucune dépendance du domaine vers l'infrastructure** :
  - Le domaine ne doit jamais dépendre d'une classe d'infrastructure ou de framework.
- **Utiliser l'injection de dépendances** pour relier les ports et les adapters.
- **Favoriser les tests unitaires** sur le domaine, et les tests d'intégration sur les adapters.

### Nommage
- Les ports (interfaces) : `XxxPort` ou `XxxRepository`.
- Les adapters : `XxxAdapter`, `XxxRepositoryImpl`, etc.
- Les services métier : `XxxService`.

### Bonnes pratiques
- Privilégier la clarté et la séparation des responsabilités.
- Documenter les interfaces du domaine.
- Éviter les annotations/frameworks dans le domaine (pas de `@Service`, `@Repository`, etc.).
- Les DTO et mappers doivent être séparés du domaine.

### Exemples de structure
```
src/main/java/secop/
  domain/
    service/
    model/
    port/
  infrastructure/
    adapter/
    repository/
  application/
    service/
```

### Tests
- Les tests unitaires du domaine dans `src/test/java/secop/domain/`.
- Les tests d'intégration dans `src/test/java/secop/infrastructure/`.

---

**Résumé** : Génère du code qui respecte strictement l'architecture hexagonale, en séparant le domaine, les ports, les adapters et l'infrastructure. Ne mélange jamais la logique métier et la technique.