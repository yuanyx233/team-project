package data_access.challenge;

import data_access.FileGameStateDataAccessObject;
import entity.AcademicRecord;
import entity.ChallengeResult;
import entity.Course;
import entity.CourseProgress;
import entity.DailyWeather;
import entity.GameDay;
import entity.Major;
import entity.Player;
import entity.Question;
import entity.WeatherCondition;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.challenge.ChallengeInputData;
import use_case.challenge.ChallengeOutputBoundary;
import use_case.challenge.ChallengeOutputData;
import use_case.challenge.ChallengeStateException;
import use_case.challenge.ClassChallengeInteractor;
import use_case.continue_game.ContinueGameInteractor;
import use_case.continue_game.ContinueGameOutputBoundary;
import use_case.continue_game.ContinueGameOutputData;
import use_case.question.QuestionGateway;
import use_case.question.QuestionLoadException;
import use_case.question.QuestionPurpose;
import use_case.question.QuestionTopic;
import use_case.rest.RestInteractor;
import use_case.rest.RestOutputBoundary;
import use_case.rest.RestOutputData;
import static org.junit.jupiter.api.Assertions.*;
import static use_case.challenge.ChallengeInputData.Action;

class SharedChallengeStateGatewayTest {
    private static final Map<String, QuestionTopic> TOPICS = Map.of(
            "first", QuestionTopic.PROGRAMMING_FOUNDATION, "second", QuestionTopic.SOFTWARE_DESIGN);
    private final AtomicReference<Player> players = new AtomicReference<>();
    private final AtomicReference<AcademicRecord> records = new AtomicReference<>();
    private SharedChallengeStateGateway state;
    @TempDir Path directory;

    @BeforeEach void setUp() {
        players.set(new Player(100, 100));
        records.set(academicRecord());
        state = new SharedChallengeStateGateway(players::get, records::get, TOPICS);
    }

    private static AcademicRecord academicRecord() {
        AcademicRecord record = new AcademicRecord();
        record.confirmMajor(new Major("cs", "CS", "Two assigned courses.", List.of(
                new Course("first", "Programming Foundation", "computing"),
                new Course("second", "Software Design", "computing"))));
        return record;
    }

    private CourseProgress progress() { return records.get().getCourseProgress().get("first"); }

    private static List<Question> questions() {
        return List.of(new Question("one", "One?", List.of("A", "B"), 0, null),
                new Question("two", "Two?", List.of("A", "B"), 1, null),
                new Question("three", "Three?", List.of("A", "B"), 0, null));
    }

    private static final class Recorder implements ChallengeOutputBoundary {
        private ChallengeOutputData latest;
        @Override public void present(ChallengeOutputData output) { latest = output; }
        @Override public void presentError(String message) { fail(message); }
    }

    private static void command(ClassChallengeInteractor interactor, Recorder recorder,
                                Action action, int answer) throws Exception {
        ChallengeOutputData current = recorder.latest;
        interactor.execute(new ChallengeInputData(action, "first", current == null ? "" : current.attemptId,
                current == null ? 0 : current.questionIndex, answer, false));
    }

    @Test void classChallengeRecordsIntoTheActualAcademicRecordAfterFinalConfirmation() throws Exception {
        Recorder recorder = new Recorder();
        ClassChallengeInteractor interactor = new ClassChallengeInteractor(state, (topic, purpose, count) -> {
            assertEquals(QuestionTopic.PROGRAMMING_FOUNDATION, topic);
            assertEquals(QuestionPurpose.PRACTICE, purpose);
            assertEquals(3, count);
            assertEquals(100, players.get().getEnergy());
            assertFalse(players.get().hasActiveActivity());
            return questions();
        }, recorder);
        command(interactor, recorder, Action.SELECT, -1);
        command(interactor, recorder, Action.START, -1);
        assertEquals(80, players.get().getEnergy());
        assertTrue(players.get().hasActiveActivity());
        for (int index = 0; index < 3; index++) {
            command(interactor, recorder, Action.ANSWER, 0);
            assertTrue(progress().getChallengeResult().isEmpty());
            assertEquals(0, progress().getPreparationPoints());
            command(interactor, recorder, Action.NEXT, -1);
        }
        command(interactor, recorder, Action.NEXT, -1);
        command(interactor, recorder, Action.SELECT, -1);
        assertEquals(ChallengeOutputData.Stage.RESULT, recorder.latest.stage);
        assertEquals(2, progress().getChallengeResult().orElseThrow().getCorrectCount());
        assertEquals(2, progress().getPreparationPoints());
        assertEquals(80, players.get().getEnergy());
        assertFalse(players.get().hasActiveActivity());
        assertEquals(0, records.get().getCourseProgress().get("second").getPreparationPoints());
    }

