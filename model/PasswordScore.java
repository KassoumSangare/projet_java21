package com.passwordtool.model;

/**
 * PasswordScore — Record Java 21 représentant la réponse du micro-service zxcvbn.
 *
 * Ce record est la désérialisation directe de la réponse JSON de l'API Flask.
 * L'immuabilité garantit qu'un score ne peut pas être altéré après réception.
 *
 * @param score            Score de force 0 à 4 (défini par zxcvbn)
 * @param feedback         Message humainement lisible sur la faiblesse détectée
 * @param crackTimeDisplay Temps de crack estimé, format lisible ("3 heures", "centuries")
 * @param crackTimeSeconds Temps de crack en secondes (pour comparaisons programmatiques)
 * @param guesses          Nombre d'essais estimés par un attaquant
 * @param passwordLength   Longueur du mot de passe analysé
 */
public record PasswordScore(
        int score,
        String feedback,
        String crackTimeDisplay,
        double crackTimeSeconds,
        long guesses,
        int passwordLength
) {
    /**
     * Retourne un label textuel correspondant au score numérique.
     * Exemple d'utilisation de Pattern Matching switch (Java 21).
     *
     * @return Label de force en français
     */
    public String strengthLabel() {
        return switch (score) {
            case 0 -> "🔴 Très faible";
            case 1 -> "🟠 Faible";
            case 2 -> "🟡 Moyen";
            case 3 -> "🟢 Fort";
            case 4 -> "🟢 Très fort";
            default -> "❓ Inconnu";
        };
    }

    /**
     * Retourne true si le mot de passe est jugé acceptable (score >= 3).
     * Seuil aligné sur les recommandations OWASP pour les mots de passe forts.
     */
    public boolean isAcceptable() {
        return score >= 3;
    }

    /**
     * Factory method pour le cas où le validateur Docker est inaccessible.
     * Permet un mode dégradé gracieux sans planter l'application.
     */
    public static PasswordScore unavailable() {
        return new PasswordScore(
            -1,
            "Validateur Docker inaccessible — score non disponible",
            "N/A",
            -1,
            -1,
            0
        );
    }

    /**
     * Factory method pour représenter une erreur de validation.
     *
     * @param reason Message d'erreur explicatif
     */
    public static PasswordScore error(String reason) {
        return new PasswordScore(-1, reason, "N/A", -1, -1, 0);
    }
}