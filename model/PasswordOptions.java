package com.passwordtool.model;

/**
 * PasswordOptions — Record Java 21 représentant la configuration d'une génération.
 * Pourquoi un Record ?
 * - Immuabilité garantie par le compilateur (pas de setters possibles)
 * - equals(), hashCode() et toString() générés automatiquement
 * - Syntaxe compacte, idéale pour les structures de données "valeur"
 *
 * @param length      Longueur souhaitée du mot de passe (défaut : 16)
 * @param useUppercase Inclure des lettres majuscules (A-Z)
 * @param useLowercase Inclure des lettres minuscules (a-z)
 * @param useDigits    Inclure des chiffres (0-9)
 * @param useSymbols   Inclure des caractères spéciaux (!@#$%...)
 * @param burstCount   Nombre de mots de passe à générer en mode rafale (défaut : 1)
 * @param validatorUrl URL du micro-service de validation Docker
 * @param skipValidation Si true, génère sans contacter le validateur Docker
 */
public record PasswordOptions(
        int length,
        boolean useUppercase,
        boolean useLowercase,
        boolean useDigits,
        boolean useSymbols,
        int burstCount,
        String validatorUrl,
        boolean skipValidation
) {
    /**
     * Constructeur compact avec validation des invariants.
     * Java 21 permet de valider les paramètres directement dans le corps
     * du constructeur compact (sans répéter les assignations).
     */
    public PasswordOptions {
        // Invariant : longueur minimale raisonnable
        if (length < 4) {
            throw new IllegalArgumentException(
                "La longueur minimale d'un mot de passe est 4 caractères. Reçu : " + length
            );
        }
        // Invariant : longueur maximale raisonnable (éviter les abus mémoire)
        if (length > 512) {
            throw new IllegalArgumentException(
                "La longueur maximale est 512 caractères. Reçu : " + length
            );
        }
        // Invariant : au moins un jeu de caractères sélectionné
        if (!useUppercase && !useLowercase && !useDigits && !useSymbols) {
            throw new IllegalArgumentException(
                "Au moins un jeu de caractères doit être activé."
            );
        }
        // Invariant : le mode rafale doit être positif
        if (burstCount < 1) {
            throw new IllegalArgumentException(
                "Le mode rafale nécessite au moins 1 mot de passe. Reçu : " + burstCount
            );
        }
    }

    /**
     * Factory method : configuration par défaut (saine et utilisable immédiatement).
     * Longueur 16, tous les jeux de caractères, sans rafale.
     */
    public static PasswordOptions defaults() {
        return new PasswordOptions(
            16,                          // length
            true,                        // useUppercase
            true,                        // useLowercase
            true,                        // useDigits
            true,                        // useSymbols
            1,                           // burstCount
            "http://localhost:5000",     // validatorUrl
            false                        // skipValidation
        );
    }
}