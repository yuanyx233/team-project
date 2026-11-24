package interface_adapter.map;

import entity.Building;
import interface_adapter.rest.RestViewModel;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * The page name of each building's screen in {@code view.GameView}. Entering a door on the map opens it.
 */
public final class BuildingViews {
    public static final String REGISTRAR = "registrar";
    public static final String LECTURE_HALL = "lecture";
    public static final String EXAM_CENTRE = "exam";

    private static final Map<Building, String> VIEWS;

    static {
        Map<Building, String> views = new EnumMap<>(Building.class);
        views.put(Building.REGISTRAR, REGISTRAR);
        views.put(Building.LECTURE_HALL, LECTURE_HALL);
        views.put(Building.EXAM_CENTRE, EXAM_CENTRE);
        views.put(Building.RESIDENCE, RestViewModel.VIEW_NAME);
        VIEWS = Collections.unmodifiableMap(views);
    }

    private BuildingViews() {
    }

    public static String viewOf(Building building) {
        return VIEWS.get(building);
    }

    public static Map<Building, String> all() {
        return VIEWS;
    }
}
