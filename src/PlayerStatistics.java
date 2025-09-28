public class PlayerStatistics {
private String name;
private int correctAnswers;
private boolean used5050;
private boolean usedAudience;

public PlayerStatistics(String name, int correctAnswers, boolean used5050, boolean usedAudience) {
    this.name = name;
    this.correctAnswers = correctAnswers;
    this.used5050 = used5050;
    this.usedAudience = usedAudience;
}
}
