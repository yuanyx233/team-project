package view;

import app.Main;
import data_access.CampusMapResourceLoader;
import data_access.FileGameStateDataAccessObject;
import data_access.InMemoryMajorCatalog;
import entity.Building;
import entity.CampusMap;
import entity.GameDay;
import entity.Player;
import entity.Question;
import entity.TilePosition;
import interface_adapter.map.BuildingViews;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import use_case.question.QuestionGateway;
import use_case.question.QuestionPurpose;
import use_case.question.QuestionTopic;
import view.map.MapView;
import static org.junit.jupiter.api.Assertions.*;

class CampusActivityNavigationTest {
    @TempDir Path directory;
    private GameView game;
    private final CampusMap map = CampusMapResourceLoader.load(CampusMapResourceLoader.DEFAULT_MAP);

    private void continueGame(int energy, QuestionGateway questions) throws Exception {
        Path checkpoint = directory.resolve("checkpoint.json");
        FileGameStateDataAccessObject saved = new FileGameStateDataAccessObject(checkpoint,
                new GameDay(4, LocalDate.of(2025, 9, 2)), new Player(energy, 100));
        saved.getAcademicRecord().confirmMajor(InMemoryMajorCatalog.predefined().findById("cs").orElseThrow());
        saved.saveCheckpoint();
        SwingUtilities.invokeAndWait(() -> {
            game = (GameView) Main.createView(checkpoint, questions).getComponent(0);
            button(currentPage(), "Continue").doClick();
            assertEquals("residence", currentPage().getName());
            button(currentPage(), "Back to Street").doClick();
        });
    }

