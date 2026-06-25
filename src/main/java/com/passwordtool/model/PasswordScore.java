package com.passwordtool.model;

public record PasswordScore(
        int score,
        String feedback,
        String crackTimeDisplay,
        double crackTimeSeconds,
        long guesses,
        int passwordLength
) {
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

    public boolean isAcceptable() {
        return score >= 3;
    }

    public static PasswordScore unavailable() {
        return new PasswordScore(-1, "Validateur Docker inaccessible — score non disponible", "N/A", -1, -1, 0);
    }

    public static PasswordScore error(String reason) {
        return new PasswordScore(-1, reason, "N/A", -1, -1, 0);
    }
}
