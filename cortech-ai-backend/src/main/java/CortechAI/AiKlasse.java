package CortechAI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AiKlasse {

    private final String apiKey;
    private final HttpClient client;

    public AiKlasse() {
        this.apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("API key niet gevonden! Zet OPENAI_API_KEY als environment variable.");
        }
        this.client = HttpClient.newHttpClient();
    }

    public String sendMessage(String message) throws Exception {
        String json = """
                {
                  "model": "gpt-4o-mini",
                  "input": [
                    {
                      "role": "system",
                      "content": [
                        {
                          "type": "input_text",
                          "text": "Je bent Cortech AI, een behulpzame assistent die vragen over computers, software en technologie beantwoordt. Geef altijd praktische en duidelijke oplossingen."
                        }
                      ]
                    },
                    {
                      "role": "user",
                      "content": [
                        {
                          "type": "input_text",
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/responses"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String body = response.body();

        // 1) HTTP-status check
        if (status < 200 || status >= 300) {
            return "Fout van AI-backend (HTTP " + status + "): " + body;
        }

        JsonObject root;
        try {
            root = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            return "Kon AI-antwoord niet parsen: " + e.getMessage() + "\nRAW: " + body;
        }

        // 2) Als er een error-object is, toon dat
        if (root.has("error")) {
            JsonObject err = root.getAsJsonObject("error");
            String msg = err.has("message") ? err.get("message").getAsString() : err.toString();
            return "AI-error: " + msg;
        }

        // 3) Normale Responses-API output lezen
        if (!root.has("output")) {
            return "Geen 'output' veld in AI-antwoord.\nRAW: " + body;
        }

        JsonArray outputArray = root.getAsJsonArray("output");
        if (outputArray.size() == 0) {
            return "Lege 'output' array in AI-antwoord.\nRAW: " + body;
        }

        JsonObject firstMessage = outputArray.get(0).getAsJsonObject();
        if (!firstMessage.has("content")) {
            return "Geen 'content' in eerste output-item.\nRAW: " + body;
        }

        JsonArray contentArray = firstMessage.getAsJsonArray("content");
        if (contentArray.size() == 0) {
            return "Lege 'content' array in AI-antwoord.\nRAW: " + body;
        }

        StringBuilder fullText = new StringBuilder();
        for (JsonElement el : contentArray) {
            if (!el.isJsonObject()) continue;
            JsonObject contentItem = el.getAsJsonObject();
            if (contentItem.has("text")) {
                if (fullText.length() > 0) fullText.append("\n");
                fullText.append(contentItem.get("text").getAsString());
            }
        }

        if (fullText.length() == 0) {
            return "Geen tekst gevonden in AI-antwoord.\nRAW: " + body;
        }

        return fullText.toString();
    }

    // Console test
    public static void main(String[] args) throws Exception {
        AiKlasse ai = new AiKlasse();
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("Cortech AI gestart. Typ 'stop' om te stoppen.");

        while (true) {
            System.out.print("\nJij: ");
            String vraag = scanner.nextLine();
            if (vraag.equalsIgnoreCase("stop")) break;
            try {
                String antwoord = ai.sendMessage(vraag);
                System.out.println("Cortech: " + antwoord);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Er ging iets mis: " + e.getMessage());
            }
        }
        scanner.close();
    }
}