    @Test void completedChallengeReturnsOutsideLectureHallAndReopensWithoutAnotherCharge() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        continueGame(100, (topic, purpose, count) -> {
            assertFalse(SwingUtilities.isEventDispatchThread());
            assertEquals(QuestionTopic.PROGRAMMING_FOUNDATION, topic);
            assertEquals(QuestionPurpose.PRACTICE, purpose);
            assertEquals(3, count);
            requests.incrementAndGet();
            return IntStream.range(0, count).mapToObj(index -> new Question("practice-" + index,
                    "Which option is correct?", List.of("Correct", "Wrong", "Other", "None"),
                    0, null)).collect(Collectors.toList());
        });
        enterFrom(Building.RESIDENCE, Building.LECTURE_HALL);
        awaitNamedButton("challenge.select");
        clickNamed("challenge.select");
        awaitNamedButton("challenge.start");
        clickNamed("challenge.start");
        for (int index = 0; index < 3; index++) {
            awaitNamedButton("challenge.submit");
            clickNamed("challenge.option.0");
            clickNamed("challenge.submit");
            awaitNamedButton("challenge.next");
            clickNamed("challenge.next");
        }
        awaitNamedButton("challenge.courses");
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(hasText(currentPage(), "Preparation points for this course: 3"));
            assertTrue(hasText(currentPage(), "Energy: 80"));
        });
        clickNamed("challenge.back");
        await(() -> "street".equals(currentPage().getName()));
        reenterFromOutside(Building.LECTURE_HALL);
        awaitNamedButton("challenge.select");
        clickNamed("challenge.select");
        awaitNamedButton("challenge.courses");
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(hasText(currentPage(), "Preparation points for this course: 3"));
            assertTrue(hasText(currentPage(), "Energy: 80"));
        });
        assertEquals(1, requests.get());
    }

    @Test void continuingFromTitleReturnsOutsideResidenceInsteadOfThePreviousLectureDoor() throws Exception {
        continueGame(0, (topic, purpose, count) -> {
            fail("Navigation and Continue must not request questions.");
            return List.of();
        });
        enterFrom(Building.RESIDENCE, Building.LECTURE_HALL);
        awaitNamedButton("challenge.back");
        clickNamed("challenge.back");
        await(() -> "street".equals(currentPage().getName()));
        SwingUtilities.invokeAndWait(() -> {
            button(currentPage(), "Title Screen").doClick();
            button(currentPage(), "Continue").doClick();
            assertEquals("residence", currentPage().getName());
            button(currentPage(), "Back to Street").doClick();
        });
        reenterFromOutside(Building.RESIDENCE);
        SwingUtilities.invokeAndWait(() -> assertTrue(hasText(currentPage(), "Energy: 0 / 100")));
    }

    @ParameterizedTest
    @EnumSource(value = Building.class, names = {"REGISTRAR", "EXAM_CENTRE"})
    void otherBuildingCallbacksAlsoReturnOutsideWithoutLoadingQuestions(Building building) throws Exception {
        continueGame(0, (topic, purpose, count) -> {
            fail("Entering and leaving a building must not request questions.");
            return List.of();
        });
        enterFrom(Building.RESIDENCE, building);
        await(() -> button(currentPage(), "Back to Street").isEnabled());
        SwingUtilities.invokeAndWait(() -> button(currentPage(), "Back to Street").doClick());
        await(() -> "street".equals(currentPage().getName()));
        reenterFromOutside(building);
    }

    private void reenterFromOutside(Building building) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MapView street = (MapView) currentPage();
            street.interact();
            assertEquals("street", currentPage().getName(), "Leaving must place the player outside the door.");
            street.walk(0, -1, 0.2);
            street.interact();
            assertEquals(BuildingViews.viewOf(building), currentPage().getName());
        });
    }

    private void enterFrom(Building from, Building destination) throws Exception {
        TilePosition door = map.doorOf(from);
        List<TilePosition> path = route(new TilePosition(door.getCol(), door.getRow() + 1), map.doorOf(destination));
        SwingUtilities.invokeAndWait(() -> {
            MapView street = (MapView) currentPage();
            for (int index = 1; index < path.size(); index++) {
                TilePosition previous = path.get(index - 1);
                TilePosition next = path.get(index);
                street.walk(next.getCol() - previous.getCol(), next.getRow() - previous.getRow(), 0.25);
            }
            street.interact();
            assertEquals(BuildingViews.viewOf(destination), currentPage().getName());
        });
    }

    /** Finds a walkable route through the actual bundled collision map. */
    private List<TilePosition> route(TilePosition start, TilePosition destination) {
        Map<TilePosition, TilePosition> previous = new HashMap<>();
        ArrayDeque<TilePosition> queue = new ArrayDeque<>();
        previous.put(start, null);
        queue.add(start);
        while (!queue.isEmpty() && !previous.containsKey(destination)) {
            TilePosition current = queue.remove();
            for (int[] direction : new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}}) {
                TilePosition next = new TilePosition(current.getCol() + direction[0], current.getRow() + direction[1]);
                if (!map.isSolid(next.getCol(), next.getRow()) && !previous.containsKey(next)) {
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }
        assertTrue(previous.containsKey(destination), "No walkable route to the building.");
        List<TilePosition> path = new ArrayList<>();
        for (TilePosition tile = destination; tile != null; tile = previous.get(tile)) { path.add(tile); }
        Collections.reverse(path);
        return path;
    }

    private void awaitNamedButton(String name) throws Exception {
        await(() -> {
            Component found = named(currentPage(), name);
            return found != null && found.isEnabled();
        });
    }

    private void clickNamed(String name) throws Exception {
        SwingUtilities.invokeAndWait(() -> ((AbstractButton) named(currentPage(), name)).doClick());
    }

    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        AtomicBoolean ready = new AtomicBoolean();
        while (!ready.get() && System.nanoTime() < deadline) {
            SwingUtilities.invokeAndWait(() -> ready.set(condition.getAsBoolean()));
            if (!ready.get()) { Thread.sleep(10); }
        }
        assertTrue(ready.get(), "The activity page did not finish updating.");
    }

    private Container currentPage() {
        for (Component page : game.getComponents()) {
            if (page.isVisible()) { return (Container) page; }
        }
        throw new AssertionError("No visible page.");
    }

    private AbstractButton button(Container parent, String label) {
        AtomicReference<AbstractButton> found = new AtomicReference<>();
        visit(parent, component -> {
            if (component instanceof AbstractButton && label.equals(((AbstractButton) component).getText())) {
                found.set((AbstractButton) component);
            }
        });
        assertNotNull(found.get(), label);
        return found.get();
    }

    private Component named(Container parent, String name) {
        AtomicReference<Component> found = new AtomicReference<>();
        visit(parent, component -> { if (name.equals(component.getName())) { found.set(component); } });
        return found.get();
    }

    private boolean hasText(Container parent, String text) {
        AtomicBoolean found = new AtomicBoolean();
        visit(parent, component -> {
            if (component instanceof JLabel && ((JLabel) component).getText().contains(text)
                    || component instanceof JTextArea && ((JTextArea) component).getText().contains(text)) {
                found.set(true);
            }
        });
        return found.get();
    }

    private void visit(Container parent, java.util.function.Consumer<Component> visitor) {
        for (Component component : parent.getComponents()) {
            visitor.accept(component);
            if (component instanceof Container) { visit((Container) component, visitor); }
        }
    }
}