    @Test void failedApiCallDoesNotChargeOrLockTheRealPlayer() throws Exception {
        Recorder recorder = new Recorder();
        ClassChallengeInteractor interactor = new ClassChallengeInteractor(state,
                (topic, purpose, count) -> { throw new QuestionLoadException("offline"); }, recorder);
        command(interactor, recorder, Action.SELECT, -1);
        assertThrows(QuestionLoadException.class, () -> command(interactor, recorder, Action.START, -1));
        assertEquals(100, players.get().getEnergy());
        assertFalse(players.get().hasActiveActivity());
        assertTrue(progress().getChallengeResult().isEmpty());
    }

    @Test void startRechecksRealEnergyAndForeignActivityAfterQuestionLoading() throws Exception {
        Recorder recorder = new Recorder();
        QuestionGateway spendWhileLoading = (topic, purpose, count) -> {
            players.get().spendEnergy(90);
            return questions();
        };
        ClassChallengeInteractor interactor = new ClassChallengeInteractor(state, spendWhileLoading, recorder);
        command(interactor, recorder, Action.SELECT, -1);
        assertThrows(ChallengeStateException.class, () -> command(interactor, recorder, Action.START, -1));
        assertEquals(10, players.get().getEnergy());
        assertFalse(players.get().hasActiveActivity());

        players.get().restoreEnergyToPercent(100);
        ClassChallengeInteractor other = new ClassChallengeInteractor(state, (topic, purpose, count) -> {
            players.get().startActivity("exam");
            return questions();
        }, recorder);
        command(other, recorder, Action.SELECT, -1);
        assertThrows(ChallengeStateException.class, () -> command(other, recorder, Action.START, -1));
        assertEquals(100, players.get().getEnergy());
        assertTrue(players.get().finishActivity("exam"));
    }

