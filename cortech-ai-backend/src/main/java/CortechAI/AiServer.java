package CortechAI;

import static spark.Spark.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AiServer {

    public static void main(String[] args) {

        // Poort instellen (Render geeft PORT, lokaal gebruik 10000)
        int portNumber = Integer.parseInt(System.getenv().getOrDefault("PORT", "10000"));
        port(portNumber);

        // CORS instellen
        options("/*", (request, response) -> {
            String headers = request.headers("Access-Control-Request-Headers");
            if (headers != null) response.header("Access-Control-Allow-Headers", headers);
            String method = request.headers("Access-Control-Request-Method");
            if (method != null) response.header("Access-Control-Allow-Methods", method);
            return "OK";
        });

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });

        // Instantie van de AI
        AiKlasse ai = new AiKlasse();

        // Endpoint voor chat
        post("/api/chat", (req, res) -> {
            res.type("application/json");

            String vraag;
            try {
                JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
                vraag = json.get("message").getAsString();
            } catch (Exception e) {
                res.status(400);
                JsonObject error = new JsonObject();
                error.addProperty("error", "Ongeldig request body");
                return error.toString();
            }

            System.out.println("[IN] Vraag: " + vraag);

            String antwoord;
            try {
                antwoord = ai.sendMessage(vraag);

                if (antwoord == null || antwoord.isBlank()) {
                    antwoord = "Sorry, ik weet hier geen antwoord op.";
                }

                System.out.println("[OUT] Antwoord: " + antwoord);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                JsonObject error = new JsonObject();
                error.addProperty("error", "AI error");
                return error.toString();
            }

            // JSON-response veilig genereren
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("answer", antwoord);
            return jsonResponse.toString();
        });

        // Test endpoint
        get("/", (req, res) -> "Cortech AI backend draait ✅");

        System.out.println("Cortech AI Server draait op poort " + portNumber);
    }
}
