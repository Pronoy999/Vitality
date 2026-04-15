package com.vitality.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitality.common.dtos.ParsedPrescriptionData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiApiService {
    private static final String SYSTEM_PROMPT = """
            You are a medical document parser specialising in handwritten and printed prescription images.

            Extract structured information from the attached prescription image(s) and return a valid JSON object.

            Rules:
            - Extract only what is explicitly visible. Do not infer or hallucinate values.
            - If a field is absent, illegible, or uncertain, set its value to null.
            - If a word is partially legible, make a best-effort transcription and append "?" to that value (e.g., "Amoxicill?").
            - Normalise medicine names to their canonical form if clearly identifiable; otherwise transcribe exactly as visible.
            - Dosage should be a single string capturing quantity, frequency, and duration. Preserve shorthand like "1-0-1", "BD", "TDS", "OD", "SOS" as-is.
            - Quantity is the total number of units (tablets, capsules, etc.) to be dispensed, as an integer. First, try to infer what is the total number of units needed. If you fail to infer, just set to 1.
            - Health metrics should be key-value pairs with standard abbreviations as keys (e.g., "BP", "SpO2", "BMI", "HR", "Temp", "Weight", "Height", "Blood Glucose").
            - Dates should be in ISO 8601 format (YYYY-MM-DD) if determinable; otherwise transcribe as-is.
            - If multiple images are provided, treat them as pages of the same prescription.
            - Return only the JSON, no explanation, no markdown fences, no preamble.

            Output schema:
            {
              "patient_name": string | null,
              "patient_age": string | null,
              "doctor_name": string | null,
              "date": string | null,
              "patient_issue": string | null,
              "diagnosis": string | null,
              "health_metrics": { "<metric_key>": string } | null,
              "medicines": [
                {
                  "name": string | null,
                  "dosage": string | null,
                  "quantity": integer | null
                }
              ] | null
            }
            """;
    private static final String USER_PROMPT = "The attached image(s) contain a medical prescription. Parse all visible information and return a JSON object matching the schema.";
    private static final Map<String, String> MIME_TYPES = Map.of(
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".webp", "image/webp",
            ".heic", "image/heic"
    );

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    public boolean isConfigured() {
        return geminiApiKey != null && !geminiApiKey.isBlank();
    }

    public ParsedPrescriptionData parsePrescription(List<Path> imagePaths) throws IOException, InterruptedException {
        String responseText = callGemini(imagePaths);
        String cleaned = cleanJson(responseText);
        return objectMapper.readValue(cleaned, ParsedPrescriptionData.class);
    }

    private String callGemini(List<Path> imagePaths) throws IOException, InterruptedException {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent";
        String requestBody = objectMapper.writeValueAsString(buildGeminiPayload(imagePaths));
        IOException lastIOException = null;
        InterruptedException lastInterruptedException = null;

        for (int attempt = 0; attempt < 5; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(120))
                    .header("x-goog-api-key", geminiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 429 || response.statusCode() >= 500) {
                    if (attempt == 4) {
                        throw new IOException("Gemini request failed with status " + response.statusCode() + ": " + response.body());
                    }
                    sleepBeforeRetry(attempt);
                    continue;
                }
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("Gemini request failed with status " + response.statusCode() + ": " + response.body());
                }
                return extractGeminiText(response.body());
            } catch (IOException e) {
                lastIOException = e;
                if (attempt == 4) {
                    throw e;
                }
                sleepBeforeRetry(attempt);
            } catch (InterruptedException e) {
                lastInterruptedException = e;
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        if (lastInterruptedException != null) {
            throw lastInterruptedException;
        }
        throw lastIOException == null ? new IOException("Gemini request failed after retries") : lastIOException;
    }

    private Map<String, Object> buildGeminiPayload(List<Path> imagePaths) throws IOException {
        List<Map<String, Object>> parts = new ArrayList<>();
        for (Path imagePath : imagePaths) {
            String extension = getExtension(imagePath.getFileName().toString());
            String mimeType = MIME_TYPES.getOrDefault(extension, "image/jpeg");
            String data = Base64.getEncoder().encodeToString(Files.readAllBytes(imagePath));
            parts.add(Map.of("inlineData", Map.of("mimeType", mimeType, "data", data)));
        }
        parts.add(Map.of("text", USER_PROMPT));
        return Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", SYSTEM_PROMPT))),
                "contents", List.of(Map.of("parts", parts))
        );
    }

    private String extractGeminiText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.size() == 0) {
            throw new IOException("Gemini returned no content parts: " + responseBody);
        }
        String text = parts.path(0).path("text").asText();
        if (text == null || text.isBlank()) {
            throw new IOException("Gemini returned empty text: " + responseBody);
        }
        return text;
    }

    private String cleanJson(String text) {
        String cleaned = text.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private void sleepBeforeRetry(int attempt) throws InterruptedException {
        Thread.sleep((long) (2000 * Math.pow(2, attempt)));
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase();
    }
}
