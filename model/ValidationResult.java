package com.passwordtool.model;

/**
 * ValidationResult — Record Java 21 représentant le résultat complet
 * d'une opération de génération + validation.
 *
 * Regroupe le mot de passe généré, son score de sécurité et l'entropie
 * calculée localement (indépendante du validateur Docker).
 *
 * @param password Le mot de passe généré
 * @param score    L'analyse de sécurité retournée par zxcvbn (ou mode dégradé)
 * @param entropy  Entropie calculée localement en bits (log2 du nombre de combinaisons)
 */
public record ValidationResult(
        String password,
        PasswordScore score,
        double entropy
) {
    /**
     * Retourne une chaîne formatée de l'entropie avec un label de force.
    
     *
     * @return Chaîne formatée, ex. "72.3 bits (Fort)"
     */
    public String formattedEntropy() {
        String level = switch ((int) entropy) {
            case int e when e < 28  -> "Très faible";
            case int e when e < 36  -> "Faible";
            case int e when e < 60  -> "Raisonnable";
            case int e when e < 128 -> "Fort";
            default                  -> "Très fort (cryptographique)";
        };
        return String.format("%.1f bits (%s)", entropy, level);
    }
}