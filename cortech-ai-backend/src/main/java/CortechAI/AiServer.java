package CortechAI;


import static spark.Spark.*;

public class AiServer {

    public static void main(String[] args) {

        // Start server op poort 8080 (Render gebruikt deze)
        port(10000);

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

        System.out.println("Cortech AI Server draait op /api/chat");
    }
}


