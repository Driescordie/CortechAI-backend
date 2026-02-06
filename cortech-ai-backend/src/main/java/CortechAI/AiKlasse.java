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

    // Stuurt een bericht naar de AI en geeft het antwoord terug
    public String sendMessage(String message) throws Exception {

        String json = """
                {
                  "model": "gpt-4.1",
                  "messages": [
                    {
                      "role": "system",
                      "content": "Je bent Cortech AI, een behulpzame assistent die vragen over computers, software en technologie beantwoordt. Geef altijd praktische en duidelijke oplossingen."
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

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();

        // Output valideren
        if (!root.has("output") || root.getAsJsonArray("output").isEmpty()) {
            return "(Geen antwoord ontvangen van de AI.)";
        }

        JsonArray outputArray = root.getAsJsonArray("output");
        JsonObject firstMessage = outputArray.get(0).getAsJsonObject();

        if (!firstMessage.has("content") || firstMessage.getAsJsonArray("content").isEmpty()) {
            return "(Geen antwoord ontvangen van de AI.)";
        }

        JsonArray contentArray = firstMessage.getAsJsonArray("content");
        StringBuilder fullText = new StringBuilder();

        for (int i = 0; i < contentArray.size(); i++) {
            JsonObject contentItem = contentArray.get(i).getAsJsonObject();
            if (contentItem.has("text")) {
                fullText.append(contentItem.get("text").getAsString());
                if (i < contentArray.size() - 1) {
                    fullText.append("\n");
                }
            }
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
                System.out.println("Er ging iets mis: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
