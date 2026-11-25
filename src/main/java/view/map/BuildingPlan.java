package view.map;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * One building of the entrance segment as {@code segment_buildings.txt} describes it: its footprint
 * on the map, how many rows of it are front wall (the rest is roof seen from above), its style, the
 * sign by its entrance, and the column of its door if it has one.
 */
public final class BuildingPlan {
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;
    private final int facadeRows;
    private final BuildingStyle style;
    private final String sign;
    private final int doorColumn;

    BuildingPlan(int left, int top, int right, int bottom, int facadeRows, BuildingStyle style, String sign,
                 int doorColumn) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.facadeRows = facadeRows;
        this.style = style;
        this.sign = sign;
        this.doorColumn = doorColumn;
    }

    /** Wall and door tiles make up building footprints. */
    static boolean isBuildingTile(char tile) {
        return tile == '#' || tile == 'R' || tile == 'L' || tile == 'X' || tile == 'H';
    }

    /**
     * Reads style lines ({@code col row facadeRows STYLE sign...}, sign {@code -} for none) against a grid.
     * @param rows only building tiles in the first {@code rows} rows belong to a footprint
     * @throws IllegalArgumentException naming the line if it is malformed, has an unknown style, or does
     *     not point at a building tile
     */
    public static List<BuildingPlan> parse(List<String> lines, TileGrid grid, int rows) {
        List<BuildingPlan> plans = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+", 5);
            try {
                if (parts.length < 5) {
                    throw new IllegalArgumentException("expected: col row facadeRows STYLE sign");
                }
                int col = Integer.parseInt(parts[0]);
                int row = Integer.parseInt(parts[1]);
                int facadeRows = Integer.parseInt(parts[2]);
                BuildingStyle style = BuildingStyle.valueOf(parts[3]);
                String sign = parts[4].equals("-") ? "" : parts[4];
                plans.add(footprint(grid, rows, col, row, facadeRows, style, sign));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Bad building style on line " + (i + 1) + ": " + line
                        + " (" + e.getMessage() + ")", e);
            }
        }
        return plans;
    }

    private static BuildingPlan footprint(TileGrid grid, int rows, int col, int row, int facadeRows,
                                          BuildingStyle style, String sign) {
        if (col < 0 || row < 0 || col >= grid.getColumns() || row >= rows || !isBuildingTile(grid.getTile(col, row))) {
            throw new IllegalArgumentException("column " + col + ", row " + row + " is not a building tile");
        }
        boolean[][] seen = new boolean[rows][grid.getColumns()];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {col, row});
        seen[row][col] = true;
        int left = col;
        int right = col;
        int top = row;
        int bottom = row;
        int door = -1;
        while (!queue.isEmpty()) {
            int[] tile = queue.poll();
            left = Math.min(left, tile[0]);
            right = Math.max(right, tile[0]);
            top = Math.min(top, tile[1]);
            bottom = Math.max(bottom, tile[1]);
            if (grid.getTile(tile[0], tile[1]) != '#') {
                door = tile[0];
            }
            int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] step : steps) {
                int c = tile[0] + step[0];
                int r = tile[1] + step[1];
                if (c >= 0 && r >= 0 && c < grid.getColumns() && r < rows && !seen[r][c]
                        && isBuildingTile(grid.getTile(c, r))) {
                    seen[r][c] = true;
                    queue.add(new int[] {c, r});
                }
            }
        }
        return new BuildingPlan(left, top, right, bottom, facadeRows, style, sign, door);
    }

    /** Whether the tile lies within this building's footprint rectangle. */
    public boolean contains(int col, int row) {
        return col >= left && col <= right && row >= top && row <= bottom;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }

    public int getFacadeRows() {
        return facadeRows;
    }

    public BuildingStyle getStyle() {
        return style;
    }

    /** The sign text, or an empty string for none. */
    public String getSign() {
        return sign;
    }

    /** The door's column, or -1 if the building has no door. */
    public int getDoorColumn() {
        return doorColumn;
    }
}
