package data_access;

import entity.AcademicRecord;
import entity.Course;
import entity.DailyWeather;
import entity.GameDay;
import entity.Major;
import entity.Player;
import entity.WeatherCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.continue_game.InvalidCheckpointException;
import use_case.rest.SaveFailedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileGameStateDataAccessObjectTest {
    private static final LocalDate START = LocalDate.of(2025, 9, 2);

    @TempDir
    Path dir;

    private FileGameStateDataAccessObject newGame(Path file) {
        return new FileGameStateDataAccessObject(file, new GameDay(1, START), new Player(100, 100));
    }

    private static Major computerScience() {
        return new Major("cs", "Computer Science", "Explore computing.", List.of(
                new Course("CS101", "Computing Foundations", "high_school_computer_science"),
                new Course("CS201", "College Computer Science", "college_computer_science")),
                "https://example.test/cs", Major.Source.LOCAL_FALLBACK);
    }

    @Test
    void savedCheckpointCanBeLoadedBack() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        FileGameStateDataAccessObject original = newGame(file);
        original.setCurrentDay(new GameDay(3, START));
        original.getPlayer().restoreEnergyToPercent(0);
        original.setCurrentWeather(new DailyWeather(START.plusDays(2), WeatherCondition.SNOW, -1.5, -7.0));
        original.saveCheckpoint();

        FileGameStateDataAccessObject loaded = newGame(file);
        assertTrue(loaded.hasCheckpoint());
        loaded.loadCheckpoint();

        assertEquals(3, loaded.getCurrentDay().getDayNumber());
        assertEquals(START, loaded.getCurrentDay().getStartDate());
        assertEquals(100, loaded.getPlayer().getEnergy());
        assertEquals(100, loaded.getPlayer().getMaxEnergy());
        assertEquals(WeatherCondition.SNOW, loaded.getCurrentWeather().getCondition());
        assertEquals(START.plusDays(2), loaded.getCurrentWeather().getDate());
        assertEquals(-1.5, loaded.getCurrentWeather().getHighTemperature());
        assertEquals(-7.0, loaded.getCurrentWeather().getLowTemperature());
    }

    @Test
    void checkpointWithoutWeatherRoundTrips() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        newGame(file).saveCheckpoint();

        FileGameStateDataAccessObject loaded = newGame(file);
        loaded.loadCheckpoint();

        assertNull(loaded.getCurrentWeather());
    }

    @Test
    void noCheckpointWhenFileIsMissing() {
        assertFalse(newGame(dir.resolve("missing.json")).hasCheckpoint());
    }

    @Test
    void damagedCheckpointIsRejectedAndLeftUnchanged() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        Files.writeString(file, "{not json");

        assertThrows(InvalidCheckpointException.class, () -> newGame(file).loadCheckpoint());
        assertEquals("{not json", Files.readString(file));
    }

    @Test
    void unsupportedVersionIsRejected() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        Files.writeString(file, "{\"version\":99,\"dayNumber\":1,\"startDate\":\"2025-09-02\","
                + "\"energy\":50,\"maxEnergy\":100}");

        assertThrows(InvalidCheckpointException.class, () -> newGame(file).loadCheckpoint());
    }

    @Test
    void impossibleValuesAreRejected() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        Files.writeString(file, "{\"version\":1,\"dayNumber\":1,\"startDate\":\"2025-09-02\","
                + "\"energy\":500,\"maxEnergy\":100}");

        assertThrows(InvalidCheckpointException.class, () -> newGame(file).loadCheckpoint());
    }

    @Test
    void saveFailsWhenLocationIsNotWritable() throws Exception {
        Path blocker = dir.resolve("blocker");
        Files.writeString(blocker, "a file, not a directory");

        assertThrows(SaveFailedException.class, () -> newGame(blocker.resolve("checkpoint.json")).saveCheckpoint());
    }

    @Test
    void newGameHasNoConfirmedMajor() {
        assertFalse(newGame(dir.resolve("checkpoint.json")).getAcademicRecord().hasConfirmedMajor());
    }

    @Test
    void confirmedMajorAndCoursesRoundTrip() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        FileGameStateDataAccessObject original = newGame(file);
        original.getAcademicRecord().confirmMajor(computerScience());
        original.saveCheckpoint();

        FileGameStateDataAccessObject loaded = newGame(file);
        loaded.loadCheckpoint();

        AcademicRecord record = loaded.getAcademicRecord();
        assertTrue(record.hasConfirmedMajor());
        Major major = record.getSelectedMajor();
        assertEquals("cs", major.getId());
        assertEquals("Computer Science", major.getName());
        assertEquals("Explore computing.", major.getDescription());
        assertEquals("https://example.test/cs", major.getSourceUrl());
        assertEquals(Major.Source.LOCAL_FALLBACK, major.getSource());
        assertEquals(2, major.getCourses().size());
        Course second = major.getCourses().get(1);
        assertEquals("CS201", second.getId());
        assertEquals("College Computer Science", second.getName());
        assertEquals("college_computer_science", second.getQuestionCategory());
        assertEquals(List.of("CS101", "CS201"), List.copyOf(record.getCourseProgress().keySet()));
    }

    @Test
    void checkpointWithoutMajorLoadsWithNoMajor() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        newGame(file).saveCheckpoint();

        FileGameStateDataAccessObject loaded = newGame(file);
        loaded.getAcademicRecord().confirmMajor(computerScience());
        loaded.loadCheckpoint();

        assertFalse(loaded.getAcademicRecord().hasConfirmedMajor());
    }

    @Test
    void versionOneCheckpointLoadsWithNoMajor() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        Files.writeString(file, "{\"version\":1,\"dayNumber\":4,\"startDate\":\"2025-09-02\","
                + "\"energy\":50,\"maxEnergy\":100}");

        FileGameStateDataAccessObject loaded = newGame(file);
        loaded.loadCheckpoint();

        assertEquals(4, loaded.getCurrentDay().getDayNumber());
        assertFalse(loaded.getAcademicRecord().hasConfirmedMajor());
    }

    @Test
    void invalidMajorIsRejectedAndStateIsUnchanged() throws Exception {
        Path file = dir.resolve("checkpoint.json");
        Files.writeString(file, "{\"version\":2,\"dayNumber\":4,\"startDate\":\"2025-09-02\","
                + "\"energy\":50,\"maxEnergy\":100,\"major\":{\"id\":\"cs\",\"name\":\"CS\","
                + "\"description\":\"d\",\"sourceUrl\":\"\",\"source\":\"LOCAL\",\"courses\":[]}}");
        FileGameStateDataAccessObject game = newGame(file);

        assertThrows(InvalidCheckpointException.class, game::loadCheckpoint);
        assertEquals(1, game.getCurrentDay().getDayNumber());
        assertFalse(game.getAcademicRecord().hasConfirmedMajor());
    }
}
