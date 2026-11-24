package view.map;

import java.awt.Color;

/**
 * How a campus building looks. Each style follows the one-line description of a St. George building
 * in the project's reference notes ({@code tools/art-source/README.md} and the street photos);
 * {@link BuildingPainter} turns it into pixels.
 */
public enum BuildingStyle {
    /** Innis Residence: red brick in stepped volumes, dark glazed bays, pale stone arched entrance. */
    RED_BRICK_BAYS(spec(Texture.BRICK, 0xA8523E, 0x843C30).windows(Windows.BAYS).roof(Roof.FLAT, 0x8A847E)
            .entrance(Entrance.STONE_ARCH).trim(0xE2D6BE)),
    /** Claude T. Bissell: brutalist precast concrete, deep-set windows, cantilevered upper floors. */
    PRECAST_CANTILEVER(spec(Texture.PANELS, 0xB8B2A6, 0x8E887E).windows(Windows.DEEP_SET).roof(Roof.FLAT, 0x9C968C)
            .entrance(Entrance.GLASS_CANOPY).trim(0x8E887E).cantilever()),
    /** Rotman: glass building with dense white vertical fins. */
    GLASS_FINS(spec(Texture.GLASS, 0x56788E, 0x3E5668).windows(Windows.FINS).roof(Roof.FLAT, 0xA8A6A0)
            .entrance(Entrance.GLASS_CANOPY).trim(0xF2F0EA)),
    /** Unnamed east-side block: pale concrete mid-rise with a window grid. */
    PALE_CONCRETE_TOWER(spec(Texture.SMOOTH, 0xD0C8B8, 0xA8A090).windows(Windows.PUNCHED)
            .roof(Roof.FLAT_PENTHOUSE, 0xA49C90).entrance(Entrance.GLASS_CANOPY).trim(0x8C8478)),
    /**
     * Robarts Library: huge, nearly windowless pale grey concrete with fine horizontal ribbing in angled
     * volumes, a few deep-set windows, a recessed glass entrance with ROBARTS cast above it, and the
     * raised triangular roof of its peacock plan.
     */
    ROBARTS(spec(Texture.RIBBED, 0xC9C3B8, 0xA8A298).windows(Windows.SPARSE).roof(Roof.TRIANGLE, 0xB4AEA4)
            .entrance(Entrance.RECESSED).trim(0xD8D2C6).faceted().carvedSign()),
    /** Whitney Hall: symmetrical Georgian red brick, white trim, portico, slate roof, chimneys. */
    GEORGIAN_BRICK(spec(Texture.BRICK, 0xB05640, 0x8A3E30).windows(Windows.PUNCHED).roof(Roof.SLATE, 0x5A5E6A)
            .entrance(Entrance.PORTICO).trim(0xF2EEE4).chimneys()),
    /** Ramsay Wright: long tan lab block, regular grid of windows, mechanical penthouse. */
    TAN_LAB_GRID(spec(Texture.SMOOTH, 0xC8AE82, 0xA48C64).windows(Windows.GRID).roof(Roof.FLAT_PENTHOUSE, 0x9E9282)
            .entrance(Entrance.GLASS_CANOPY).trim(0x8E7C5C)),
    /** Morrison Hall: modern red-brown brick residence with large windows. */
    BROWN_BRICK(spec(Texture.BRICK, 0x8E4E3A, 0x6E3A2C).windows(Windows.LARGE).roof(Roof.FLAT, 0x8A847E)
            .entrance(Entrance.GLASS_CANOPY).trim(0x3A3634)),
    /** Sidney Smith Hall: pale concrete, long ribbon windows, dark recessed ground floor with columns, steps. */
    CONCRETE_RIBBON(spec(Texture.SMOOTH, 0xD6D0C2, 0xB0AA9C).windows(Windows.RIBBON).roof(Roof.FLAT, 0xA6A094)
            .entrance(Entrance.COLONNADE).trim(0xD6D0C2)),
    /** Sir Daniel Wilson Residence: 1950s collegiate sandstone, slate roof, gables. */
    SANDSTONE_GABLES(spec(Texture.STONE, 0xD4BC92, 0xAE9670).windows(Windows.PUNCHED)
            .roof(Roof.SLATE_GABLES, 0x56606A).entrance(Entrance.STONE_ARCH).trim(0xEAE0CA).chimneys()),
    /** Victorian red brick house: steep front gable, porch. */
    VICTORIAN_HOUSE(spec(Texture.BRICK, 0xA44E3A, 0x7E3A2C).windows(Windows.PUNCHED).roof(Roof.HOUSE_GABLE, 0x4E525C)
            .entrance(Entrance.PORCH).trim(0xEDE6D6).chimneys()),
    /** Lash Miller: brutalist red-brown concrete with deep vertical fins, a glass box on one corner. */
    BRUTALIST_FINS(spec(Texture.PANELS, 0x9A5E48, 0x744434).windows(Windows.DEEP_FINS)
            .roof(Roof.FLAT_PENTHOUSE, 0x8E8078).entrance(Entrance.GLASS_CANOPY).trim(0xB07A62)),
    /** Knox College: Collegiate Gothic sandstone, central tower, pointed-arch entrance and windows, slate roof. */
    COLLEGIATE_GOTHIC(spec(Texture.STONE, 0xC8B07E, 0xA08C62).windows(Windows.POINTED).roof(Roof.GOTHIC_TOWER, 0x50565E)
            .entrance(Entrance.GOTHIC_ARCH).trim(0xE0D0A8)),
    /** McLennan Physical Labs: warm brick, pale window surrounds, a tall chimney. */
    WARM_BRICK_LABS(spec(Texture.BRICK, 0xB46A48, 0x8C5038).windows(Windows.PUNCHED).roof(Roof.FLAT, 0x8E8680)
            .entrance(Entrance.GLASS_CANOPY).trim(0xE8DCC4).tallChimney()),
    /** Astronomy and Astrophysics: buff brick lab with a small rooftop observatory dome. */
    BUFF_BRICK_DOME(spec(Texture.BRICK, 0xD2B57E, 0xAE9460).windows(Windows.PUNCHED).roof(Roof.DOME, 0x9C9488)
            .entrance(Entrance.GLASS_CANOPY).trim(0xEEE4CC)),
    /** Myhal Centre: angular glass with a dark frame and terracotta panels. */
    ANGULAR_GLASS(spec(Texture.GLASS, 0x4E6E84, 0x2E3A44).windows(Windows.ANGULAR).roof(Roof.FLAT, 0x9A9690)
            .entrance(Entrance.GLASS_CANOPY).trim(0x2E3A44)),
    /** Galbraith: 1960s buff brick between a vertical concrete frame, tall windows. */
    BUFF_BRICK_FRAME(spec(Texture.BRICK, 0xC9A874, 0xA48658).windows(Windows.TALL_FRAMED).roof(Roof.FLAT, 0x9A9284)
            .entrance(Entrance.GLASS_CANOPY).trim(0xD8D2C4)),
    /** Bahen Centre: pinkish concrete towers either side of a dark louvred box over a lit glazed band, raised terrace. */
    BAHEN_LOUVRES(spec(Texture.PANELS, 0xD0BAAE, 0xA8928A).windows(Windows.LOUVRE_BOX)
            .roof(Roof.FLAT_PENTHOUSE, 0xA8A09A).entrance(Entrance.TERRACE_STAIR).trim(0xE4D2C8)),
    /** Koffler Centre: Beaux-Arts former library, pale stone base, tall pilasters, deep cornice. */
    BEAUX_ARTS(spec(Texture.STONE, 0xDCD0B8, 0xB4A88E).windows(Windows.PILASTERS).roof(Roof.FLAT, 0x8E9690)
            .entrance(Entrance.GRAND_STAIRS).trim(0xF0E8D6).cornice(0xC8BCA2)),
    /** Wallberg: long 1940s buff brick engineering building, regular windows, stone entrance. */
    LONG_BUFF_BRICK(spec(Texture.BRICK, 0xCDAE78, 0xA88E5C).windows(Windows.PUNCHED).roof(Roof.FLAT, 0x988E80)
            .entrance(Entrance.STONE_ARCH).trim(0xE6DAC0)),
    /** Three-storey heritage red brick with stone trim. */
    HERITAGE_RED(spec(Texture.BRICK, 0xA6503C, 0x803C2E).windows(Windows.PUNCHED).roof(Roof.FLAT, 0x8A847E)
            .entrance(Entrance.STONE_ARCH).trim(0xE8DCC4).cornice(0xD8CCB0)),
    /** Exam Centre, 255 McCaul: buff brick, stacked dark window panels in vertical strips, recessed entrance. */
    EXAM_STRIPS(spec(Texture.BRICK, 0xCDB284, 0xA89062).windows(Windows.STRIPS).roof(Roof.FLAT, 0x9A9284)
            .entrance(Entrance.RECESSED).trim(0xB49A6C));

