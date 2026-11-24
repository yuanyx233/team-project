package entity;

/**
 * The four campus buildings the player can enter from the street.
 */
public enum Building {
    REGISTRAR('R', "Registrar's Office"),
    LECTURE_HALL('L', "Lecture Hall"),
    EXAM_CENTRE('X', "Exam Centre"),
    RESIDENCE('H', "Innis Residence");

    private final char doorTile;
    private final String displayName;

    Building(char doorTile, String displayName) {
        this.doorTile = doorTile;
        this.displayName = displayName;
    }

    /** The map file character that marks this building's door. */
    public char getDoorTile() {
        return doorTile;
    }

    public String getDisplayName() {
        return displayName;
    }
}