    @Test void duplicateRequestsDoNotChargeOrAwardAgainAndCannotOverwriteTheResult() throws Exception {
        state.startChallenge("first", "attempt", 20);
        state.startChallenge("first", "attempt", 20);
        state.completeChallenge("first", "attempt", new ChallengeResult(2, 3));
        players.get().startActivity("exam");
        state.startChallenge("first", "attempt", 20);
        state.completeChallenge("first", "attempt", new ChallengeResult(2, 3));
        state.abandonChallenge("attempt");
        assertThrows(ChallengeStateException.class,
                () -> state.completeChallenge("first", "attempt", new ChallengeResult(3, 3)));
        assertThrows(IllegalStateException.class,
                () -> progress().recordChallengeResult(new ChallengeResult(0, 3)));
        assertEquals(80, players.get().getEnergy());
        assertEquals(2, progress().getPreparationPoints());
        assertTrue(players.get().finishActivity("exam"));
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("first", "new", 20));
    }

    @Test void leavingKeepsTheCostAndDoesNotReleaseALaterExamOrAwardPoints() throws Exception {
        state.startChallenge("first", "attempt", 20);
        state.abandonChallenge("attempt");
        players.get().startActivity("exam");
        state.abandonChallenge("attempt");
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("first", "attempt", 20));
        assertThrows(ChallengeStateException.class,
                () -> state.completeChallenge("first", "attempt", new ChallengeResult(3, 3)));
        assertEquals(80, players.get().getEnergy());
        assertTrue(progress().getChallengeResult().isEmpty());
        assertEquals(0, progress().getPreparationPoints());
        assertTrue(players.get().finishActivity("exam"));
        state.startChallenge("first", "retry", 20);
        assertEquals(60, players.get().getEnergy());
    }

    @Test void wrongCourseAttemptAndLostLockCannotWriteProgressOrReleaseAnotherActivity() throws Exception {
        state.startChallenge("first", "attempt", 20);
        ChallengeResult result = new ChallengeResult(3, 3);
        assertThrows(ChallengeStateException.class, () -> state.completeChallenge("second", "attempt", result));
        assertThrows(ChallengeStateException.class, () -> state.completeChallenge("first", "unknown", result));
        assertThrows(ChallengeStateException.class, () -> state.abandonChallenge("unknown"));
        assertTrue(players.get().finishActivity("attempt"));
        players.get().startActivity("exam");
        assertThrows(ChallengeStateException.class, () -> state.completeChallenge("first", "attempt", result));
        assertThrows(ChallengeStateException.class, () -> state.abandonChallenge("attempt"));
        assertTrue(progress().getChallengeResult().isEmpty());
        assertEquals(80, players.get().getEnergy());
        assertTrue(players.get().finishActivity("exam"));
    }

    @Test void unassignedCoursesInvalidRequestsAndInsufficientEnergyChangeNothing() throws Exception {
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("missing", "attempt", 20));
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("first", "", 20));
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("first", "attempt", -1));
        assertThrows(NullPointerException.class, () -> progress().recordChallengeResult(null));
        players.set(new Player(19, 100));
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("first", "attempt", 20));
        assertEquals(19, players.get().getEnergy());
        assertFalse(players.get().hasActiveActivity());
        records.set(new AcademicRecord());
        assertTrue(state.loadOverview().getCourses().isEmpty());
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("first", "attempt", 0));
    }

    @Test void courseTopicsMustBeExplicitAndAreNeverInferredFromNamesOrBroadCategories() throws Exception {
        SharedChallengeStateGateway unmapped = new SharedChallengeStateGateway(players::get, records::get, Map.of());
        assertThrows(ChallengeStateException.class, unmapped::loadOverview);
        assertThrows(ChallengeStateException.class, () -> unmapped.startChallenge("first", "attempt", 20));
        assertEquals(100, players.get().getEnergy());
        assertFalse(players.get().hasActiveActivity());
        assertEquals(QuestionTopic.PROGRAMMING_FOUNDATION, state.loadOverview().getCourses().get(0).getTopic());
        assertEquals(QuestionTopic.SOFTWARE_DESIGN, state.loadOverview().getCourses().get(1).getTopic());
    }

    @Test void suppliersReadReplacedStateAndOldAttemptsCannotChangeIt() throws Exception {
        state.startChallenge("first", "old", 20);
        state.completeChallenge("first", "old", new ChallengeResult(3, 3));
        Player oldPlayer = players.get();
        CourseProgress oldProgress = progress();
        players.set(new Player(35, 100));
        records.set(academicRecord());
        assertEquals(35, state.loadOverview().getEnergy());
        assertTrue(state.loadOverview().getCourses().get(0).getResult().isEmpty());
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("first", "old", 20));
        assertThrows(ChallengeStateException.class,
                () -> state.completeChallenge("first", "old", new ChallengeResult(3, 3)));
        assertThrows(ChallengeStateException.class, () -> state.abandonChallenge("old"));
        state.startChallenge("first", "new", 20);
        assertEquals(15, players.get().getEnergy());
        assertEquals(80, oldPlayer.getEnergy());
        assertEquals(3, oldProgress.getPreparationPoints());
        assertEquals(0, progress().getPreparationPoints());
    }

    @Test void concurrentDuplicateStartsSpendEnergyOnlyOnce() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Void> start = () -> { state.startChallenge("first", "attempt", 20); return null; };
            for (var future : pool.invokeAll(List.of(start, start))) { future.get(); }
        }
        finally { pool.shutdownNow(); }
        assertEquals(80, players.get().getEnergy());
        state.completeChallenge("first", "attempt", new ChallengeResult(1, 3));
        assertEquals(1, progress().getPreparationPoints());
    }

    @Test void restoredCompletedResultCanBeViewedAtZeroEnergyWithoutLoadingQuestions() throws Exception {
        progress().recordChallengeResult(new ChallengeResult(2, 3));
        players.set(new Player(0, 100));
        Recorder recorder = new Recorder();
        ClassChallengeInteractor interactor = new ClassChallengeInteractor(state, (topic, purpose, count) -> {
            fail("Viewing completed work must not load questions.");
            return List.of();
        }, recorder);
        command(interactor, recorder, Action.SELECT, -1);
        command(interactor, recorder, Action.START, -1);
        assertEquals(ChallengeOutputData.Stage.RESULT, recorder.latest.stage);
        assertEquals(2, recorder.latest.result.getPreparationPoints());
        assertFalse(players.get().hasActiveActivity());
    }

    @Test void realPlayerLockBlocksRestAndContinueUntilTheChallengeLeaves() throws Exception {
        Path checkpoint = directory.resolve("checkpoint.json");
        FileGameStateDataAccessObject game = new FileGameStateDataAccessObject(checkpoint,
                new GameDay(1, LocalDate.of(2025, 9, 2)), players.get());
        game.saveCheckpoint();
        String saved = Files.readString(checkpoint);
        SharedChallengeStateGateway shared = new SharedChallengeStateGateway(game::getPlayer, records::get, TOPICS);
        shared.startChallenge("first", "attempt", 20);
        AtomicReference<String> restFailure = new AtomicReference<>();
        RestInteractor rest = new RestInteractor(game, date ->
                new DailyWeather(date, WeatherCondition.CLEAR, 20, 10), new RestOutputBoundary() {
                    @Override public void prepareSuccessView(RestOutputData output) { }
                    @Override public void prepareFailView(String message) { restFailure.set(message); }
                    @Override public void prepareSaveRetryView(boolean saved) { }
                });
        AtomicReference<String> continueFailure = new AtomicReference<>();
        ContinueGameInteractor resume = new ContinueGameInteractor(game, new ContinueGameOutputBoundary() {
            @Override public void prepareSuccessView(ContinueGameOutputData output) { fail("Must not load."); }
            @Override public void prepareFailView(String message) { continueFailure.set(message); }
        });
        rest.execute();
        resume.execute();
        assertNotNull(restFailure.get());
        assertNotNull(continueFailure.get());
        assertEquals(1, game.getCurrentDay().getDayNumber());
        assertEquals(80, game.getPlayer().getEnergy());
        assertEquals(saved, Files.readString(checkpoint));
        shared.abandonChallenge("attempt");
        rest.execute();
        assertEquals(2, game.getCurrentDay().getDayNumber());
        assertEquals(100, game.getPlayer().getEnergy());
        assertFalse(game.getPlayer().hasActiveActivity());
    }
}
