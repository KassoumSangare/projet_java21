package com.passwordtool;

import com.passwordtool.cli.CliParser;
import com.passwordtool.generator.PasswordGenerator;
import com.passwordtool.model.PasswordOptions;
import com.passwordtool.model.PasswordScore;
import com.passwordtool.model.ValidationResult;
import com.passwordtool.validator.DockerValidatorClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Main — Point d'entrée de l'outil CLI de génération de mots de passe.
 *
 * Résultat : tous les mots de passe générés sont sous le seuil de sécurité recommandé (score < 3) :
 *   - Le mot de passe est affiché
 */
public class Main {

    // Séparateur visuel réutilisé dans l'affichage
    private static final String SEPARATOR = "─".repeat(60);

    public static void main(String[] args) {

        // ── Étape 1 : Parser les arguments de la ligne de commande ────────────
        CliParser parser = new CliParser();
        PasswordOptions options;

        try {
            options = parser.parse(args);
        } catch (IllegalArgumentException e) {
            // Erreur de l'utilisateur : on affiche le message et on quitte proprement
            System.err.println("❌ Erreur d'argument : " + e.getMessage());
            System.err.println("   Utilisez --help pour voir les options disponibles.");
            System.exit(1);
            return; // Nécessaire pour le compilateur (unreachable mais requis)
        }

        // ── Étape 2 : Initialisation des composants ───────────────────────────
        PasswordGenerator generator = new PasswordGenerator();
        DockerValidatorClient validatorClient = new DockerValidatorClient(options.validatorUrl());

        // ── Étape 3 : Vérification de la disponibilité du validateur ──────────
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

        // ── Étape 4 : Génération et validation ────────────────────────────────
        List<ValidationResult> results = new ArrayList<>();
        int burstCount = options.burstCount();
        boolean isBurst = burstCount > 1;

        if (isBurst) {
            System.out.printf("🚀 Mode rafale : génération de %d mots de passe...%n%n", burstCount);
        }

        for (int i = 0; i < burstCount; i++) {
            // Génération du mot de passe
            String password = generator.generate(options);
            double entropy  = generator.calculateEntropy(options);

            // Validation via Docker (ou mode dégradé si indisponible/skip)
            PasswordScore score;
            if (options.skipValidation()) {
                score = PasswordScore.error("Validation désactivée (--skip-validation)");
            } else {
                score = validatorClient.validate(password);
            }

            results.add(new ValidationResult(password, score, entropy));
        }

        // ── Étape 5 : Affichage des résultats ─────────────────────────────────
        displayResults(results, options, isBurst);
    }

    /**
     * Affiche les résultats de manière formatée et lisible.
     * Adapte l'affichage selon le mode (simple vs rafale).
     */
    private static void displayResults(List<ValidationResult> results,
                                        PasswordOptions options,
                                        boolean isBurst) {
        System.out.println(SEPARATOR);

        if (!isBurst) {
            // ── Mode simple : affichage détaillé ──────────────────────────────
            ValidationResult result = results.getFirst();
            displaySingleResult(result, 0, false);

        } else {
            // ── Mode rafale : tableau compact + résumé statistique ────────────
            int acceptableCount = 0;
            String bestPassword = null;
            int bestScore = -1;

            for (int i = 0; i < results.size(); i++) {
                ValidationResult result = results.get(i);
                displaySingleResult(result, i + 1, true);

                // Suivi du meilleur mot de passe (pour recommandation finale)
                if (result.score().score() > bestScore) {
                    bestScore = result.score().score();
                    bestPassword = result.password();
                }
                if (result.score().isAcceptable()) {
                    acceptableCount++;
                }
            }

            // Résumé du mode rafale
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

    /**
     * Affiche un résultat individuel (mot de passe + score + entropie).
     *
     * @param result  Le résultat à afficher
     * @param index   Numéro dans la rafale (0 = mode simple)
     * @param compact Si true, affichage condensé (mode rafale)
     */
    private static void displaySingleResult(ValidationResult result, int index, boolean compact) {
        PasswordScore score = result.score();

        if (compact) {
            // Format compact pour le mode rafale
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
            // Format détaillé pour le mode simple
            System.out.println();
            System.out.println("🔑 Mot de passe généré :");
            System.out.println("   " + result.password());
            System.out.println();
            System.out.println("📐 Entropie théorique : " + result.formattedEntropy());

            if (score.score() >= 0) {
                // Le validateur a répondu avec succès
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
                // Mode dégradé : le validateur n'était pas disponible
                System.out.println("⚠️  Validation Docker : " + score.feedback());
            }
        }
    }
}