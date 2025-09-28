public class AnswerOption {
    public String text;      //testo dell'opzione di risposta
    public boolean isCorrect; //true se questa è la risposta corretta

    public AnswerOption(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    @Override
    public String toString() {
        return text + " [" + (isCorrect ? "CORRETTA" : "SBAGLIATA") + "]";
    }
}