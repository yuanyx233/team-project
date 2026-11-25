package view.map;

import java.util.ArrayList;
import java.util.List;

/**
 * Building sprites drawn over the campus backdrop. Each line of the placement file is
 * {@code file left top width height} in map pixels; the bottom edge is the building's front,
 * so a sprite hides the player only while the player stands behind (north of) that edge.
 */
public final class SpriteLayer {
    private SpriteLayer() {
    }

    public static List<Placement> parse(List<String> lines) {
        List<Placement> placements = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            try {
                if (parts.length != 5) {
                    throw new NumberFormatException();
                }
                placements.add(new Placement(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Bad sprite placement on line " + (i + 1) + ": " + line);
            }
        }
        return placements;
    }

    public static final class Placement {
        private final String file;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        Placement(String file, int x, int y, int width, int height) {
            this.file = file;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public String getFile() {
            return file;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        /** Whether this sprite covers a player whose feet are at {@code walkerY} map pixels. */
        public boolean isInFrontOf(double walkerY) {
            return walkerY < y + height;
        }
    }
}
