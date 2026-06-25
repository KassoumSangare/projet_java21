package com.passwordtool.validator;

import com.passwordtool.model.PasswordScore;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DockerValidatorClient {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;
    private final String baseUrl;

    public DockerValidatorClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public PasswordScore validate(String password) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/validate"))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            }
            return PasswordScore.error("Le validateur a retourné une erreur HTTP " + response.statusCode());
        } catch (java.net.ConnectException e) {
            return PasswordScore.unavailable();
        } catch (java.net.http.HttpTimeoutException e) {
            return PasswordScore.error("Timeout : le validateur met trop de temps à répondre.");
        } catch (Exception e) {
            return PasswordScore.error("Erreur inattendue : " + e.getMessage());
        }
    }

    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/health"))
                    .timeout(CONNECT_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

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
            return PasswordScore.error("Réponse JSON invalide du validateur : " + e.getMessage());
        }
    }
}
