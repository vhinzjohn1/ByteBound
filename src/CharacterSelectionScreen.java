
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class CharacterSelectionScreen extends JPanel {

    private ByteBound game;

    private JPanel girlCard;
    private JPanel boyCard;
    private JLabel girlImageLabel;
    private JLabel boyImageLabel;
    private ImageIcon[] girlIdleFrames;
    private ImageIcon[] boyIdleFrames;
    private Timer idleAnimationTimer;
    private int idleFrame;

    private String selectedCharacter = "Girl";

    private final Color backgroundColor =
            new Color(232, 224, 201);

    private final Color cardColor =
            new Color(198, 214, 178);

    private final Color selectedColor =
            new Color(67, 112, 70);

    public CharacterSelectionScreen(ByteBound game) {

        this.game = game;

        setLayout(new BorderLayout(10, 20));
        setBackground(backgroundColor);
        setBorder(BorderFactory.createEmptyBorder(
                35, 40, 30, 40));

        // TITLE
        JLabel title = new JLabel(
                "CHOOSE YOUR CHARACTER",
                SwingConstants.CENTER);

        title.setFont(new Font(
                "Monospaced", Font.BOLD, 30));

        title.setForeground(new Color(52, 83, 59));

        add(title, BorderLayout.NORTH);

        // CHARACTER CARDS
        JPanel characters = new JPanel(
                new GridLayout(1, 2, 30, 0));

        characters.setOpaque(false);

        girlIdleFrames = loadIdleFrames(
                "/charSprites/gs downIdle1.png",
                "/charSprites/gs downIdle2.png");
        boyIdleFrames = loadIdleFrames(
                "/charSprites/bs downIdle1.png",
                "/charSprites/bs downIdle2.png");

        girlCard = createCharacterCard("Girl", girlIdleFrames);
        boyCard = createCharacterCard("Boy", boyIdleFrames);

        characters.add(girlCard);
        characters.add(boyCard);

        add(characters, BorderLayout.CENTER);

        // BOTTOM MENU
        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 20, 5));

        bottom.setOpaque(false);

        JButton playButton = createButton("CONFIRM");
        JButton backButton = createButton("BACK");

        playButton.addActionListener(e -> game.startGame(selectedCharacter));

        backButton.addActionListener(e -> game.showTitleScreen());

        bottom.add(playButton);
        bottom.add(backButton);

        add(bottom, BorderLayout.SOUTH);

        updateSelection();

        idleAnimationTimer = new Timer(350, e -> {
            idleFrame = 1 - idleFrame;
            showIdleFrame(girlImageLabel, girlIdleFrames, idleFrame);
            showIdleFrame(boyImageLabel, boyIdleFrames, idleFrame);
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                idleAnimationTimer.start();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                idleAnimationTimer.stop();
            }
        });
    }

    private JPanel createCharacterCard(
            String characterName, ImageIcon[] idleFrames) {

        JPanel card = new JPanel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBackground(cardColor);

        card.setBorder(new LineBorder(backgroundColor, 5));

        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (idleFrames == null) {
            imageLabel.setText("Sprite not found");
        } else {
            imageLabel.setIcon(idleFrames[0]);
        }

        if (characterName.equals("Girl")) {
            girlImageLabel = imageLabel;
        } else {
            boyImageLabel = imageLabel;
        }

        JLabel nameLabel = new JLabel(characterName);

        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 22));

        nameLabel.setForeground(new Color(52, 83, 59));

        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(nameLabel);
        card.add(Box.createVerticalGlue());

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedCharacter = characterName;
                updateSelection();
            }
        });

        return card;
    }

    private ImageIcon[] loadIdleFrames(String firstPath, String secondPath) {
        try {
            return new ImageIcon[] {
                    loadSpriteIcon(firstPath),
                    loadSpriteIcon(secondPath)
            };
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    private ImageIcon loadSpriteIcon(String path) throws IOException {
        try (var resource = getClass().getResourceAsStream(path)) {
            if (resource == null) {
                throw new IOException("Cannot find image: " + path);
            }

            BufferedImage image = ImageIO.read(resource);
            if (image == null) {
                throw new IOException("Invalid image: " + path);
            }

            return new ImageIcon(Images.scaled(image, 200, 200));
        }
    }

    private void showIdleFrame(JLabel label, ImageIcon[] frames, int frame) {
        if (label != null && frames != null) {
            label.setIcon(frames[frame]);
        }
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Monospaced", Font.BOLD, 22));

        button.setBackground(new Color(245, 225, 165));
        button.setForeground(new Color(52, 83, 59));
        button.setFocusPainted(false);

        return button;
    }

    private void updateSelection() {

        girlCard.setBorder(new LineBorder(
                selectedCharacter.equals("Girl") ? selectedColor : backgroundColor, 5));

        boyCard.setBorder(new LineBorder(
                selectedCharacter.equals("Boy") ? selectedColor : backgroundColor, 5));

        repaint();
    }
}
