import java.io.BufferedInputStream;
import java.io.InputStream;

// Plays Background_Music.mp3 from the start of the app and starts it again when it ends.
public final class Music {

    private Music() {
    }

    public static void startLoop() {
        Thread thread = new Thread(Music::loop, "music");
        thread.setDaemon(true);
        thread.start();
    }

    private static void loop() {
        while (true) {
            try (InputStream in = Music.class.getResourceAsStream("/music/Background_Music.mp3")) {
                if (in == null) {
                    System.out.println("Missing /music/Background_Music.mp3");
                    return;
                }
                javazoom.jl.player.Player player =
                        new javazoom.jl.player.Player(new BufferedInputStream(in));
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
