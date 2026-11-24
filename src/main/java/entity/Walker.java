package entity;

import java.util.Optional;

/**
 * The player's character on the campus map. Positions are in map pixels (16 per tile) and refer to
 * the middle of the character's feet. Collision uses a small hitbox around the feet, so the upper
 * body can overlap the tile above, which gives the 3/4-view look.
 */
public class Walker {
    public static final int TILE_SIZE = 16;
    public static final int HITBOX_WIDTH = 12;
    public static final int HITBOX_HEIGHT = 8;
    /** Walking speed in map pixels per second (4 tiles per second). */
    public static final double SPEED = 4.0 * TILE_SIZE;

    private static final double EPS = 1e-9;
    /** Largest distance moved per collision step, so the walker never skips over a tile. */
    private static final double MAX_STEP = TILE_SIZE / 2.0;
    private static final double HALF_WIDTH = HITBOX_WIDTH / 2.0;

    private double footX;
    private double footY;

    public Walker(double footX, double footY) {
        this.footX = footX;
        this.footY = footY;
    }

    /**
     * Moves in the given direction for the given time, stopping at solid tiles. Each axis is
     * resolved separately, so the walker slides along a wall that blocks only one axis.
     * @param dirX -1 (left), 0, or 1 (right)
     * @param dirY -1 (up), 0, or 1 (down)
     * @param seconds elapsed time
     * @param map the map to collide with
     */
    public void move(double dirX, double dirY, double seconds, CampusMap map) {
        double length = Math.hypot(dirX, dirY);
        if (length == 0 || seconds <= 0) {
            return;
        }
        double distance = SPEED * seconds;
        int steps = (int) Math.ceil(distance / MAX_STEP);
        double stepX = dirX / length * distance / steps;
        double stepY = dirY / length * distance / steps;
        for (int i = 0; i < steps; i++) {
            moveX(stepX, map);
            moveY(stepY, map);
        }
    }

    private void moveX(double dx, CampusMap map) {
        if (dx == 0) {
            return;
        }
        double newX = footX + dx;
        int firstRow = firstTile(footY - HITBOX_HEIGHT);
        int lastRow = lastTile(footY);
        if (dx > 0) {
            int col = lastTile(newX + HALF_WIDTH);
            if (anySolidInColumn(map, col, firstRow, lastRow)) {
                newX = col * TILE_SIZE - HALF_WIDTH;
            }
        } else {
            int col = firstTile(newX - HALF_WIDTH);
            if (anySolidInColumn(map, col, firstRow, lastRow)) {
                newX = (col + 1) * TILE_SIZE + HALF_WIDTH;
            }
        }
        footX = newX;
    }

    private void moveY(double dy, CampusMap map) {
        if (dy == 0) {
            return;
        }
        double newY = footY + dy;
        int firstCol = firstTile(footX - HALF_WIDTH);
        int lastCol = lastTile(footX + HALF_WIDTH);
        if (dy > 0) {
            int row = lastTile(newY);
            if (anySolidInRow(map, row, firstCol, lastCol)) {
                newY = row * TILE_SIZE;
            }
        } else {
            int row = firstTile(newY - HITBOX_HEIGHT);
            if (anySolidInRow(map, row, firstCol, lastCol)) {
                newY = (row + 1) * TILE_SIZE + HITBOX_HEIGHT;
            }
        }
        footY = newY;
    }

    private static boolean anySolidInColumn(CampusMap map, int col, int firstRow, int lastRow) {
        for (int row = firstRow; row <= lastRow; row++) {
            if (map.isSolid(col, row)) {
                return true;
            }
        }
        return false;
    }

    private static boolean anySolidInRow(CampusMap map, int row, int firstCol, int lastCol) {
        for (int col = firstCol; col <= lastCol; col++) {
            if (map.isSolid(col, row)) {
                return true;
            }
        }
        return false;
    }

    /** Tile index containing the inclusive start edge of a span. */
    private static int firstTile(double start) {
        return (int) Math.floor(start / TILE_SIZE);
    }

    /** Tile index containing the exclusive end edge of a span. */
    private static int lastTile(double end) {
        return (int) Math.floor((end - EPS) / TILE_SIZE);
    }

    /** The building whose door the hitbox overlaps, if any. */
    public Optional<Building> buildingAtDoor(CampusMap map) {
        for (int row = firstTile(footY - HITBOX_HEIGHT); row <= lastTile(footY); row++) {
            for (int col = firstTile(footX - HALF_WIDTH); col <= lastTile(footX + HALF_WIDTH); col++) {
                Optional<Building> building = map.buildingAtTile(col, row);
                if (building.isPresent()) {
                    return building;
                }
            }
        }
        return Optional.empty();
    }

    /** Stands the walker on the tile directly below the given door, without touching the door. */
    public void placeBelow(TilePosition door) {
        footX = door.getCol() * TILE_SIZE + TILE_SIZE / 2.0;
        footY = (door.getRow() + 1) * TILE_SIZE + (TILE_SIZE + HITBOX_HEIGHT) / 2.0;
    }

    public double getFootX() {
        return footX;
    }

    public double getFootY() {
        return footY;
    }
}
