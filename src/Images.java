import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

// A picture loaded with ImageIO is not in the screen's format.
// Copying it once makes later draws a fast blit instead of a conversion every frame.
final class Images {

    private Images() {
    }

    static BufferedImage forScreen(BufferedImage source) {
        if (source == null) {
            return null;
        }
        return copy(source, source.getWidth(), source.getHeight());
    }

    // Nearest-neighbor keeps pixel art sharp. Done once, not every frame.
    static BufferedImage scaled(BufferedImage source, int width, int height) {
        if (source == null) {
            return null;
        }
        BufferedImage image = screenConfig().createCompatibleImage(
                width, height, Transparency.TRANSLUCENT);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return image;
    }

    private static BufferedImage copy(BufferedImage source, int width, int height) {
        BufferedImage image = screenConfig().createCompatibleImage(
                width, height, Transparency.TRANSLUCENT);
        Graphics2D g = image.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return image;
    }

    private static GraphicsConfiguration screenConfig() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
    }
}
