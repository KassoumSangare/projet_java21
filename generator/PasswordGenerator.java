package com.passwordtool.generator;

import com.passwordtool.model.PasswordOptions;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

    // ── Jeux de caractères disponibles ────────────────────────────────────────
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";

    // Symboles courants, compatibles avec la plupart des politiques de sécurité.
    // On exclut délibérément les caractères ambigus (backtick, quotes simples)
    // qui posent des problèmes dans les shells.
    private static final String SYMBOLS   = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    // SecureRandom est thread-safe et réutilisable : on le crée une seule fois
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Génère un unique mot de passe selon les options fournies.
     *
     * @param options Configuration de génération (longueur, jeux de caractères)
     * @return Le mot de passe généré sous forme de String
     */
    public String generate(PasswordOptions options) {

        // ── Étape 1 : Construire l'alphabet complet à partir des jeux activés
        StringBuilder alphabetBuilder = new StringBuilder();
        List<Character> mandatoryChars = new ArrayList<>();

        if (options.useUppercase()) {
            alphabetBuilder.append(UPPERCASE);
            // Force au moins un caractère de ce jeu (garantie de couverture)
            mandatoryChars.add(randomCharFrom(UPPERCASE));
        }
        if (options.useLowercase()) {
            alphabetBuilder.append(LOWERCASE);
            mandatoryChars.add(randomCharFrom(LOWERCASE));
        }
        if (options.useDigits()) {
            alphabetBuilder.append(DIGITS);
            mandatoryChars.add(randomCharFrom(DIGITS));
        }
        if (options.useSymbols()) {
            alphabetBuilder.append(SYMBOLS);
            mandatoryChars.add(randomCharFrom(SYMBOLS));
        }

        String alphabet = alphabetBuilder.toString();
        int alphabetSize = alphabet.length();

        // ── Étape 2 : Remplir les positions restantes aléatoirement
        List<Character> passwordChars = new ArrayList<>(mandatoryChars);
        int remaining = options.length() - mandatoryChars.size();

        for (int i = 0; i < remaining; i++) {
            passwordChars.add(alphabet.charAt(secureRandom.nextInt(alphabetSize)));
        }

        // ── Étape 3 : Mélange aléatoire (Fisher-Yates via Collections.shuffle)
        // IMPORTANT : sans ce mélange, les caractères obligatoires seraient
        // toujours en début de chaîne, créant un biais détectable.
        Collections.shuffle(passwordChars, secureRandom);

        // ── Étape 4 : Convertir la liste de caractères en String
        StringBuilder password = new StringBuilder(options.length());
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    /**
     * Calcule l'entropie théorique d'un mot de passe en bits.
     *
     * Formule : H = L × log2(N)
     *   - L = longueur du mot de passe
     *   - N = taille de l'alphabet utilisé
     *
     * Cette entropie représente le nombre de bits d'information aléatoire :
     * un mot de passe de 72 bits nécessite 2^72 essais en force brute.
     *
     * @param options Options utilisées pour la génération
     * @return Entropie en bits (double pour la précision)
     */
    public double calculateEntropy(PasswordOptions options) {
        int alphabetSize = 0;
        if (options.useUppercase()) alphabetSize += UPPERCASE.length(); // 26
        if (options.useLowercase()) alphabetSize += LOWERCASE.length(); // 26
        if (options.useDigits())    alphabetSize += DIGITS.length();    // 10
        if (options.useSymbols())   alphabetSize += SYMBOLS.length();   // 28

        // log2(N) = ln(N) / ln(2)
        return options.length() * (Math.log(alphabetSize) / Math.log(2));
    }

    /**
     * Sélectionne un caractère aléatoire depuis un jeu de caractères donné.
     * Méthode utilitaire privée pour la garantie de couverture.
     */
    private char randomCharFrom(String charset) {
        return charset.charAt(secureRandom.nextInt(charset.length()));
    }
}