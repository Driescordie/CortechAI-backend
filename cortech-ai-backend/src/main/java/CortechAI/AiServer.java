package CortechAI;

import static spark.Spark.*;

public class AiServer {

    public static void main(String[] args) {

        // Render geeft de poort via de environment variable PORT
        int portNumber = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        port(portNumber);

        AiKlasse ai = new AiKlasse();

        post("/api/chat", (req, res) -> {
            res.type("application/json");

            String vraag = req.body();
            String antwoord;

            try {
                antwoord = ai.sendMessage(vraag);
            } catch (Exception e) {
                antwoord = "Er ging iets mis: " + e.getMessage();
            }

            return "{ \"answer\": \"" + antwoord.replace("\"", "\\\"") + "\" }";
        });

        System.out.println("Cortech AI Server draait op poort " + portNumber + " op /api/chat");
    }
}
