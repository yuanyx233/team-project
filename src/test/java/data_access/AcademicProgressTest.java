package data_access;

import entity.AcademicRecord;
import entity.DailyWeather;
import entity.WeatherCondition;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import use_case.rest.RestInteractor;
import use_case.rest.RestOutputBoundary;
import use_case.rest.RestOutputData;

import entity.ChallengeResult;
import entity.ExamBattle;
import entity.ExamResult;
import entity.GameDay;
import entity.Player;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.json.JSONObject;
import use_case.continue_game.InvalidCheckpointException;

import static org.junit.jupiter.api.Assertions.*;

class AcademicProgressTest {
    @TempDir Path directory;

    private FileGameStateDataAccessObject game() {
        FileGameStateDataAccessObject state = new FileGameStateDataAccessObject(
                directory.resolve("save.json"), new GameDay(1, LocalDate.of(2025, 9, 2)),
                new Player(100, 100));
        state.getAcademicRecord().confirmMajor(InMemoryMajorCatalog.predefined().findById("cs").orElseThrow());
        return state;
    }

    @Test
    void challengeAndExamShareEnergyAndProgress() throws Exception {
        FileGameStateDataAccessObject state = game();
        state.startChallenge("CS101", "class", 20);
        state.completeChallenge("CS101", "class", new ChallengeResult(2, 3));
        state.completeChallenge("CS101", "class", new ChallengeResult(2, 3));
        state.startExam("CS101", "exam", 40);
        state.startExam("CS101", "exam", 40);
        assertEquals(40, state.getPlayer().getEnergy());
        assertEquals(2, state.getAcademicRecord().getCourseProgress().get("CS101").getPreparationPoints());
        assertThrows(Exception.class, () -> state.startChallenge("CS201", "other", 20));
        state.abandonExam("exam");
        state.startExam("CS101", "retry", 40);
        assertEquals(0, state.getPlayer().getEnergy());
    }

    @Test
    void checkpointRestoresBothResultsAndPreventsRetakes() throws Exception {
        FileGameStateDataAccessObject state = game();
        state.startChallenge("CS101", "class", 20);
        state.completeChallenge("CS101", "class", new ChallengeResult(3, 3));
        state.startExam("CS101", "exam", 40);
        ExamBattle battle = new ExamBattle(3);
        battle.submitAnswer(true);
        battle.submitAnswer(true);
        battle.submitAnswer(true);
        state.completeExam("CS101", "exam", new ExamResult(battle));
        state.completeExam("CS101", "exam", new ExamResult(battle));
        state.saveCheckpoint();

        FileGameStateDataAccessObject loaded = game();
        loaded.loadCheckpoint();
        assertEquals("cs", loaded.getAcademicRecord().getSelectedMajor().getId());
        assertEquals(40, loaded.getPlayer().getEnergy());
        assertEquals(3, loaded.getAcademicRecord().getCourseProgress().get("CS101").getPreparationPoints());
        assertEquals("A", loaded.getAcademicRecord().getCourseProgress().get("CS101").getExamResult().getGrade());
        assertThrows(IllegalStateException.class, () -> loaded.startExam("CS101", "retake", 40));
        assertNull(loaded.getAcademicRecord().getCourseProgress().get("CS201").getExamResult());
    }

    @Test
    void invalidCourseAndInsufficientEnergyDoNotCharge() {
        FileGameStateDataAccessObject state = game();
        assertThrows(IllegalStateException.class, () -> state.startExam("BIO101", "wrong", 40));
        state.getPlayer().spendEnergy(80);
        assertThrows(IllegalStateException.class, () -> state.startExam("CS101", "low", 40));
        assertEquals(20, state.getPlayer().getEnergy());
    }

    @Test
    void restReservesTheSameActivitySlot() {
        FileGameStateDataAccessObject state = game();
        state.beginRest();
        assertThrows(IllegalStateException.class, () -> state.startExam("CS101", "exam", 40));
        assertThrows(Exception.class, () -> state.startChallenge("CS101", "class", 20));
        assertEquals(100, state.getPlayer().getEnergy());
        state.endRest();
        state.startExam("CS101", "exam", 40);
        assertThrows(IllegalStateException.class, state::beginRest);
    }

    @Test
    void damagedExamDoesNotReplaceCurrentProgress() throws Exception {
        FileGameStateDataAccessObject state = game();
        state.startChallenge("CS101", "class", 20);
        state.completeChallenge("CS101", "class", new ChallengeResult(3, 3));
        state.startExam("CS101", "exam", 40);
        state.completeExam("CS101", "exam", new ExamResult(3, 3, true));
        state.saveCheckpoint();
        JSONObject corrupt = new JSONObject(Files.readString(directory.resolve("save.json")));
        corrupt.put("dayNumber", 9).put("energy", 10);
        corrupt.getJSONObject("academic").getJSONObject("courses").getJSONObject("CS101")
                .put("exam", new JSONObject().put("correct", 0).put("answered", 5).put("won", false));
        String contents = corrupt.toString();
        Files.writeString(directory.resolve("save.json"), contents);
        Player player = state.getPlayer();
        AcademicRecord record = state.getAcademicRecord();

        assertThrows(InvalidCheckpointException.class, state::loadCheckpoint);
        assertSame(player, state.getPlayer());
        assertSame(record, state.getAcademicRecord());
        assertEquals(1, state.getCurrentDay().getDayNumber());
        assertEquals(40, state.getPlayer().getEnergy());
        assertEquals(3, record.getCourseProgress().get("CS101").getPreparationPoints());
        assertEquals("A", record.getCourseProgress().get("CS101").getExamResult().getGrade());
        assertEquals(contents, Files.readString(directory.resolve("save.json")));
    }

    @Test
    void restBlocksActivitiesWhileWeatherIsLoading() throws Exception {
        FileGameStateDataAccessObject state = game();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try {
            RestInteractor rest = new RestInteractor(state, date -> {
                entered.countDown();
                try { assertTrue(release.await(5, TimeUnit.SECONDS)); }
                catch (InterruptedException exception) { throw new IllegalStateException(exception); }
                return new DailyWeather(date, WeatherCondition.CLEAR, 20, 10);
            }, new RestOutputBoundary() {
                @Override public void prepareSuccessView(RestOutputData data) { }
                @Override public void prepareSaveRetryView(boolean saved) { }
            });
            Future<?> pending = worker.submit(rest::execute);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            assertThrows(IllegalStateException.class, () -> state.startExam("CS101", "exam", 40));
            assertThrows(Exception.class, () -> state.startChallenge("CS101", "class", 20));
            release.countDown();
            pending.get(5, TimeUnit.SECONDS);
            assertFalse(state.hasActiveActivity());
            assertEquals(2, state.getCurrentDay().getDayNumber());
        } finally {
            release.countDown();
            worker.shutdownNow();
        }
    }
}
