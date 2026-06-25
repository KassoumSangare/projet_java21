package com.passwordtool.cli;

import com.passwordtool.model.PasswordOptions;

public class CliParser {
    public PasswordOptions parse(String[] args) {
        int length = 16;
        boolean useUppercase = true;
        boolean useLowercase = true;
        boolean useDigits = true;
        boolean useSymbols = true;
        int burstCount = 1;
        String validatorUrl = "http://localhost:5000";
        boolean skipValidation = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help", "-h" -> {
                    printHelp();
                    System.exit(0);
                }
                case "--length", "-l" -> {
                    i = requireNextArg(args, i, "--length");
                    length = parsePositiveInt(args[i], "--length");
                }
                case "--no-upper" -> useUppercase = false;
                case "--no-lower" -> useLowercase = false;
                case "--no-digits" -> useDigits = false;
                case "--no-symbols" -> useSymbols = false;
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

        return new PasswordOptions(length, useUppercase, useLowercase, useDigits, useSymbols,
            burstCount, validatorUrl, skipValidation);
    }

    private int requireNextArg(String[] args, int i, String flag) {
        if (i + 1 >= args.length) {
            throw new IllegalArgumentException(
                "L'option '" + flag + "' requiert une valeur. Ex: " + flag + " 20"
            );
        }
        return i + 1;
    }

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
            """);
    }
}
