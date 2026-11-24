package view.map;

import java.awt.Color;
import java.util.Objects;

/**
 * What one character looks like: skin tone, hair, clothing colours and optional backpack and cap.
 * The player has a fixed look; campus students get a look chosen from a number (their position).
 */
public final class CharacterLook {
    /** Hair styles available in the bundled hair sheet. */
    public enum HairStyle {
        SHORT(3), LONG(4), WAVY(4);

        private final int colours;

        HairStyle(int colours) {
            this.colours = colours;
        }

        /** How many hair colours the sheet has for this style. */
        public int colours() {
            return colours;
        }
    }

    public static final int SKIN_TONES = 5;
    public static final Color UOFT_BLUE = new Color(0x2A, 0x4C, 0x9A);
    private static final Color[] HOODIES = {UOFT_BLUE, new Color(0xB8, 0x3C, 0x3A), new Color(0x3E, 0x7E, 0x4C),
            new Color(0x3A, 0x3A, 0x42), new Color(0xC8, 0xC4, 0xBC), new Color(0xD8, 0x9A, 0x3A),
            new Color(0x7A, 0x56, 0xA0), new Color(0x4A, 0x8C, 0xB4)};
    private static final Color[] TROUSERS = {new Color(0x4A, 0x5C, 0x8E), new Color(0x2E, 0x34, 0x4A),
            new Color(0x6E, 0x5A, 0x48), new Color(0x5C, 0x5E, 0x64)};
    private static final Color[] SHOES = {new Color(0xEA, 0xEA, 0xEA), new Color(0x3A, 0x34, 0x34),
            new Color(0xB4, 0x4A, 0x3A)};
    private static final Color[] PACKS = {new Color(0x3A, 0x3A, 0x42), new Color(0xB0, 0x3A, 0x34),
            new Color(0x4A, 0x6A, 0x3E), new Color(0xD8, 0xB0, 0x4A)};
    private static final Color[] CAPS = {UOFT_BLUE, new Color(0x2E, 0x2E, 0x34), new Color(0xC8, 0x4A, 0x3A)};

    private final int skinTone;
    private final HairStyle hairStyle;
    private final int hairColour;
    private final Color hoodie;
    private final Color trousers;
    private final Color shoes;
    private final Color backpack;
    private final Color cap;

    public CharacterLook(int skinTone, HairStyle hairStyle, int hairColour, Color hoodie, Color trousers,
                         Color shoes, Color backpack, Color cap) {
        this.skinTone = skinTone;
        this.hairStyle = hairStyle;
        this.hairColour = hairColour;
        this.hoodie = hoodie;
        this.trousers = trousers;
        this.shoes = shoes;
        this.backpack = backpack;
        this.cap = cap;
    }

    /** The player: short dark hair, University of Toronto blue hoodie, jeans, white trainers, backpack. */
    public static CharacterLook player() {
        return new CharacterLook(0, HairStyle.SHORT, 2, UOFT_BLUE, TROUSERS[0], SHOES[0], PACKS[0], null);
    }

    /** A student look chosen from a number; the same number always gives the same look. */
    public static CharacterLook forNumber(int number) {
        HairStyle[] styles = HairStyle.values();
        HairStyle style = styles[ArtHash.of(number, 0, 200) % styles.length];
        Color backpack = ArtHash.of(number, 0, 207) % 5 < 2 ? PACKS[ArtHash.of(number, 0, 208) % PACKS.length] : null;
        Color cap = ArtHash.of(number, 0, 209) % 4 == 0 ? CAPS[ArtHash.of(number, 0, 210) % CAPS.length] : null;
        return new CharacterLook(
                ArtHash.of(number, 0, 201) % SKIN_TONES,
                style,
                ArtHash.of(number, 0, 202) % style.colours(),
                HOODIES[ArtHash.of(number, 0, 203) % HOODIES.length],
                TROUSERS[ArtHash.of(number, 0, 204) % TROUSERS.length],
                SHOES[ArtHash.of(number, 0, 205) % SHOES.length],
                backpack,
                cap);
    }

    public int getSkinTone() {
        return skinTone;
    }

    public HairStyle getHairStyle() {
        return hairStyle;
    }

    public int getHairColour() {
        return hairColour;
    }

    public Color getHoodie() {
        return hoodie;
    }

    public Color getTrousers() {
        return trousers;
    }

    public Color getShoes() {
        return shoes;
    }

    /** Backpack colour, or null for none. */
    public Color getBackpack() {
        return backpack;
    }

    /** Cap colour, or null for none. */
    public Color getCap() {
        return cap;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CharacterLook)) {
            return false;
        }
        CharacterLook other = (CharacterLook) o;
        return skinTone == other.skinTone && hairStyle == other.hairStyle && hairColour == other.hairColour
                && hoodie.equals(other.hoodie) && trousers.equals(other.trousers) && shoes.equals(other.shoes)
                && Objects.equals(backpack, other.backpack) && Objects.equals(cap, other.cap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skinTone, hairStyle, hairColour, hoodie, trousers, shoes, backpack, cap);
    }
}
