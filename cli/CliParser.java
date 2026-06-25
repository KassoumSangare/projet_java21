package com.passwordtool.cli;

import com.passwordtool.model.PasswordOptions;


public class CliParser {

    /**
     * Parse le tableau args[] passé à main() et retourne un PasswordOptions.
     *
     * @param args Arguments bruts de la ligne de commande
     * @return Options configurées selon les arguments
     * @throws IllegalArgumentException si un argument est invalide ou manquant
     */
    public PasswordOptions parse(String[] args) {

        // Valeurs par défaut (reprises depuis PasswordOptions.defaults())
        int length           = 16;
        boolean useUppercase = true;
        boolean useLowercase = true;
        boolean useDigits    = true;
        boolean useSymbols   = true;
        int burstCount       = 1;
        String validatorUrl  = "http://localhost:5000";
        boolean skipValidation = false;

        // ── Parsing itératif des arguments ────────────────────────────────────
        // On itère avec un index pour accéder aux valeurs des arguments "à valeur"
        // (ex: --length 20 nécessite de lire le token suivant)
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {

                case "--help", "-h" -> {
                    printHelp();
                    // Convention Unix : exit 0 pour --help (pas une erreur)
                    System.exit(0);
                }

                case "--length", "-l" -> {
                    i = requireNextArg(args, i, "--length");
                    length = parsePositiveInt(args[i], "--length");
                }

                case "--no-upper"   -> useUppercase = false;
                case "--no-lower"   -> useLowercase = false;
                case "--no-digits"  -> useDigits    = false;
                case "--no-symbols" -> useSymbols   = false;

                case "--burst", "-b" -> {
                    i = requireNextArg(args, i, "--burst");
                    burstCount = parsePositiveInt(args[i], "--burst");
                }

                case "--validator-url" -> {
                    i = requireNextArg(args, i, "--validator-url");
                    validatorUrl = args[i];
                }

                case "--skip-validation" -> skipValidation = true;

                default -> throw new IllegalArgumentException(
                    "Option inconnue : '" + args[i] + "'. Utilisez --help pour l'aide."
                );
            }
        }

        // La validation des invariants (ex: au moins un jeu actif) est
        // déléguée au constructeur compact du Record PasswordOptions
        return new PasswordOptions(
            length, useUppercase, useLowercase, useDigits, useSymbols,
            burstCount, validatorUrl, skipValidation
        );
    }

    /**
     * Vérifie qu'un argument "à valeur" a bien une valeur suivante dans le tableau.
     *
     * @param args  Tableau d'arguments
     * @param i     Index courant (de l'option)
     * @param flag  Nom de l'option (pour le message d'erreur)
     * @return L'index i+1 (pour lire la valeur dans la boucle)
     */
    private int requireNextArg(String[] args, int i, String flag) {
        if (i + 1 >= args.length) {
            throw new IllegalArgumentException(
                "L'option '" + flag + "' requiert une valeur. Ex: " + flag + " 20"
            );
        }
        return i + 1;
    }

    /**
     * Parse un entier positif depuis une chaîne, avec message d'erreur contextuel.
     *
     * @param value La chaîne à parser
     * @param flag  Le nom de l'option (pour le message d'erreur)
     * @return L'entier parsé
     */
    private int parsePositiveInt(String value, String flag) {
        try {
            int n = Integer.parseInt(value);
            if (n <= 0) {
                throw new IllegalArgumentException(
                    "La valeur de '" + flag + "' doit être un entier positif. Reçu : " + value
                );
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Valeur invalide pour '" + flag + "' : '" + value + "' n'est pas un entier."
            );
        }
    }

    /**
     * Affiche le manuel d'utilisation sur la sortie standard.
     */
    private void printHelp() {
        System.out.println("""
    

            USAGE:
              java -jar password-tool.jar [OPTIONS]

            OPTIONS:
              --length, -l <n>        Longueur du mot de passe (défaut: 16, min: 4)
              --no-upper              Exclure les majuscules (A-Z)
              --no-lower              Exclure les minuscules (a-z)
              --no-digits             Exclure les chiffres (0-9)
              --no-symbols            Exclure les symboles (!@#$...)
              --burst, -b <n>         Générer n mots de passe (mode rafale)
              --validator-url <url>   URL du validateur (défaut: http://localhost:5000)
              --skip-validation       Générer sans contacter le validateur Docker
              --help, -h              Afficher cette aide

            EXEMPLES:
              java -jar password-tool.jar
              java -jar password-tool.jar --length 24 --no-symbols
              java -jar password-tool.jar --burst 5 --length 20
              java -jar password-tool.jar --skip-validation --length 32
              java -jar password-tool.jar --validator-url http://192.168.1.10:5000

            PRÉREQUIS:
              Le validateur Docker doit être démarré :
              docker build -t zxcvbn-validator ./docker/
              docker run -d -p 5000:5000 --name validator zxcvbn-validator
            """);
    }
}