package interface_adapter.map;

import entity.Building;
import entity.CampusMap;
import entity.Walker;
import interface_adapter.ViewManagerModel;
import interface_adapter.rest.RestViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapControllerTest {
    private static final int T = Walker.TILE_SIZE;
    private static final CampusMap MAP = CampusMap.parse(List.of(
            "RLXH......",
            "..........",
            "..........",
            ".........."));

    private MapViewModel viewModel;
    private ViewManagerModel viewManager;
    private MapController controller;

    @BeforeEach
    void setUp() {
        viewModel = new MapViewModel();
        viewManager = new ViewManagerModel();
        controller = new MapController(MAP, new Walker(6 * T, 3 * T), viewModel, viewManager);
    }

    @Test
    void layoutIsPublishedToTheViewModel() {
        assertEquals(10, viewModel.getColumns());
        assertEquals(4, viewModel.getRows());
        assertEquals('R', viewModel.getTile(0, 0));
        assertEquals('.', viewModel.getTile(5, 2));
        assertEquals(Building.values().length, viewModel.getLabels().size());
    }

    @Test
    void tickMovesTheWalkerInTheViewModel() {
        controller.tick(1, 0, 0.5);
        assertEquals(6 * T + 2 * T, viewModel.getWalkerX(), 1e-6);
        assertEquals(3 * T, viewModel.getWalkerY(), 1e-6);
    }

    @Test
    void noPromptAwayFromDoors() {
        controller.tick(0, 0, 0.1);
        assertEquals("", viewModel.getPromptText());
    }

    @Test
    void promptNamesTheBuildingAtTheDoor() {
        controller.returnTo(Building.LECTURE_HALL);
        controller.tick(0, -1, 0.2);
        assertEquals("Press E to enter Lecture Hall", viewModel.getPromptText());
    }

    @Test
    void interactAtADoorShowsThatBuilding() {
        controller.returnTo(Building.EXAM_CENTRE);
        controller.tick(0, -1, 0.2);
        controller.interact();
        assertEquals(BuildingViews.EXAM_CENTRE, viewManager.getActiveView());
    }

    @Test
    void enteringTheResidenceNeedsNoEnergy() {
        // Navigation never reads the player's energy, so it works at zero energy.
        controller.returnTo(Building.RESIDENCE);
        controller.tick(0, -1, 0.2);
        controller.interact();
        assertEquals(RestViewModel.VIEW_NAME, viewManager.getActiveView());
    }

    @Test
    void interactAwayFromDoorsDoesNothing() {
        viewManager.setActiveView(MapViewModel.VIEW_NAME);
        controller.interact();
        assertEquals(MapViewModel.VIEW_NAME, viewManager.getActiveView());
    }

    @Test
    void returnToStandsBelowTheDoorAndShowsTheMap() {
        viewManager.setActiveView(RestViewModel.VIEW_NAME);
        controller.returnTo(Building.RESIDENCE);
        assertEquals(MapViewModel.VIEW_NAME, viewManager.getActiveView());
        assertEquals(3 * T + T / 2.0, viewModel.getWalkerX(), 1e-6);
        assertTrue(viewModel.getWalkerY() > T && viewModel.getWalkerY() <= 2 * T);
        assertEquals("", viewModel.getPromptText());
    }

    @Test
    void everyBuildingHasADistinctView() {
        assertEquals(Building.values().length, BuildingViews.all().values().stream().distinct().count());
        assertEquals(RestViewModel.VIEW_NAME, BuildingViews.viewOf(Building.RESIDENCE));
    }
}
