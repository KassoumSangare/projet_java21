# Guide d’utilisation

Ce document explique comment utiliser l’outil pas à pas.

## Prérequis

- Java 21
- Maven
- Docker

## 1. Démarrer le validateur Docker

Depuis la racine du projet :

```bash
bash scripts/start-validator.sh
```

Ce script construit l’image Docker et lance le conteneur sur le port 5000.

## 2. Exécuter l’application

Construire le projet :

```bash
mvn package
```

Puis lancer l’outil :

```bash
java -jar target/password-tool-1.0-SNAPSHOT.jar
```

## 3. Options disponibles

Quelques exemples :

```bash
java -jar target/password-tool-1.0-SNAPSHOT.jar --length 20
java -jar target/password-tool-1.0-SNAPSHOT.jar --burst 3 --length 16
java -jar target/password-tool-1.0-SNAPSHOT.jar --skip-validation
```

## 4. Comprendre les résultats

- Le mot de passe généré est affiché.
- L’entropie théorique donne une estimation de la sécurité.
- Le score fourni par le validateur indique si le mot de passe est fort.

## 5. En cas de problème

Si le validateur ne répond pas, vérifier :
- que Docker est bien démarré,
- que le conteneur tourne sur le port 5000,
- que l’URL du validateur est correcte.
