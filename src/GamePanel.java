import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.LockSupport;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GamePanel extends JPanel implements Runnable {

    // Real size of the panel. Portals and text use these, not a fixed 960x768.
    int screenWidth;
    int screenHeight;

    // Same size Player.draw uses for the sprite.
    final int playerSize = 120;

    // Quiz rules. Change these if you want a longer or harder game.
    final int startingHearts = 3;
    final int pointsPerCorrect = 10;

    //FPS
    int FPS = 60;

    // Created once. Building a Font during paint makes garbage and causes hitches.
    final Font answerFont = new Font("Monospaced", Font.BOLD, 16);
    final Font hudFont = new Font("Monospaced", Font.BOLD, 22);
    final Font messageFont = new Font("Monospaced", Font.BOLD, 20);
    final Font endTitleFont = new Font("Monospaced", Font.BOLD, 26);

    final Color ground = new Color(186, 214, 156);
    final Color textGreen = new Color(52, 83, 59);
    final Color correctGreen = new Color(36, 110, 52);
    final Color wrongRed = new Color(150, 48, 36);
    final Color boxFill = new Color(232, 224, 201);
    final Color buttonFill = new Color(245, 225, 165);
    final Color shade = new Color(40, 55, 35, 110);

    static final int END_W = 460;
    static final int END_H = 300;
    static final int BTN_W = 170;
    static final int BTN_H = 46;

    // Drawn directly, instead of asking Swing to repaint. That queue was the stutter.
    final Canvas canvas = new Canvas();
    String hudLine = "Hearts: 3    Score: 0";
    int shownHearts = 3;
    int shownScore = 0;

    static final double[] PORTAL_OFFSETS = {0.0, 0.2, 0.45};

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    Player player;

    Question[] questions;
    AnswerPortal[] portals = new AnswerPortal[0];
    int stage = 0;
    int score = 0;
    int hearts = startingHearts;
    String message = "";
    boolean gameWon = false;
    boolean gameOver = false;

    // The portal the player just stepped in, while its result animation plays.
    AnswerPortal chosenPortal = null;
    boolean playingResult = false;

    // Remembers R from the last frame so holding the key restarts only once.
    boolean restartKeyWasDown = false;
    volatile boolean running = true;
    volatile boolean leaveToMenu = false;
    volatile boolean restartRequested = false;

    final ByteBound app;
    final BufferedImage winImage;
    final BufferedImage loseImage;
    String endTitle = "";
    String endSubtitleLine = "";
    String endScoreLine = "";

public GamePanel(String selectedCharacter, int width, int height, ByteBound app) {

        this.app = app;
        winImage = loadEndImage("/icon/win_chest.png");
        loseImage = loadEndImage("/icon/lose_gravestone.png");

        screenWidth = width;
        screenHeight = height;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(ground);

        canvas.setPreferredSize(new Dimension(screenWidth, screenHeight));
        canvas.setBackground(ground);
        canvas.setIgnoreRepaint(true);
        canvas.setFocusable(true);
        canvas.addKeyListener(keyH);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clickEndPanel(e.getX(), e.getY());
            }
        });
        add(canvas, BorderLayout.CENTER);

        player = new Player(this, keyH, selectedCharacter);
        questions = QuestionBank.load();
        startQuiz();
}

@Override
public boolean requestFocusInWindow() {
    return canvas.requestFocusInWindow();
}

public void startGameThread() {
    gameThread = new Thread(this, "ByteBound");
    gameThread.start();
}

