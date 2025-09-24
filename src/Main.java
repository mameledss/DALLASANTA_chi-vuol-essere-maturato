public class Main {
    public static void main(String[] args) {
        ApiClient client = new ApiClient();
        String response = client.fetchQuestions(5, "multiple", "easy");
        System.out.println(response);
    }
}