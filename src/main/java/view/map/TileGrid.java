package view.map;

import java.util.List;

/**
 * Read-only view of the campus tile characters, as the map painters need them.
 * Positions outside the grid read as grass so painters can look at neighbours freely.
 */
public interface TileGrid {
    char OUTSIDE = '.';

    int getColumns();

    int getRows();

    /** The tile character at a position inside the grid. */
    char getTile(int col, int row);

    /** The tile character, or {@link #OUTSIDE} for positions outside the grid. */
    default char tileOrGrass(int col, int row) {
        if (col < 0 || row < 0 || col >= getColumns() || row >= getRows()) {
            return OUTSIDE;
        }
        return getTile(col, row);
    }

    /** A grid built from text rows of equal length, top to bottom. */
    static TileGrid of(List<String> rows) {
        List<String> copy = List.copyOf(rows);
        return new TileGrid() {
            @Override
            public int getColumns() {
                return copy.isEmpty() ? 0 : copy.get(0).length();
            }

            @Override
            public int getRows() {
                return copy.size();
            }

            @Override
            public char getTile(int col, int row) {
                return copy.get(row).charAt(col);
            }
        };
    }
}
