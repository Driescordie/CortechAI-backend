package CortechAi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AiKlasse {

    // Enjoy the code boyssssss

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
          "input": [
            {
              "role": "system",
              "content": [
                {"type": "input_text", "text": "Je bent Cortech AI, een behulpzame assistent die vragen over computers, software en technologie beantwoordt."}
              ]
            },
            {
              "role": "user",
              "content": [
                {"type": "input_text", "text": "%s"}
              ]
            }
          ]
        }
        """.formatted(message);

        System.out.println("[JSON VOOR VERSTUREN] " + json);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/responses"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[RAW OPENAI RESPONSE] " + response.body());

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();

    
        if (root.has("output_text")) {
            return root.get("output_text").getAsString();
        }

        
        if (root.has("output")) {
            JsonArray outputArray = root.getAsJsonArray("output");
            StringBuilder sb = new StringBuilder();
            for (var item : outputArray) {
                JsonObject obj = item.getAsJsonObject();
                if (obj.has("content")) {
                    JsonArray contentArray = obj.getAsJsonArray("content");
                    for (var c : contentArray) {
                        JsonObject contentObj = c.getAsJsonObject();
                        if (contentObj.has("text")) {
                            sb.append(contentObj.get("text").getAsString());
                        }
                    }
                }
            }
            if (sb.length() > 0) return sb.toString();
        }

        return "(Geen antwoord ontvangen van de AI.)";
    }

  
    public static void main(String[] args) throws Exception {
        AiKlasse ai = new AiKlasse();
        String antwoord = ai.sendMessage("Wat is het verschil tussen SSD en HDD?");
        System.out.println("AI antwoord: " + antwoord);
    }
}
