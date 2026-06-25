package com.passwordtool.model;

public record ValidationResult(
        String password,
        PasswordScore score,
        double entropy
) {
    public String formattedEntropy() {
        String level = switch ((int) entropy) {
            case int e when e < 28 -> "Très faible";
            case int e when e < 36 -> "Faible";
            case int e when e < 60 -> "Raisonnable";
            case int e when e < 128 -> "Fort";
            default -> "Très fort (cryptographique)";
        };
        return String.format("%.1f bits (%s)", entropy, level);
    }
}
