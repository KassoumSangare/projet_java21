package com.passwordtool.model;

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
    public PasswordOptions {
        if (length < 4) {
            throw new IllegalArgumentException(
                "La longueur minimale d'un mot de passe est 4 caractères. Reçu : " + length
            );
        }
        if (length > 512) {
            throw new IllegalArgumentException(
                "La longueur maximale est 512 caractères. Reçu : " + length
            );
        }
        if (!useUppercase && !useLowercase && !useDigits && !useSymbols) {
            throw new IllegalArgumentException("Au moins un jeu de caractères doit être activé.");
        }
        if (burstCount < 1) {
            throw new IllegalArgumentException(
                "Le mode rafale nécessite au moins 1 mot de passe. Reçu : " + burstCount
            );
        }
    }

    public static PasswordOptions defaults() {
        return new PasswordOptions(16, true, true, true, true, 1, "http://localhost:5000", false);
    }
}
