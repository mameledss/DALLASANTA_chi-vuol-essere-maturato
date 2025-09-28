import com.google.gson.Gson;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ApiClient {
    private final HttpClient client = HttpClient.newHttpClient();

    public String fetchQuestions(int amount, String type, String difficulty) {
        String url = "https://opentdb.com/api.php?amount=" + amount + "&difficulty=" + difficulty + "&type=" + type;
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(java.net.URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return "Errore richiesta API";
        }

        Gson gson = new Gson();
        ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
        for (ApiQuestion question : apiResponse.results) {
            System.out.print(question.question);
            System.out.println(question.correct_answer);
        }
        return response.body();
    }
    public List<ApiQuestion> fetchQuestionsAsList(int amount, String type, String difficulty) {
        //costruzione URL per l'API
        String url = "https://opentdb.com/api.php?amount=" + amount + "&difficulty=" + difficulty + "&type=" + type;

        //creazione richiesta HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(java.net.URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            //invio della richiesta
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Errore nella richiesta HTTP: " + e.getMessage());
            return null;
        }

        try {
            //parsing JSON di risposta
            Gson gson = new Gson();
            ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);

            //controllo del codice di risposta dell'API
            //0 = Success, 1 = No Results, 2 = Invalid Parameter, 3 = Token Not Found, 4 = Token Empty
            if (apiResponse.response_code != 0) {
                System.out.println("Errore API Open Trivia DB - Codice: " + apiResponse.response_code);
                switch (apiResponse.response_code) {
                    case 1:
                        System.out.println("Nessun risultato trovato per i parametri specificati");
                        break;
                    case 2:
                        System.out.println("Parametri non validi nella richiesta");
                        break;
                    case 3:
                        System.out.println("Token di sessione non trovato");
                        break;
                    case 4:
                        System.out.println("Token di sessione esaurito");
                        break;
                    default:
                        System.out.println("Errore sconosciuto");
                }
                return null;
            }

            //verifica che ci siano abbastanza domande
            if (apiResponse.results == null || apiResponse.results.size() < amount) {
                System.out.println("Numero insufficiente di domande ricevute dall'API");
                return null;
            }
            System.out.println("Caricate " + apiResponse.results.size() + " domande di difficoltà: " + difficulty);
            return apiResponse.results;

        } catch (Exception e) {
            System.out.println("Errore nel parsing JSON: " + e.getMessage());
            return null;
        }
    }
}