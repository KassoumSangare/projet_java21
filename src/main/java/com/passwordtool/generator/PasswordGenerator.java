package com.passwordtool.generator;

import com.passwordtool.model.PasswordOptions;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(PasswordOptions options) {
        StringBuilder alphabetBuilder = new StringBuilder();
        List<Character> mandatoryChars = new ArrayList<>();

        if (options.useUppercase()) {
            alphabetBuilder.append(UPPERCASE);
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

        List<Character> passwordChars = new ArrayList<>(mandatoryChars);
        int remaining = options.length() - mandatoryChars.size();

        for (int i = 0; i < remaining; i++) {
            passwordChars.add(alphabet.charAt(secureRandom.nextInt(alphabetSize)));
        }

        Collections.shuffle(passwordChars, secureRandom);

        StringBuilder password = new StringBuilder(options.length());
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    public double calculateEntropy(PasswordOptions options) {
        int alphabetSize = 0;
        if (options.useUppercase()) alphabetSize += UPPERCASE.length();
        if (options.useLowercase()) alphabetSize += LOWERCASE.length();
        if (options.useDigits()) alphabetSize += DIGITS.length();
        if (options.useSymbols()) alphabetSize += SYMBOLS.length();

        return options.length() * (Math.log(alphabetSize) / Math.log(2));
    }

    private char randomCharFrom(String charset) {
        return charset.charAt(secureRandom.nextInt(charset.length()));
    }
}
