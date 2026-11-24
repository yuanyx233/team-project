package entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The St. George Street tile grid. Each tile is one character:
 * {@code .} grass, {@code -} sidewalk, {@code p} plaza paving, {@code =} road, {@code #} building wall,
 * {@code T} tree, {@code o} street furniture (a lamp, bench, bins, a student and so on),
 * {@code F} half of a parked food truck, {@code c} half of a parked car,
 * and each building's door character (see {@link Building#getDoorTile()}).
 * Walls, trees, furniture, food trucks and cars are solid. Doors face south, so the tile below each door
 * must be walkable.
 */
public final class CampusMap {
    public static final char GRASS = '.';
    public static final char SIDEWALK = '-';
    public static final char ROAD = '=';
    public static final char WALL = '#';
    public static final char TREE = 'T';
    public static final char PLAZA = 'p';
    public static final char FURNITURE = 'o';
    public static final char FOOD_TRUCK = 'F';
    public static final char PARKED_CAR = 'c';

    private final char[][] tiles;
    private final Map<Building, TilePosition> doors;

    private CampusMap(char[][] tiles, Map<Building, TilePosition> doors) {
        this.tiles = tiles;
        this.doors = doors;
    }

    /**
     * Builds a map from text rows.
     * @param rows the map rows, top to bottom
     * @return the parsed map
     * @throws IllegalArgumentException if the rows are empty or uneven, contain an unknown character,
     *     do not have exactly one door per building, or a door has no walkable tile below it
     */
    public static CampusMap parse(List<String> rows) {
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            throw new IllegalArgumentException("Map is empty");
        }
        int width = rows.get(0).length();
        char[][] tiles = new char[rows.size()][];
        Map<Building, List<TilePosition>> found = new EnumMap<>(Building.class);
        for (int row = 0; row < rows.size(); row++) {
            String line = rows.get(row);
            if (line.length() != width) {
                throw new IllegalArgumentException("Map row " + (row + 1) + " has width " + line.length()
                        + " but row 1 has width " + width);
            }
            tiles[row] = line.toCharArray();
            for (int col = 0; col < width; col++) {
                char c = tiles[row][col];
                Optional<Building> building = buildingForDoor(c);
                if (building.isPresent()) {
                    found.computeIfAbsent(building.get(), b -> new ArrayList<>()).add(new TilePosition(col, row));
                } else if (!isKnownGround(c)) {
                    throw new IllegalArgumentException("Unknown map character '" + c + "' at row " + (row + 1)
                            + ", column " + (col + 1));
                }
            }
        }

        Map<Building, TilePosition> doors = new EnumMap<>(Building.class);
        for (Building building : Building.values()) {
            List<TilePosition> positions = found.getOrDefault(building, List.of());
            if (positions.size() != 1) {
                throw new IllegalArgumentException("Map must have exactly one door for " + building.getDisplayName()
                        + " ('" + building.getDoorTile() + "') but has " + positions.size());
            }
            doors.put(building, positions.get(0));
        }

        CampusMap map = new CampusMap(tiles, doors);
        for (Map.Entry<Building, TilePosition> door : doors.entrySet()) {
            TilePosition p = door.getValue();
            if (map.isSolid(p.getCol(), p.getRow() + 1)) {
                throw new IllegalArgumentException("The tile below the door of " + door.getKey().getDisplayName()
                        + " at row " + (p.getRow() + 1) + ", column " + (p.getCol() + 1) + " must be walkable");
            }
        }
        return map;
    }

    private static boolean isKnownGround(char c) {
        return c == GRASS || c == SIDEWALK || c == ROAD || c == PLAZA || isSolidCharacter(c);
    }

    private static Optional<Building> buildingForDoor(char c) {
        for (Building building : Building.values()) {
            if (building.getDoorTile() == c) {
                return Optional.of(building);
            }
        }
        return Optional.empty();
    }

    /** Width in tiles. */
    public int getWidth() {
        return tiles[0].length;
    }

    /** Height in tiles. */
    public int getHeight() {
        return tiles.length;
    }

    private boolean inBounds(int col, int row) {
        return row >= 0 && row < tiles.length && col >= 0 && col < tiles[0].length;
    }

    /**
     * Returns the tile character, used by the view to choose a color.
     * @throws IndexOutOfBoundsException if the position is outside the map
     */
    public char tileAt(int col, int row) {
        if (!inBounds(col, row)) {
            throw new IndexOutOfBoundsException("Tile " + new TilePosition(col, row) + " is outside the map");
        }
        return tiles[row][col];
    }

    /** Whether the tile blocks movement. Positions outside the map are solid. */
    public boolean isSolid(int col, int row) {
        if (!inBounds(col, row)) {
            return true;
        }
        char c = tiles[row][col];
        return isSolidCharacter(c);
    }

    private static boolean isSolidCharacter(char c) {
        return c == WALL || c == TREE || c == FURNITURE || c == FOOD_TRUCK || c == PARKED_CAR;
    }

    public TilePosition doorOf(Building building) {
        return doors.get(building);
    }

    /** The building whose door is on this tile, if any. */
    public Optional<Building> buildingAtTile(int col, int row) {
        if (!inBounds(col, row)) {
            return Optional.empty();
        }
        return buildingForDoor(tiles[row][col]);
    }
}
