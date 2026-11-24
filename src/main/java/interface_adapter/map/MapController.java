package interface_adapter.map;

import entity.Building;
import entity.CampusMap;
import entity.TilePosition;
import entity.Walker;
import interface_adapter.ViewManagerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Moves the player around the campus map and switches to a building's screen at its door.
 * Navigation changes only which screen is shown, so there is no use case interactor behind it.
 */
public class MapController {
    /** How many tiles above its door a building's name is drawn. */
    private static final int LABEL_ROWS_ABOVE_DOOR = 2;

    private final CampusMap map;
    private final Walker walker;
    private final MapViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public MapController(CampusMap map, Walker walker, MapViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.map = map;
        this.walker = walker;
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        viewModel.setLayout(tilesOf(map), labelsOf(map));
        publishWalker();
    }

    /**
     * Advances the walker by one frame.
     * @param dirX -1, 0, or 1
     * @param dirY -1, 0, or 1
     * @param seconds time since the previous frame
     */
    public void tick(double dirX, double dirY, double seconds) {
        walker.move(dirX, dirY, seconds, map);
        publishWalker();
    }

    /** Opens the screen of the building whose door the walker is in. Does nothing elsewhere. */
    public void interact() {
        walker.buildingAtDoor(map).ifPresent(b -> viewManagerModel.setActiveView(BuildingViews.viewOf(b)));
    }

    /** Shows the map with the walker standing just outside the given building's door. */
    public void returnTo(Building building) {
        walker.placeBelow(map.doorOf(building));
        publishWalker();
        viewManagerModel.setActiveView(MapViewModel.VIEW_NAME);
    }

    private void publishWalker() {
        Optional<Building> door = walker.buildingAtDoor(map);
        String prompt = door.map(b -> "Press E to enter " + b.getDisplayName()).orElse("");
        viewModel.setWalker(walker.getFootX(), walker.getFootY(), prompt);
    }

    private static char[][] tilesOf(CampusMap map) {
        char[][] tiles = new char[map.getHeight()][map.getWidth()];
        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                tiles[row][col] = map.tileAt(col, row);
            }
        }
        return tiles;
    }

    private static List<MapViewModel.Label> labelsOf(CampusMap map) {
        List<MapViewModel.Label> labels = new ArrayList<>();
        for (Building building : Building.values()) {
            TilePosition door = map.doorOf(building);
            labels.add(new MapViewModel.Label(building.getDisplayName(), door.getCol(),
                    Math.max(0, door.getRow() - LABEL_ROWS_ABOVE_DOOR)));
        }
        return labels;
    }
}
