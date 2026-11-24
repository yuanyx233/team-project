package interface_adapter.map;

import entity.Walker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * Holds what the campus map view draws: the tile layout, building labels, the walker's position in
 * map pixels, and the door prompt.
 */
public class MapViewModel {
    /** The map is the game's "street" page. */
    public static final String VIEW_NAME = "street";
    public static final String STATE_PROPERTY = "state";
    /** Map pixels per tile; walker positions are in map pixels. */
    public static final int TILE_SIZE = Walker.TILE_SIZE;

    /** A building name drawn on the map, centered on the given tile. */
    public static final class Label {
        private final String text;
        private final int col;
        private final int row;

        public Label(String text, int col, int row) {
            this.text = text;
            this.col = col;
            this.row = row;
        }

        public String getText() {
            return text;
        }

        public int getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }
    }

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private char[][] tiles = new char[0][0];
    private List<Label> labels = List.of();
    private double walkerX;
    private double walkerY;
    private String promptText = "";

    public void setLayout(char[][] tiles, List<Label> labels) {
        this.tiles = tiles;
        this.labels = List.copyOf(labels);
        support.firePropertyChange(STATE_PROPERTY, null, this);
    }

    public void setWalker(double walkerX, double walkerY, String promptText) {
        this.walkerX = walkerX;
        this.walkerY = walkerY;
        this.promptText = promptText;
        support.firePropertyChange(STATE_PROPERTY, null, this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public int getColumns() {
        return tiles.length == 0 ? 0 : tiles[0].length;
    }

    public int getRows() {
        return tiles.length;
    }

    public char getTile(int col, int row) {
        return tiles[row][col];
    }

    public List<Label> getLabels() {
        return labels;
    }

    public double getWalkerX() {
        return walkerX;
    }

    public double getWalkerY() {
        return walkerY;
    }

    /** The door prompt, or an empty string when the walker is not at a door. */
    public String getPromptText() {
        return promptText;
    }
}
