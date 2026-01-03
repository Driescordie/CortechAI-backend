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

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();

        try {
            JsonArray outputArray = root.getAsJsonArray("output");
            if (outputArray.size() == 0) return "(Geen antwoord ontvangen van de AI.)";

            JsonObject firstMessage = outputArray.get(0).getAsJsonObject();
            JsonArray contentArray = firstMessage.getAsJsonArray("content");
            if (contentArray.size() == 0) return "(Geen antwoord ontvangen van de AI.)";

            JsonObject contentItem = contentArray.get(0).getAsJsonObject();

            // Correcte key: "text" of "output_text"
            if (contentItem.has("text")) return contentItem.get("text").getAsString();
            if (contentItem.has("output_text")) return contentItem.get("output_text").getAsString();

            return "(Geen tekst ontvangen van de AI.)";

        } catch (Exception e) {
            return "(Fout bij uitlezen van AI-antwoord, maar chat blijft werken.)";
        }
    }

    // Test main voor console
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
                System.out.println("Er ging iets mis: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
