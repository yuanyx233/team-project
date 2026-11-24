package view.map;

/**
 * Which way the player faces and which walking frame to show. It follows the movement keys the map
 * screen already reads: the player faces the way they move (horizontal wins on diagonals), the
 * walk cycle runs at {@link #FRAMES_PER_SECOND} while moving, and stopping shows the standing frame.
 */
public final class WalkAnimation {
    public static final double FRAMES_PER_SECOND = 10.0;

    private CharacterSheet.Direction facing = CharacterSheet.Direction.DOWN;
    private double walkedSeconds;
    private boolean moving;

    /**
     * Advances the animation.
     * @param dirX -1, 0 or 1 for left, none or right
     * @param dirY -1, 0 or 1 for up, none or down
     * @param seconds time since the last update
     */
    public void update(int dirX, int dirY, double seconds) {
        moving = dirX != 0 || dirY != 0;
        if (!moving) {
            walkedSeconds = 0;
            return;
        }
        if (dirX != 0) {
            facing = dirX > 0 ? CharacterSheet.Direction.RIGHT : CharacterSheet.Direction.LEFT;
        } else {
            facing = dirY > 0 ? CharacterSheet.Direction.DOWN : CharacterSheet.Direction.UP;
        }
        walkedSeconds += seconds;
    }

    /** Whether the player moved in the last update. */
    public boolean isMoving() {
        return moving;
    }

    public CharacterSheet.Direction getFacing() {
        return facing;
    }

    /** The frame to draw: 0 when standing, otherwise the current step of the walk cycle. */
    public int getFrame() {
        if (!moving) {
            return 0;
        }
        return (int) (walkedSeconds * FRAMES_PER_SECOND + 1e-9) % CharacterSheet.FRAMES;
    }
}
