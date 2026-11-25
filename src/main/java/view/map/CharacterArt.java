package view.map;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * The bundled CC0 character images ("32-pixel Human Sprites" by cyanowl, see
 * {@code /characters/CREDITS.md}), loaded once and converted to ARGB so they can be read pixel by pixel.
 */
public final class CharacterArt {
    private static final String FOLDER = "/characters/";
    private static CharacterArt instance;

    private final BufferedImage body;
    private final BufferedImage hair;
    private final BufferedImage hoodie;
    private final BufferedImage jeans;
    private final BufferedImage shoes;
    private final BufferedImage sideWalk;

    private CharacterArt() {
        body = load(FOLDER + "human_sprites.png");
        hair = load(FOLDER + "human_sprites_hair.png");
        hoodie = load(FOLDER + "human_sprites_0_HOODIE.gif");
        jeans = load(FOLDER + "human_sprites_0_JEANS.gif");
        shoes = load(FOLDER + "human_sprites_0_SHOES.gif");
        sideWalk = load(FOLDER + "Human_Sprites_Smooth_SideWalking_strip8.png");
    }

    /** The shared, lazily loaded art. */
    public static synchronized CharacterArt get() {
        if (instance == null) {
            instance = new CharacterArt();
        }
        return instance;
    }

    /**
     * Loads one bundled image as ARGB.
     * @throws IllegalStateException naming the resource if it is missing or unreadable
     */
    static BufferedImage load(String path) {
        try (InputStream stream = CharacterArt.class.getResourceAsStream(path)) {
            BufferedImage image = stream == null ? null : ImageIO.read(stream);
            if (image == null) {
                throw new IllegalStateException("Missing character art: " + path);
            }
            BufferedImage argb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = argb.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return argb;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read character art: " + path, e);
        }
    }

    public BufferedImage getBody() {
        return body;
    }

    public BufferedImage getHair() {
        return hair;
    }

    public BufferedImage getHoodie() {
        return hoodie;
    }

    public BufferedImage getJeans() {
        return jeans;
    }

    public BufferedImage getShoes() {
        return shoes;
    }

    public BufferedImage getSideWalk() {
        return sideWalk;
    }
}
