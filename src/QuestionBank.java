// Every question for the game lives here.
// To add a stage, add one more "new Question(...)" to the list.
public class QuestionBank {

    public static Question[] load() {
        return new Question[] {
            new Question(
                "What does System.out.println do?",
                new String[] { "Prints a line", "Deletes a file", "Closes Java" },
                0
            ),
            new Question(
                "Which type stores text?",
                new String[] { "int", "String", "boolean" },
                1
            ),
            new Question(
                "Which type stores a whole number?",
                new String[] { "String", "boolean", "int" },
                2
            ),
            new Question(
                "Which keyword is used to create a class in Java?",
                new String[] { "class", "method", "public" },
                0
            ),
            new Question(
                "Which symbol is used to end a statement in Java?",
                new String[] { ":", ".", ";" },
                2
            )
        };
    }
}