    /** Wall surface. */
    enum Texture { BRICK, PANELS, SMOOTH, STONE, GLASS, RIBBED }

    /** Window pattern on the upper floors. */
    enum Windows {
        PUNCHED, BAYS, DEEP_SET, FINS, GRID, LARGE, RIBBON, SPARSE, POINTED, DEEP_FINS, ANGULAR, TALL_FRAMED,
        LOUVRE_BOX, PILASTERS, STRIPS
    }

    /** Roof seen from above. */
    enum Roof { FLAT, FLAT_PENTHOUSE, SLATE, SLATE_GABLES, HOUSE_GABLE, TRIANGLE, DOME, GOTHIC_TOWER }

    /** Main entrance. */
    enum Entrance { STONE_ARCH, GLASS_CANOPY, PORTICO, COLONNADE, RECESSED, GOTHIC_ARCH, PORCH, GRAND_STAIRS,
        TERRACE_STAIR }

    final Texture texture;
    final Color wall;
    final Color wallDark;
    final Windows windows;
    final Roof roof;
    final Color roofColour;
    final Entrance entrance;
    final Color trim;
    final Color cornice;
    final boolean chimneys;
    final boolean tallChimney;
    final boolean cantilever;
    final boolean faceted;
    final boolean carvedSign;

    BuildingStyle(Spec s) {
        texture = s.texture;
        wall = s.wall;
        wallDark = s.wallDark;
        windows = s.windows;
        roof = s.roof;
        roofColour = s.roofColour;
        entrance = s.entrance;
        trim = s.trim;
        cornice = s.cornice == null ? s.trim : s.cornice;
        chimneys = s.chimneys;
        tallChimney = s.tallChimney;
        cantilever = s.cantilever;
        faceted = s.faceted;
        carvedSign = s.carvedSign;
    }

