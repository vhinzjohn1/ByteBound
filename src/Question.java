// One quiz question. The answers line up with the three portals:
// index 0 is the top portal, 1 is the middle, 2 is the bottom.
public class Question {

    public String prompt;
    public String[] answers;
    public int correctIndex;

    public Question(String prompt, String[] answers, int correctIndex) {
        this.prompt = prompt;
        this.answers = answers;
        this.correctIndex = correctIndex;
    }
}
