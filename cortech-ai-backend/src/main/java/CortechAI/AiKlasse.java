package CortechAI;

import com.google.gson.JsonArray;
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
        if (apiKey == null) {
            throw new RuntimeException("API key niet gevonden! Zet OPENAI_API_KEY als environment variable.");
        }
        this.client = HttpClient.newHttpClient();
    }

    public String sendMessage(String message) throws Exception {

        String json = """
                {
                  "model": "gpt-4.1",
                  "messages": [
                    {
                      "role": "system",
                      "content": "Je bent Cortech AI, een behulpzame assistent die vragen over computers, software en technologie beantwoordt."
                    },
                    {
                      "role": "user",
                      "content": "%s"
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

        // RAW logging
        System.out.println("[RAW OPENAI RESPONSE] " + response.body());

        // Als het geen JSON is → foutmelding teruggeven
        if (!response.body().trim().startsWith("{")) {
            return "OpenAI stuurde geen geldige JSON terug. Mogelijke oorzaak: verkeerde API key of modelnaam.";
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();

        // Error van OpenAI
        if (root.has("error")) {
            return "OpenAI fout: " + root.getAsJsonObject("error").get("message").getAsString();
        }

        // Nieuw formaat
        if (root.has("output_text")) {
            return root.get("output_text").getAsString();
        }

        // Oud formaat
        if (root.has("output")) {
            JsonArray outputArray = root.getAsJsonArray("output");
            if (!outputArray.isEmpty()) {
                JsonObject firstMessage = outputArray.get(0).getAsJsonObject();
                if (firstMessage.has("content")) {
                    JsonArray contentArray = firstMessage.getAsJsonArray("content");
                    StringBuilder sb = new StringBuilder();
                    for (var item : contentArray) {
                        JsonObject obj = item.getAsJsonObject();
                        if (obj.has("text")) sb.append(obj.get("text").getAsString());
                    }
                    return sb.toString();
                }
            }
        }

        return "(Geen antwoord ontvangen van de AI.)";
    }
}
