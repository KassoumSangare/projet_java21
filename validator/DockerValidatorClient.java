package com.passwordtool.validator;

import com.passwordtool.model.PasswordScore;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
public class DockerValidatorClient {

    // Timeout de connexion : si le conteneur ne répond pas en 3s, on abandonne.
    // Valeur choisie pour être réactive sans être trop agressive.
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);

    // Timeout de lecture : la requête zxcvbn est quasi-instantanée.
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;
    private final String baseUrl;

    /**
     * Constructeur : initialise le client HTTP avec les timeouts configurés.
     *
     * @param baseUrl URL de base du micro-service (ex: "http://localhost:5000")
     */
    public DockerValidatorClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        // HttpClient Java 21 : builder pattern, immuable une fois construit
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                // Suit automatiquement les redirections HTTP (bonne pratique)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Envoie un mot de passe au validateur Docker et retourne son score.
     *
     * Protocole :
     *   POST {baseUrl}/validate
     *   Content-Type: application/json
     *   Body: {"password": "<le mot de passe>"}
     *
     * @param password Le mot de passe à valider
     * @return PasswordScore avec le résultat, ou PasswordScore.error() en cas d'échec
     */
    public PasswordScore validate(String password) {
        try {
            // ── Construction du corps JSON de la requête ───────────────────────
            // On utilise org.json pour la sérialisation (évite les injections
            // si le mot de passe contient des guillemets ou antislashs)
            JSONObject requestBody = new JSONObject();
            requestBody.put("password", password);

            // ── Construction de la requête HTTP ───────────────────────────────
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/validate"))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    // POST avec le corps JSON encodé en UTF-8
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // ── Envoi synchrone de la requête (adapté à un outil CLI) ──────────
            // En CLI, l'utilisateur attend le résultat — pas besoin d'async.
            // En cas d'API serveur, on utiliserait sendAsync().
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // ── Traitement de la réponse ──────────────────────────────────────
            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                // Le serveur a répondu, mais avec une erreur (ex: 400, 500)
                return PasswordScore.error(
                    "Le validateur a retourné une erreur HTTP " + response.statusCode()
                );
            }

        } catch (java.net.ConnectException e) {
            // Cas le plus fréquent : le conteneur Docker n'est pas démarré
            return PasswordScore.unavailable();

        } catch (java.net.http.HttpTimeoutException e) {
            // Le conteneur est démarré mais ne répond pas dans les délais
            return PasswordScore.error("Timeout : le validateur met trop de temps à répondre.");

        } catch (Exception e) {
            // Catch-all pour toute autre exception inattendue (JSON malformé, etc.)
            return PasswordScore.error("Erreur inattendue : " + e.getMessage());
        }
    }

    /**
     * Vérifie si le micro-service est disponible via l'endpoint /health.
     * Utile pour informer l'utilisateur avant de lancer un mode rafale.
     *
     * @return true si le service répond avec {"status": "ok"}
     */
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/health"))
                    .timeout(CONNECT_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200;

        } catch (Exception e) {
            // Toute exception signifie que le service est inaccessible
            return false;
        }
    }

    /**
     * Parse la réponse JSON du validateur et construit un PasswordScore.
     *
     * Format attendu :
     * {
     *   "score": 3,
     *   "feedback": "Ajoutez des caractères supplémentaires",
     *   "crack_time_display": "3 heures",
     *   "crack_time_seconds": 10800.0,
     *   "guesses": 360000,
     *   "password_length": 12
     * }
     *
     * @param jsonBody Corps de la réponse HTTP en String
     * @return PasswordScore désérialisé
     */
    private PasswordScore parseResponse(String jsonBody) {
        try {
            JSONObject json = new JSONObject(jsonBody);

            return new PasswordScore(
                json.getInt("score"),
                json.optString("feedback", "Aucun feedback disponible"),
                json.optString("crack_time_display", "N/A"),
                json.optDouble("crack_time_seconds", -1),
                json.optLong("guesses", -1),
                json.optInt("password_length", 0)
            );

        } catch (Exception e) {
            // La réponse reçue n'est pas un JSON valide (erreur côté Flask ?)
            return PasswordScore.error("Réponse JSON invalide du validateur : " + e.getMessage());
        }
    }
}