import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Animated portal for answer choices.
 * Sheet: portalSprites/portal_sheet.png (640 x 120) -> 8 columns x 3 rows, each frame 80 x 40.
 * Row 0 = IDLE (loops), Row 1 = CORRECT (plays once, holds), Row 2 = WRONG (plays once, holds).
 */
public class PortalSprite {
    public enum State { IDLE, CORRECT, WRONG }

    private static final int FRAME_W = 80, FRAME_H = 40, FRAMES = 8;
    private static final double FPS = 8.0;

    private static BufferedImage sheet; // shared by all portals

    // Frames already scaled for the screen. Built once per scale, then just blitted.
    private static int preparedScale = -1;
    private static BufferedImage[][] preparedFrames;

    private final int x, y, scale;
    private final BufferedImage[][] frames;
    private State state = State.IDLE;
    private double time = 0;            // seconds in current state
    private final double offset;        // desync idle animations between portals

    public PortalSprite(int x, int y, int scale, double idleOffsetSeconds) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.offset = idleOffsetSeconds;
        if (sheet == null) {
            try (InputStream in = PortalSprite.class.getResourceAsStream("/portalSprites/portal_sheet.png")) {
                if (in == null) {
                    throw new IOException("Missing /portalSprites/portal_sheet.png");
                }
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    throw new IOException("Invalid /portalSprites/portal_sheet.png");
                }
                sheet = Images.forScreen(image);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Could not load /portalSprites/portal_sheet.png", e);
            }
        }
        // Build the scaled frames now, so the first painted frame is not a hitch.
        frames = framesFor(scale);
    }

    public void setState(State s) {
        if (s != state) {
            state = s;
            time = 0;
        }
    }

    public State getState() {
        return state;
    }

    /** Call every game tick with seconds since the last tick (e.g. 1/60.0). */
    public void update(double dt) {
        time += dt;
    }

    /** True once CORRECT / WRONG has played all 8 frames. */
    public boolean finished() {
        return state != State.IDLE && time * FPS >= FRAMES;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return FRAME_W * scale;
    }

    public int getHeight() {
        return FRAME_H * scale;
    }

    /** Bounding box, handy for collision with the player. */
    public Rectangle bounds() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    public void draw(Graphics2D g) {
        int frame;
        if (state == State.IDLE) {
            frame = (int) ((time + offset) * FPS) % FRAMES;
        } else {
            frame = Math.min(FRAMES - 1, (int) (time * FPS));
        }

        // The frame was scaled when the portal was created, so this draw does not resize it.
        g.drawImage(frames[state.ordinal()][frame], x, y, null);
    }

    // Cuts the sheet into 8 x 3 frames at this scale. All portals share the result.
    private static BufferedImage[][] framesFor(int scale) {
        if (preparedScale == scale && preparedFrames != null) {
            return preparedFrames;
        }

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
        int drawW = FRAME_W * scale;
        int drawH = FRAME_H * scale;
        BufferedImage[][] frames = new BufferedImage[State.values().length][FRAMES];

        for (int row = 0; row < frames.length; row++) {
            for (int col = 0; col < FRAMES; col++) {
                BufferedImage frame = gc.createCompatibleImage(drawW, drawH, Transparency.TRANSLUCENT);
                Graphics2D g = frame.createGraphics();
                g.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int sx = col * FRAME_W;
                int sy = row * FRAME_H;
                g.drawImage(sheet, 0, 0, drawW, drawH, sx, sy, sx + FRAME_W, sy + FRAME_H, null);
                g.dispose();
                frames[row][col] = frame;
            }
        }

        preparedFrames = frames;
        preparedScale = scale;
        return frames;
    }
}
