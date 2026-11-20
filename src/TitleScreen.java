
import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class TitleScreen extends JPanel {

    private Image backgroundImage;
    private ByteBound main;

    public TitleScreen(ByteBound main) {
        this.main = main;

        // LOAD BACKGROUND IMAGE
        var background = getClass().getResource("/titlescreen bg.jpg");
        if (background == null) {
            throw new IllegalStateException("Missing image: /titlescreen bg.jpg");
        }
        try {
            backgroundImage = ImageIO.read(background);
            if (backgroundImage == null) {
                throw new IOException("Invalid image: /titlescreen bg.jpg");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load /titlescreen bg.jpg", e);
        }

        setLayout(new GridBagLayout());
        setBackground(new Color(65, 110, 70));

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);

        // LOAD STARDEW VALLEY FONT
        JLabel title = new JLabel("BYTEBOUND");
        title.setFont(new Font("Monospaced", Font.BOLD, 120));
        title.setForeground(new Color(255, 235, 180));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("A little world of JAVA adventures!");
        subtitle.setFont(new Font("Monospaced", Font.BOLD, 20));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = createButton("START GAME");
        JButton exitButton = createButton("EXIT GAME");

        startButton.addActionListener(e -> main.startGame());
        exitButton.addActionListener(e -> System.exit(0));

        menu.add(title);
        menu.add(Box.createVerticalStrut(10));
        menu.add(subtitle);
        menu.add(Box.createVerticalStrut(45));
        menu.add(startButton);
        menu.add(Box.createVerticalStrut(30));
        menu.add(exitButton);

        add(menu);
    }

    // DRAW BACKGROUND IMAGE
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(
                backgroundImage,
                0, 0,
                getWidth(), getHeight(),
                this
            );
        }
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);

        // USE STARDEW VALLEY FONT ON BUTTONS
        button.setFont(new Font("Monospaced", Font.BOLD, 30));
        button.setForeground(new Color(55, 75, 45));
        button.setBackground(new Color(245, 225, 165));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 45));
        button.setPreferredSize(new Dimension(300, 45));

        return button;
    }
}