public void stopLoop() {
    running = false;
    Thread thread = gameThread;
    if (thread != null && thread != Thread.currentThread()) {
        try {
            thread.join(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

@Override
public void run() {
    while (running && !canvas.isDisplayable()) {
        LockSupport.parkNanos(10_000_000L);
    }
    if (!running) {
        return;
    }

    canvas.createBufferStrategy(2);
    long frameNanos = 1_000_000_000L / FPS;

    while (running && !leaveToMenu) {
        long frameStart = System.nanoTime();
        syncCanvasSize();
        update();
        render();
        waitForNextFrame(frameStart, frameNanos);
    }

    if (leaveToMenu) {
        SwingUtilities.invokeLater(() -> app.backToTitle(this));
    }
}

// Sleep most of the leftover time, then wait out the last fraction so frames stay even.
private void waitForNextFrame(long frameStart, long frameNanos) {
    long frameEnd = frameStart + frameNanos;
    long remaining = frameEnd - System.nanoTime();
    if (remaining > 1_500_000L) {
        LockSupport.parkNanos(remaining - 800_000L);
    }
    while (System.nanoTime() < frameEnd) {
        Thread.onSpinWait();
    }
}

private void syncCanvasSize() {
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    if (width <= 0 || height <= 0 || (width == screenWidth && height == screenHeight)) {
        return;
    }
    screenWidth = width;
    screenHeight = height;
    if (!playingResult && !gameWon && !gameOver && questions != null && stage < questions.length) {
        loadStage();
    }
}

private void render() {
    BufferStrategy strategy = canvas.getBufferStrategy();
    if (strategy == null) {
        return;
    }

    do {
        do {
            Graphics2D g2 = (Graphics2D) strategy.getDrawGraphics();
            try {
                g2.setColor(ground);
                g2.fillRect(0, 0, screenWidth, screenHeight);
                drawFrame(g2);
            } finally {
                g2.dispose();
            }
        } while (strategy.contentsRestored());

        strategy.show();
        Toolkit.getDefaultToolkit().sync();
    } while (strategy.contentsLost());
    }
    public void update() {
        if (restartRequested) {
            restartRequested = false;
            startQuiz();
        }

        // R only works after the quiz is finished, and only once per press.
        if ((gameWon || gameOver) && keyH.rPressed && !restartKeyWasDown) {
            startQuiz();
        }
        restartKeyWasDown = keyH.rPressed;

        // 1/60 of a second. PortalSprite uses this to pick the next frame.
        double dt = 1.0 / FPS;
        for (int i = 0; i < portals.length; i++) {
            portals[i].sprite.update(dt);
        }

        // Stay in the portal until the correct or wrong row has played.
        if (playingResult) {
            if (chosenPortal.sprite.finished()) {
                finishAnswer();
            }
            return;
        }

        player.update();

        if (!gameWon && !gameOver) {
            checkPortals();
        }
    }

    // Puts the quiz back to question 1 and walks the player to the left.
    private void startQuiz() {
        stage = 0;
        score = 0;
        hearts = startingHearts;
        message = "";
        gameWon = false;
        gameOver = false;
        playingResult = false;
        chosenPortal = null;
        fitToPanel();
        placePlayerAtStart();
    }

    // Copies the panel's real width and height, then rebuilds the portals to fit.
    private void fitToPanel() {
        if (getWidth() > 0 && getHeight() > 0) {
            screenWidth = getWidth();
            screenHeight = getHeight();
        }

        if (screenWidth <= 0 || screenHeight <= 0 || questions == null) {
            return;
        }

        if (!playingResult && !gameWon && !gameOver && stage < questions.length) {
            loadStage();
        }
    }

    // Left side of the screen, clear of every portal, facing the answers.
    private void placePlayerAtStart() {
        player.x = 48;
        player.y = Math.max(48, screenHeight / 2 - playerSize / 2);
        player.direction = "right";
    }

    // Builds the three portals so they sit fully on the right side of this panel.
    // Each frame is 80 x 40. Scale picks how many times bigger to draw it.
    private void loadStage() {
        Question question = questions[stage];
        int count = question.answers.length;
        portals = new AnswerPortal[count];

        int margin = 40;
        int topSpace = 120;
        int bottomSpace = 16;
        int labelSpace = 22;
        int gap = 14;
        int available = screenHeight - topSpace - bottomSpace;

        int portalScale = 4;
        while (portalScale > 1) {
            int portalH = 40 * portalScale;
            int portalW = 80 * portalScale;
            int needed = count * (labelSpace + portalH) + (count - 1) * gap;
            if (needed <= available && portalW + margin < screenWidth / 2) {
                break;
            }
            portalScale--;
        }

        int portalW = 80 * portalScale;
        int portalH = 40 * portalScale;
        int portalX = screenWidth - portalW - margin;
        int portalY = topSpace + labelSpace;
        FontRenderContext text = new FontRenderContext(null, false, false);
        for (int i = 0; i < count; i++) {
            AnswerPortal portal = new AnswerPortal();
            portal.answer = question.answers[i];
            portal.correct = (i == question.correctIndex);
            portal.sprite = new PortalSprite(
                    portalX, portalY, portalScale, PORTAL_OFFSETS[i % PORTAL_OFFSETS.length]);
            int textWidth = (int) answerFont.getStringBounds(portal.answer, text).getWidth();
            portal.labelX = portalX + (portalW - textWidth) / 2;
            portal.labelY = portalY - 6;
            portals[i] = portal;
            portalY += portalH + labelSpace + gap;
        }
    }

    // Stepping into a portal starts row 1 (correct) or row 2 (wrong).
    // Score and the walk back happen after that row finishes, in finishAnswer.
    private void checkPortals() {
        AnswerPortal steppedPortal = null;

        for (int i = 0; i < portals.length; i++) {
            if (feetTouch(portals[i].sprite)) {
                steppedPortal = portals[i];
                break;
            }
        }

        if (steppedPortal == null) {
            return;
        }

        chosenPortal = steppedPortal;
        if (steppedPortal.correct) {
            chosenPortal.sprite.setState(PortalSprite.State.CORRECT);
        } else {
            chosenPortal.sprite.setState(PortalSprite.State.WRONG);
        }
        playingResult = true;
    }

    // The character is tall, so only the feet can choose a portal.
    private boolean feetTouch(PortalSprite portal) {
        int feetW = playerSize / 4;
        int feetH = playerSize / 6;
        int feetX = player.x + (playerSize - feetW) / 2;
        int feetY = player.y + playerSize - feetH - (playerSize / 10);
        return feetX < portal.getX() + portal.getWidth()
                && feetX + feetW > portal.getX()
                && feetY < portal.getY() + portal.getHeight()
                && feetY + feetH > portal.getY();
    }

    private void finishAnswer() {
        playingResult = false;

        if (chosenPortal.correct) {
            score += pointsPerCorrect;
            message = "Correct! +" + pointsPerCorrect;
            stage++;

            if (stage >= questions.length) {
                gameWon = true;
                endTitle = "CONGRATULATIONS!";
                endSubtitleLine = "All questions complete!";
                endScoreLine = "Final score: " + score;
            } else {
                loadStage();
            }
        } else {
            hearts--;
            message = "Wrong! -1 heart";

            if (hearts <= 0) {
                hearts = 0;
                gameOver = true;
                endTitle = "GAME OVER";
                endSubtitleLine = "You ran out of hearts.";
                endScoreLine = "Final score: " + score;
            } else {
                // Same question stays. Put every portal back on the idle row.
                for (int i = 0; i < portals.length; i++) {
                    portals[i].sprite.setState(PortalSprite.State.IDLE);
                }
            }
        }

        chosenPortal = null;
        placePlayerAtStart();
    }

    private void drawFrame(Graphics2D g2) {
        g2.setFont(answerFont);
        g2.setColor(textGreen);
        for (int i = 0; i < portals.length; i++) {
            g2.drawString(portals[i].answer, portals[i].labelX, portals[i].labelY);
            portals[i].sprite.draw(g2);
        }

        player.draw(g2);
        drawHud(g2);
    }

    private void drawHud(Graphics2D g2) {
        if (hearts != shownHearts || score != shownScore) {
            shownHearts = hearts;
            shownScore = score;
            hudLine = "Hearts: " + hearts + "    Score: " + score;
        }
        g2.setColor(textGreen);
        g2.setFont(hudFont);
        g2.drawString(hudLine, 24, 40);

        if (!gameWon && !gameOver && stage < questions.length) {
            g2.setFont(messageFont);
            drawCentered(g2, questions[stage].prompt, 78);
        }

        if (!message.isEmpty() && !gameWon && !gameOver) {
            g2.setColor(message.startsWith("Correct") ? correctGreen : wrongRed);
            g2.setFont(messageFont);
            drawCentered(g2, message, 112);
        }

        if (gameWon || gameOver) {
            drawEndPanel(g2);
        }
    }

    // Floating card, same colors as the character select screen.
    private void drawEndPanel(Graphics2D g2) {
        int x = endX();
        int y = endY();

        g2.setColor(shade);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(boxFill);
        g2.fillRoundRect(x, y, END_W, END_H, 18, 18);
        g2.setColor(textGreen);
        g2.drawRoundRect(x, y, END_W, END_H, 18, 18);

        g2.drawImage(gameWon ? winImage : loseImage,
                x + 24, y + 44, 120, 120, null);

        int textX = x + 154;
        int textWidth = END_W - 174;
        g2.setFont(endTitleFont);
        drawCenteredInArea(g2, endTitle, textX, textWidth, y + 78);
        g2.setFont(messageFont);
        drawCenteredInArea(g2, endSubtitleLine, textX, textWidth, y + 121);
        drawCenteredInArea(g2, endScoreLine, textX, textWidth, y + 158);

        drawButton(g2, againX(), buttonY(), "PLAY AGAIN");
        drawButton(g2, menuX(), buttonY(), "MAIN MENU");
    }

    private BufferedImage loadEndImage(String path) {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Cannot find image: " + path);
            }
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IOException("Invalid image: " + path);
            }
            return Images.scaled(image, 120, 120);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load " + path, e);
        }
    }

    private void drawButton(Graphics2D g2, int x, int y, String label) {
        g2.setColor(buttonFill);
        g2.fillRoundRect(x, y, BTN_W, BTN_H, 12, 12);
        g2.setColor(textGreen);
        g2.drawRoundRect(x, y, BTN_W, BTN_H, 12, 12);
        g2.setFont(messageFont);
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (BTN_W - metrics.stringWidth(label)) / 2;
        int textY = y + (BTN_H + metrics.getAscent() - metrics.getDescent()) / 2;
        g2.drawString(label, textX, textY);
    }

    private void clickEndPanel(int x, int y) {
        if (!gameWon && !gameOver) {
            return;
        }
        if (inside(x, y, againX(), buttonY(), BTN_W, BTN_H)) {
            restartRequested = true;
        } else if (inside(x, y, menuX(), buttonY(), BTN_W, BTN_H)) {
            leaveToMenu = true;
        }
    }

    private int endX() {
        return (screenWidth - END_W) / 2;
    }

    private int endY() {
        return (screenHeight - END_H) / 2;
    }

    private int buttonY() {
        return endY() + END_H - BTN_H - 12;
    }

    private int againX() {
        return endX() + 40;
    }

    private int menuX() {
        return endX() + END_W - 40 - BTN_W;
    }

    private boolean inside(int px, int py, int x, int y, int w, int h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    private void drawCentered(Graphics2D g2, String text, int y) {
        FontMetrics metrics = g2.getFontMetrics();
        int x = (screenWidth - metrics.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    private void drawCenteredInArea(Graphics2D g2, String text, int left, int width, int y) {
        FontMetrics metrics = g2.getFontMetrics();
        int x = left + (width - metrics.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }
}

// One answer choice: the animated portal, the words above it, and whether it is right.
class AnswerPortal {
    PortalSprite sprite;
    String answer;
    boolean correct;
    int labelX;
    int labelY;
}
