# Architecture du projet

Ce document explique la structure du projet pour aider un dev à comprendre comment les différents modules interagissent.

## Vue d’ensemble

Le projet est composé de deux parties principales :

1. Une application Java CLI
2. Un validateur Docker basé sur Python et zxcvbn

## Partie Java

L’application Java contient :
- `Main` : point d’entrée du programme
- `CliParser` : lecture des options de la ligne de commande
- `PasswordGenerator` : génération du mot de passe
- `DockerValidatorClient` : communication avec le service Docker

## Partie validation

Le service Docker reçoit le mot de passe, l’analyse avec zxcvbn et renvoie un score de sécurité.

## Flux de fonctionnement

```text
Utilisateur -> CLI Java -> Génération du mot de passe
                 -> Envoi au validateur Docker
                 -> Réception du score de sécurité
                 -> Affichage du résultat
```

## Pourquoi cette architecture ?

- La partie génération est indépendante du service de validation.
- Le service Docker peut être remplacé ou amélioré sans changer la logique Java.
- Le projet montre un exemple simple de séparation des responsabilités.
