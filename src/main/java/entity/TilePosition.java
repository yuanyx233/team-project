package entity;

import java.util.Objects;

/**
 * A column and row on the campus map grid.
 */
public final class TilePosition {
    private final int col;
    private final int row;

    public TilePosition(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TilePosition)) {
            return false;
        }
        TilePosition other = (TilePosition) o;
        return col == other.col && row == other.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }

    @Override
    public String toString() {
        return "(" + col + ", " + row + ")";
    }
}
