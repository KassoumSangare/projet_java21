# Password Tool

Ce projet est une application Java qui génère des mots de passe aléatoires et peut les valider avec un service Docker basé sur zxcvbn.

## Objectif

L’objectif est de montrer comment construire un petit outil CLI en Java 21 avec :
- une génération de mots de passe sécurisés,
- une validation de la force du mot de passe,
- un micro-service Docker pour analyser la qualité du mot de passe.

## Structure du projet

- `src/main/java/com/passwordtool/` : code Java principal
- `docker/` : service de validation Python/Flask
- `scripts/` : scripts d’aide pour démarrer le validateur
- `pom.xml` : configuration Maven du projet

## Comment ça fonctionne

1. L’application génère un mot de passe selon les options choisies.
2. Elle calcule une entropie théorique locale.
3. Elle envoie le mot de passe à un service Docker pour obtenir un score de sécurité.
4. Elle affiche le résultat à l’utilisateur.

## Exemple d’utilisation

```bash
java -jar target/password-tool-1.0-SNAPSHOT.jar --length 16
```

Pour plus d’informations sur les options, utiliser :

```bash
java -jar target/password-tool-1.0-SNAPSHOT.jar --help
```
