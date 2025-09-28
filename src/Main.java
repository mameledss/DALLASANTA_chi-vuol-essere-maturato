import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean used5050 = false;
    private static boolean usedAudience = false;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   CHI VUOL ESSERE MATURATO?   ");
        System.out.println("=================================\n");

        System.out.print("Inserisci il tuo nome: ");
        String playerName = scanner.nextLine().trim();

        if (playerName.isEmpty()) {
            playerName = "Anonimo";
        }

        System.out.println("\nBenvenuto " + playerName + "!");
        System.out.println("Dovrai rispondere a 10 domande per diventare maturato:");
        System.out.println("- 5 domande facili");
        System.out.println("- 3 domande medie");
        System.out.println("- 2 domande difficili");
        System.out.println("\nHai a disposizione 2 aiuti:");
        System.out.println("- H1: Guarda il bigliettino (50/50)");
        System.out.println("- H2: Suggerimento dei compagni");
        System.out.println("- R: Ritirati in qualsiasi momento\n");

        ApiClient client = new ApiClient();
        List<ApiQuestion> allQuestions = new ArrayList<>();

        //domande con difficoltà crescente
        try {
            //5 domande facili
            List<ApiQuestion> easyQuestions = client.fetchQuestionsAsList(5, "multiple", "easy");
            if (easyQuestions == null || easyQuestions.size() < 5) {
                System.out.println("Errore nel caricamento domande facili");
                return;
            }
            allQuestions.addAll(easyQuestions);

            //3 domande medie
            List<ApiQuestion> mediumQuestions = client.fetchQuestionsAsList(3, "multiple", "medium");
            if (mediumQuestions == null || mediumQuestions.size() < 3) {
                System.out.println("Errore nel caricamento domande medie");
                return;
            }
            allQuestions.addAll(mediumQuestions);

            //2 domande difficili
            List<ApiQuestion> hardQuestions = client.fetchQuestionsAsList(2, "multiple", "hard");
            if (hardQuestions == null || hardQuestions.size() < 2) {
                System.out.println("Errore nel caricamento domande difficili");
                return;
            }
            allQuestions.addAll(hardQuestions);

        } catch (Exception e) {
            System.out.println("Errore nel caricamento delle domande: " + e.getMessage());
            return;
        }

        System.out.println("Domande caricate con successo, inizio gioco\n");

        int correctAnswers = playGame(allQuestions, playerName); //inizio del gioco

        savePlayerStatistics(playerName, correctAnswers, used5050, usedAudience); //salvataggio statistiche

        // Messaggio finale
        if (correctAnswers == 10) {
            System.out.println(playerName.toUpperCase() + " SEI MATURATO!");
        } else {
            System.out.println("Hai risposto correttamente a " + correctAnswers + " domande su 10");
        }
        scanner.close();
    }

    private static int playGame(List<ApiQuestion> questions, String playerName) {
        int correctAnswers = 0;
        int safePoint = 0; //punteggio sicuro raggiunto

        for (int i = 0; i < questions.size(); i++) {
            ApiQuestion question = questions.get(i);

            //aggiorna safe point
            if (i == 4) safePoint = 5; //dopo 5 domande facili
            if (i == 7) safePoint = 8; //dopo 3 domande medie

            System.out.println("DOMANDA " + (i + 1) + "/10");
            System.out.println("Difficoltà: " + question.difficulty.toUpperCase());
            System.out.println("Categoria: " + question.category);
            System.out.println();

            //prepara opzioni di risposta
            List<AnswerOption> options = prepareAnswerOptions(question);
            Collections.shuffle(options);

            System.out.println(question.question);
            System.out.println();

            char[] letters = {'A', 'B', 'C', 'D'};
            for (int j = 0; j < options.size(); j++) {
                System.out.println(letters[j] + ") " + options.get(j).text);
            }

            System.out.println();
            System.out.println("Aiuti disponibili:");
            if (!used5050) System.out.println("H1 - Guarda il bigliettino (50/50)");
            if (!usedAudience) System.out.println("H2 - Suggerimento dei compagni");
            System.out.println("R - Ritirati");
            System.out.print("\nLa tua risposta (A/B/C/D/H1/H2/R): ");

            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("R")) {
                System.out.println("Ti sei ritirato con " + correctAnswers + " risposte corrette.");
                return correctAnswers;
            }

            if (input.equals("H1") && !used5050) {
                options = use5050Help(options);
                used5050 = true;

                System.out.println("\n=== AIUTO 50/50 UTILIZZATO ===");
                for (int j = 0; j < options.size(); j++) {
                    System.out.println(letters[j] + ") " + options.get(j).text);
                }
                System.out.print("\nLa tua risposta (A/B/C/D): ");
                input = scanner.nextLine().trim().toUpperCase();
            }

            if (input.equals("H2") && !usedAudience) {
                useAudienceHelp(options);
                usedAudience = true;
                System.out.print("\nLa tua risposta (A/B/C/D): ");
                input = scanner.nextLine().trim().toUpperCase();
            }

            if (input.equals("H1") && used5050) {
                System.out.println("Hai già utilizzato l'aiuto 50/50");
                i--; //ripeti la domanda
                continue;
            }

            if (input.equals("H2") && usedAudience) {
                System.out.println("Hai già utilizzato il suggerimento dei compagni");
                i--; //ripeti la domanda
                continue;
            }

            //verifica la risposta
            if (input.length() == 1 && "ABCD".contains(input)) { //se è di lunghezza 1 ed è A, B, C, D
                int selectedIndex = input.charAt(0) - 'A'; //Converte la lettera in un indice numerico: A=0, B=1, C=2, D=3
                if (selectedIndex < options.size() && options.get(selectedIndex).isCorrect) { //se l'opzione è corretta
                    correctAnswers++;
                    System.out.println("RISPOSTA CORRETTA!\n");
                } else {
                    System.out.println("RISPOSTA SBAGLIATA!");
                    System.out.println("La risposta corretta era: " + question.correct_answer);
                    System.out.println("Hai totalizzato " + safePoint + " punti sicuri");
                    return safePoint;
                }
            } else {
                System.out.println("Input non valido. Inserisci A, B, C, D, H1, H2 o R");
                i--; //ripeti la domanda
            }
        }
        return correctAnswers;
    }

    private static List<AnswerOption> prepareAnswerOptions(ApiQuestion question) {
        List<AnswerOption> options = new ArrayList<>();
        options.add(new AnswerOption(question.correct_answer, true));

        for (String incorrect : question.incorrect_answers) {
            options.add(new AnswerOption(incorrect, false));
        }
        return options;
    }

    private static List<AnswerOption> use5050Help(List<AnswerOption> options) {
        List<AnswerOption> result = new ArrayList<>();
        AnswerOption correctOption = null;
        List<AnswerOption> incorrectOptions = new ArrayList<>();

        //separa opzione corretta da quelle sbagliate
        for (AnswerOption option : options) {
            if (option.isCorrect) {
                correctOption = option;
            } else {
                incorrectOptions.add(option);
            }
        }
        result.add(correctOption); //mantiene la risposta corretta

        if (!incorrectOptions.isEmpty()) { //aggiungi solo una risposta sbagliata casuale
            Collections.shuffle(incorrectOptions);
            result.add(incorrectOptions.get(0));
        }
        Collections.shuffle(result);  // Mescola di nuovo le opzioni rimaste
        return result;
    }

    private static void useAudienceHelp(List<AnswerOption> options) {
        Random random = new Random();
        System.out.println("\n=== SUGGERIMENTO DEI COMPAGNI ===");

        char[] letters = {'A', 'B', 'C', 'D'};
        int correctIndex = -1;

        //trova l'indice della risposta corretta
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).isCorrect) {
                correctIndex = i;
                break;
            }
        }
        int[] percentages = new int[options.size()]; //array di 4 che ospiterà percentuali
        int totalPercentage = 100;

        if (correctIndex != -1) {
            percentages[correctIndex] = 45 + random.nextInt(26); //da una percentuale tra 45% e 70% alla risposta corretta
            totalPercentage -= percentages[correctIndex]; //sottrae questa percentuale da 100
        }

        //distribuisci il resto tra le altre opzioni
        for (int i = 0; i < options.size(); i++) {
            if (i != correctIndex && totalPercentage > 0) {
                int maxPercent = Math.min(totalPercentage, 30); //calcola il massimo assegnabile (30% o quello che rimane)
                percentages[i] = random.nextInt(maxPercent + 1); //assegna una percentuale casuale fino a quel massimo
                totalPercentage -= percentages[i]; //aggiorna il totale rimanente
            }
        }

        //se rimangono punti percentuali, li assegna alla prima opzione sbagliata per arrivare esattamente al 100%
        for (int i = 0; i < options.size() && totalPercentage > 0; i++) {
            if (i != correctIndex) {
                percentages[i] += totalPercentage;
                break;
            }
        }
        //mostra i risultati
        for (int i = 0; i < options.size(); i++) {
            System.out.println(letters[i] + ") " + percentages[i] + "%");
        }
    }

    private static void savePlayerStatistics(String playerName, int correctAnswers, boolean used5050, boolean usedAudience) {
        PlayerStatistics stats = new PlayerStatistics(playerName, correctAnswers, used5050, usedAudience);

        saveToFile(stats); //salva in un file unico per tutti i giocatori

        //salva anche il file individuale
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(playerName.replaceAll("\\s+", "_") + "_stats.json")) {
            gson.toJson(stats, writer);
            System.out.println("Statistiche salvate in: " + playerName.replaceAll("\\s+", "_") + "_stats.json");
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio delle statistiche individuali: " + e.getMessage());
        }
    }

    private static void saveToFile(PlayerStatistics newStats) {
        List<PlayerStatistics> allStats = new ArrayList<>();
        Gson gson = new Gson();

        //legge statistiche esistenti se il file esiste
        File file = new File("all_players_stats.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<PlayerStatistics>>(){}.getType();
                List<PlayerStatistics> existingStats = gson.fromJson(reader, listType);
                if (existingStats != null) {
                    allStats = existingStats;
                }
            } catch (IOException e) {
                System.out.println("Errore nella lettura del file globale: " + e.getMessage());
            }
        }
        allStats.add(newStats); //aggiunge le nuove statistiche

        //salva tutto nel file globale
        try (FileWriter writer = new FileWriter("all_players_stats.json")) {
            gson.toJson(allStats, writer);
            System.out.println("Statistiche aggiunte al file globale: all_players_stats.json");
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio del file globale: " + e.getMessage());
        }
    }
}