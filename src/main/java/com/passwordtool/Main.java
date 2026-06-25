package com.passwordtool;

import com.passwordtool.cli.CliParser;
import com.passwordtool.generator.PasswordGenerator;
import com.passwordtool.model.PasswordOptions;
import com.passwordtool.model.PasswordScore;
import com.passwordtool.model.ValidationResult;
import com.passwordtool.validator.DockerValidatorClient;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String SEPARATOR = "─".repeat(60);

    public static void main(String[] args) {
        CliParser parser = new CliParser();
        PasswordOptions options;

        try {
            options = parser.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Erreur d'argument : " + e.getMessage());
            System.err.println("   Utilisez --help pour voir les options disponibles.");
            System.exit(1);
            return;
        }

        PasswordGenerator generator = new PasswordGenerator();
        DockerValidatorClient validatorClient = new DockerValidatorClient(options.validatorUrl());

        if (!options.skipValidation()) {
            System.out.print("🔍 Connexion au validateur Docker (" + options.validatorUrl() + ")... ");
            if (validatorClient.isAvailable()) {
                System.out.println("✅ Connecté");
            } else {
                System.out.println("⚠️  Non disponible");
                System.out.println("   → Mode dégradé : entropie locale uniquement.");
                System.out.println("   → Pour désactiver cette vérification : --skip-validation");
                System.out.println();
            }
        }

        List<ValidationResult> results = new ArrayList<>();
        int burstCount = options.burstCount();
        boolean isBurst = burstCount > 1;

        if (isBurst) {
            System.out.printf("🚀 Mode rafale : génération de %d mots de passe...%n%n", burstCount);
        }

        for (int i = 0; i < burstCount; i++) {
            String password = generator.generate(options);
            double entropy = generator.calculateEntropy(options);

            PasswordScore score;
            if (options.skipValidation()) {
                score = PasswordScore.error("Validation désactivée (--skip-validation)");
            } else {
                score = validatorClient.validate(password);
            }

            results.add(new ValidationResult(password, score, entropy));
        }

        displayResults(results, options, isBurst);
    }

    private static void displayResults(List<ValidationResult> results,
                                       PasswordOptions options,
                                       boolean isBurst) {
        System.out.println(SEPARATOR);

        if (!isBurst) {
            ValidationResult result = results.getFirst();
            displaySingleResult(result, 0, false);
        } else {
            int acceptableCount = 0;
            String bestPassword = null;
            int bestScore = -1;

            for (int i = 0; i < results.size(); i++) {
                ValidationResult result = results.get(i);
                displaySingleResult(result, i + 1, true);

                if (result.score().score() > bestScore) {
                    bestScore = result.score().score();
                    bestPassword = result.password();
                }
                if (result.score().isAcceptable()) {
                    acceptableCount++;
                }
            }

            System.out.println(SEPARATOR);
            System.out.printf("📊 Résumé : %d/%d mots de passe acceptables (score ≥ 3)%n",
                acceptableCount, results.size());

            if (bestPassword != null && bestScore >= 3) {
                System.out.println("⭐ Meilleur mot de passe recommandé :");
                System.out.println("   " + bestPassword);
            } else if (bestScore < 3) {
                System.out.println("⚠️  Aucun mot de passe n'atteint le seuil recommandé.");
                System.out.println("   Conseil : augmentez la longueur avec --length 20");
            }
        }

        System.out.println(SEPARATOR);
    }

    private static void displaySingleResult(ValidationResult result, int index, boolean compact) {
        PasswordScore score = result.score();

        if (compact) {
            String scoreDisplay = score.score() >= 0
                ? score.strengthLabel()
                : "⚠️  Validateur indisponible";

            System.out.printf("[%2d] %s%n     Score : %s | Entropie : %s%n%n",
                index,
                result.password(),
                scoreDisplay,
                result.formattedEntropy()
            );
        } else {
            System.out.println();
            System.out.println("🔑 Mot de passe généré :");
            System.out.println("   " + result.password());
            System.out.println();
            System.out.println("📐 Entropie théorique : " + result.formattedEntropy());

            if (score.score() >= 0) {
                System.out.println("🛡️  Force (zxcvbn)    : " + score.strengthLabel() +
                    " (score " + score.score() + "/4)");
                System.out.println("⏱️  Temps de crack    : " + score.crackTimeDisplay());
                System.out.println("💬 Feedback           : " + score.feedback());

                if (!score.isAcceptable()) {
                    System.out.println();
                    System.out.println("⚠️  Ce mot de passe ne satisfait pas le seuil recommandé (score ≥ 3).");
                    System.out.println("   Conseil : essayez --length " + (result.password().length() + 4));
                } else {
                    System.out.println();
                    System.out.println("✅ Mot de passe conforme aux recommandations de sécurité.");
                }
            } else {
                System.out.println("⚠️  Validation Docker : " + score.feedback());
            }
        }
    }
}
