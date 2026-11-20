
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ByteBound {

    JFrame window;
    JPanel mainPanel;
    CardLayout cardLayout;

    // The play area is half the monitor, so the game is easy to see.
    int gameWidth;
    int gameHeight;
    GamePanel currentGame;

    public ByteBound() {

        window = new JFrame();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Byte Bound");

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // TITLE SCREEN
        TitleScreen titleScreen = new TitleScreen(this);
        mainPanel.add(titleScreen, "TITLE");

        // CHARACTER SELECTION SCREEN
        CharacterSelectionScreen characterScreen =
                new CharacterSelectionScreen(this);

        mainPanel.add(characterScreen, "CHARACTER");

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        gameWidth = screen.width / 2;
        gameHeight = screen.height / 2;
        mainPanel.setPreferredSize(new Dimension(gameWidth, gameHeight));

        window.add(mainPanel);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        showTitleScreen();
        Music.startLoop();
    }

    // Called when START GAME is clicked
    public void startGame() {
        cardLayout.show(mainPanel, "CHARACTER");
    }

    // Called when BACK is clicked
    public void showTitleScreen() {
        cardLayout.show(mainPanel, "TITLE");
    }

    // Called when PLAY is clicked
    public void startGame(String selectedCharacter) {
        closeGame();

        currentGame = new GamePanel(selectedCharacter, gameWidth, gameHeight, this);
        mainPanel.add(currentGame, "GAME");
        cardLayout.show(mainPanel, "GAME");
        mainPanel.revalidate();
        mainPanel.repaint();

        currentGame.startGameThread();
        SwingUtilities.invokeLater(() -> currentGame.requestFocusInWindow());
    }

    // Called from the win or game over panel.
    public void backToTitle(GamePanel finished) {
        if (currentGame == finished) {
            mainPanel.remove(finished);
            currentGame = null;
        }
        showTitleScreen();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void closeGame() {
        if (currentGame == null) {
            return;
        }
        currentGame.stopLoop();
        mainPanel.remove(currentGame);
        currentGame = null;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new ByteBound();
        });
    }
}