    private static Spec spec(Texture texture, int wall, int wallDark) {
        Spec s = new Spec();
        s.texture = texture;
        s.wall = new Color(wall);
        s.wallDark = new Color(wallDark);
        return s;
    }

    /** Builder for the style table above. */
    private static final class Spec {
        private Texture texture;
        private Color wall;
        private Color wallDark;
        private Windows windows = Windows.PUNCHED;
        private Roof roof = Roof.FLAT;
        private Color roofColour = new Color(0x8A847E);
        private Entrance entrance = Entrance.GLASS_CANOPY;
        private Color trim = new Color(0xE8E0D0);
        private Color cornice;
        private boolean chimneys;
        private boolean tallChimney;
        private boolean cantilever;
        private boolean faceted;
        private boolean carvedSign;

        Spec windows(Windows w) {
            windows = w;
            return this;
        }

        Spec roof(Roof r, int colour) {
            roof = r;
            roofColour = new Color(colour);
            return this;
        }

        Spec entrance(Entrance e) {
            entrance = e;
            return this;
        }

        Spec trim(int colour) {
            trim = new Color(colour);
            return this;
        }

        Spec cornice(int colour) {
            cornice = new Color(colour);
            return this;
        }

        Spec chimneys() {
            chimneys = true;
            return this;
        }

        Spec tallChimney() {
            tallChimney = true;
            return this;
        }

        Spec cantilever() {
            cantilever = true;
            return this;
        }

        Spec faceted() {
            faceted = true;
            return this;
        }

        Spec carvedSign() {
            carvedSign = true;
            return this;
        }
    }
}
