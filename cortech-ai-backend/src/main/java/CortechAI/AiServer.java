package CortechAI;

import static spark.Spark.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AiServer {

    public static void main(String[] args) {

        // Render geeft de poort via de environment variable PORT
        int portNumber = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        port(portNumber);

        // ✅ CORS setup (HEEL BELANGRIJK voor frontend)
        options("/*", (request, response) -> {
            String headers = request.headers("Access-Control-Request-Headers");
            if (headers != null) {
                response.header("Access-Control-Allow-Headers", headers);
            }

            String method = request.headers("Access-Control-Request-Method");
            if (method != null) {
                response.header("Access-Control-Allow-Methods", method);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type");
        });

        AiKlasse ai = new AiKlasse();

        // ✅ Chat endpoint
        post("/api/chat", (req, res) -> {
            res.type("application/json");

            String vraag;
            try {
                JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
                vraag = json.get("message").getAsString();
            } catch (Exception e) {
                res.status(400);
                return "{ \"error\": \"Ongeldig request body\" }";
            }

            String antwoord;
            try {
                antwoord = ai.sendMessage(vraag);
            } catch (Exception e) {
                res.status(500);
                return "{ \"error\": \"AI error\" }";
            }

            return "{ \"answer\": \"" + antwoord.replace("\"", "\\\"") + "\" }";
        });

        // (optioneel maar handig)
        get("/", (req, res) -> "Cortech AI backend draait ✅");

        System.out.println("Cortech AI Server draait op poort " + portNumber);
    }
}
