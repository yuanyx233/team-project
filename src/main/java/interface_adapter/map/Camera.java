package interface_adapter.map;

/**
 * Computes how far the map is scrolled so that the player stays centered.
 */
public final class Camera {
    private Camera() {
    }

    /**
     * Returns the scroll offset along one axis, in the same units as the arguments.
     * The target is centered, but the view never shows space beyond the map edges.
     * If the map is smaller than the view, the map is centered instead.
     * @param target position to center on
     * @param viewSize size of the visible area
     * @param mapSize size of the whole map
     */
    public static double offset(double target, double viewSize, double mapSize) {
        if (mapSize <= viewSize) {
            return -(viewSize - mapSize) / 2.0;
        }
        double centered = target - viewSize / 2.0;
        return Math.max(0, Math.min(centered, mapSize - viewSize));
    }
}
