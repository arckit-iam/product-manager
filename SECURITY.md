# Politique de sécurité

## Versions prises en charge

Le projet est en développement actif : seule la dernière version publiée reçoit des correctifs de sécurité.

## Signaler une vulnérabilité

Ne pas ouvrir d'*issue* publique pour une faille de sécurité.

Utiliser le signalement privé de GitHub : onglet **Security** du dépôt, puis **Report a vulnerability** (*private vulnerability reporting*), ou écrire à [etiennsalle@gmail.com](mailto:etiennsalle@gmail.com). Indiquer :

- la version ou le commit concerné ;
- une description de la faille et de son impact ;
- les étapes pour la reproduire.

Un accusé de réception est envoyé dès que possible, puis le correctif et sa publication sont coordonnés avec la personne ayant signalé la faille.

## Bonnes pratiques de déploiement

- Ne jamais versionner de secrets (mots de passe, jetons, clés privées, keytabs) : utiliser des secrets Kubernetes ou des variables d'environnement.
- Les exemples du dépôt utilisent des valeurs fictives (domaine `example`) : les remplacer par vos propres valeurs.
