import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Player extends Entity {
    
    GamePanel gp;
    KeyHandler keyH;
    String selectedCharacter;
    boolean moving = false;

    public Player(GamePanel gp, KeyHandler keyH, String selectedCharacter) {

    this.gp = gp;
    this.keyH = keyH;
    this.selectedCharacter = selectedCharacter;

    setDefaultValues();
    getPlayerImage();
    }
    
    public void setDefaultValues() {
        x = 100;
        y = 100;
        speed = 4;
        direction = "down";
    }

    public void getPlayerImage() {

        try {

        if (selectedCharacter.equals("Boy")) {

            // BOY SPRITES

            upIdle1 = loadSprite("/charSprites/bs upIdle1.png");
            upIdle2 = loadSprite("/charSprites/bs upIdle2.png");

            up1 = loadSprite("/charSprites/sprite_r4_c1.png");
            up2 = loadSprite("/charSprites/sprite_r4_c2.png");

            downIdle1 = loadSprite("/charSprites/bs downIdle1.png");
            downIdle2 = loadSprite("/charSprites/bs downIdle2.png");

            down1 = loadSprite("/charSprites/sprite_r1_c1.png");
            down2 = loadSprite("/charSprites/sprite_r1_c2.png");

            leftIdle1 = loadSprite("/charSprites/bs leftIdle1.png");
            leftIdle2 = loadSprite("/charSprites/bs leftIdle2.png");

            left1 = loadSprite("/charSprites/sprite_r2_c3.png");
            left2 = loadSprite("/charSprites/sprite_r2_c4.png");

            rightIdle1 = loadSprite("/charSprites/bs rightIdle1.png");
            rightIdle2 = loadSprite("/charSprites/bs rightIdle2.png");

            right1 = loadSprite("/charSprites/sprite_r3_c3.png");
            right2 = loadSprite("/charSprites/sprite_r3_c4.png");

        } else {

            // GIRL SPRITES
            upIdle1 = loadSprite("/charSprites/gs upIdle1.png");
            upIdle2 = loadSprite("/charSprites/gs upIdle2.png");

            up1 = loadSprite("/charSprites/gs up1.png");
            up2 = loadSprite("/charSprites/gs up2.png");

            downIdle1 = loadSprite("/charSprites/gs downIdle1.png");
            downIdle2 = loadSprite("/charSprites/gs downIdle2.png");

            down1 = loadSprite("/charSprites/gs down1.png");
            down2 = loadSprite("/charSprites/gs down2.png");

            leftIdle1 = loadSprite("/charSprites/gs leftIdle1.png");
            leftIdle2 = loadSprite("/charSprites/gs leftIdle2.png");

            left1 = loadSprite("/charSprites/gs left1.png");
            left2 = loadSprite("/charSprites/gs left2.png");

            rightIdle1 = loadSprite("/charSprites/gs rightIdle1.png");
            rightIdle2 = loadSprite("/charSprites/gs rightIdle2.png");

            right1 = loadSprite("/charSprites/gs right1.png");
            right2 = loadSprite("/charSprites/gs right2.png");
        }

    } catch (IOException e) {
        throw new IllegalStateException("Could not load sprites for " + selectedCharacter, e);
    }
}

    // Scale the sprite once here. draw() then copies it, instead of resizing every frame.
    private BufferedImage loadSprite(String path) throws IOException {
        try (var input = getClass().getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Missing image: " + path);
            }
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IOException("Invalid image: " + path);
            }
            return Images.scaled(image, 120, 120);
        }
    }
    // UPDATE
    
    public void update() {
        moving = false;

        if (keyH.upPressed == true) {
            direction = "up";
            y -= speed;
            moving = true;
        }
        else if (keyH.downPressed == true) {
            direction = "down";
            y += speed;
            moving = true;
        }
        else if (keyH.leftPressed == true) {
            direction = "left";
            x -= speed;
            moving = true;
        }
        else if (keyH.rightPressed == true) {
            direction = "right";
            x += speed;
            moving = true;
        }

    // KEEP PLAYER INSIDE THE WINDOW
    int playerWidth = 120;
    int playerHeight = 120;

    int maxX = gp.getWidth() - playerWidth;
    int maxY = gp.getHeight() - playerHeight;

    x = Math.max(0, Math.min(x, maxX));
    y = Math.max(0, Math.min(y, maxY));

    // PLAYER ANIMATION COUNTER
    spriteCounter++;

        if (spriteCounter > (moving ? 10 : 20)) {
            spriteNum = (spriteNum == 1) ? 2 : 1;
            spriteCounter = 0;
        }
}

// DRAW
    
public void draw(Graphics2D g2) {

    BufferedImage image = null;

    if (moving == true) {

        // WALKING ANIMATION
        switch (direction) {
            case "up":
                image = (spriteNum == 1) ? up1 : up2;
                break;

            case "down":
                image = (spriteNum == 1) ? down1 : down2;
                break;

            case "left":
                image = (spriteNum == 1) ? left1 : left2;
                break;

            case "right":
                image = (spriteNum == 1) ? right1 : right2;
                break;
        }

    } else {

        // IDLE ANIMATION
        switch (direction) {
            case "up":
                image = (spriteNum == 1) ? upIdle1 : upIdle2;
                break;

            case "down":
                image = (spriteNum == 1) ? downIdle1 : downIdle2;
                break;

            case "left":
                image = (spriteNum == 1) ? leftIdle1 : leftIdle2;
                break;

            case "right":
                image = (spriteNum == 1) ? rightIdle1 : rightIdle2;
                break;
        }
    }

    if (image != null) {
        g2.drawImage(image, x, y, null);
    }
}
}
