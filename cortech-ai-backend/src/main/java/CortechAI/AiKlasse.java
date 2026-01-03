package CortechAI;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

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

        // JSON-body volgens nieuwe Responses API
        String json = """
                {
                    "model": "gpt-4o-mini",
                    "input": [
                        {
                            "role": "user",
                            "content": [
                                {"type": "text", "text": "%s"}
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

        // Optioneel: debug
        // System.out.println("RAW RESPONSE: " + response.body());

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();

        // Check of er output is
        if (!root.has("output")) {
            return "OpenAI gaf geen output terug: " + response.body();
        }

        JsonArray outputArray = root.getAsJsonArray("output");
        if (outputArray.size() == 0) {
            return "OpenAI gaf een lege output terug.";
        }

        JsonObject firstMessage = outputArray.get(0).getAsJsonObject();
        JsonArray contentArray = firstMessage.getAsJsonArray("content");
        if (contentArray.size() == 0) {
            return "OpenAI gaf een lege content terug.";
        }

        // Pak de tekst uit het eerste content-item
        JsonObject contentItem = contentArray.get(0).getAsJsonObject();
        if (!contentItem.has("text")) {
            return "Geen tekst gevonden in content: " + contentItem.toString();
        }

        return contentItem.get("text").getAsString();
    }

    public static void main(String[] args) throws Exception {

        AiKlasse ai = new AiKlasse();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Cortech AI gestart. Typ 'stop' om te stoppen.");

        while (true) {
            System.out.print("\nJij: ");
            String vraag = scanner.nextLine();

            if (vraag.equalsIgnoreCase("stop")) {
                System.out.println("Chat beëindigd.");
                break;
            }

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
