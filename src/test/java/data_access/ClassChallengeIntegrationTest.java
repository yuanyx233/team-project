package data_access;

import entity.AcademicRecord;
import entity.ChallengeResult;
import entity.Course;
import entity.DailyWeather;
import entity.GameDay;
import entity.Major;
import entity.Player;
import entity.Question;
import entity.WeatherCondition;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import use_case.exam.ExamInputData;
import use_case.exam.ExamInteractor;
import use_case.exam.ExamOutputBoundary;
import use_case.exam.ExamOutputData;
import use_case.question.QuestionGateway;
import use_case.question.QuestionLoadException;
import use_case.question.QuestionTopic;
import use_case.rest.RestInteractor;
import use_case.rest.RestOutputBoundary;
import use_case.rest.RestOutputData;
import static org.junit.jupiter.api.Assertions.*;

class ClassChallengeIntegrationTest {
    @TempDir Path directory;

    private FileGameStateDataAccessObject game() {
        FileGameStateDataAccessObject state = new FileGameStateDataAccessObject(directory.resolve("save.json"),
                new GameDay(1, LocalDate.of(2025, 9, 2)), new Player(100, 100));
        state.getAcademicRecord().confirmMajor(InMemoryMajorCatalog.predefined().findById("cs").orElseThrow());
        return state;
    }

    private static final class ChallengeRecorder implements ChallengeOutputBoundary {
        private ChallengeOutputData latest;
        @Override public void present(ChallengeOutputData output) { latest = output; }
        @Override public void presentError(String message) { fail(message); }
    }

    private static final class ExamRecorder implements ExamOutputBoundary {
        private ExamOutputData latest;
        @Override public void present(ExamOutputData output) { latest = output; }
        @Override public void presentError(String message) { fail(message); }
    }

    private static void challenge(ClassChallengeInteractor useCase, ChallengeRecorder recorder,
                                  ChallengeInputData.Action action, int answer) throws Exception {
        ChallengeOutputData current = recorder.latest;
        useCase.execute(new ChallengeInputData(action, "CS101", current == null ? "" : current.attemptId,
                current == null ? 0 : current.questionIndex, answer, false));
    }

    private static void exam(ExamInteractor useCase, ExamRecorder recorder,
                             ExamInputData.Action action, int answer) {
        ExamOutputData current = recorder.latest;
        useCase.execute(new ExamInputData(action, "CS101", current == null ? "" : current.attemptId,
                current == null ? 0 : current.questionIndex, answer, false));
    }

    @Test void classPointsUnlockExamSkillsAndBothResultsSurviveRestAndContinue() throws Exception {
        FileGameStateDataAccessObject state = game();
        AtomicInteger loads = new AtomicInteger();
        QuestionGateway questions = (topic, purpose, count) -> {
            loads.incrementAndGet();
            assertEquals(QuestionTopic.PROGRAMMING_FOUNDATION, topic);
            return IntStream.range(0, count).mapToObj(index -> new Question(purpose + "-" + index,
                    "Test question " + index, List.of("Correct", "Wrong", "Wrong two", "Wrong three"),
                    0, null)).collect(Collectors.toList());
        };
        ChallengeRecorder classroom = new ChallengeRecorder();
        ClassChallengeInteractor lesson = new ClassChallengeInteractor(state, questions, classroom);
        challenge(lesson, classroom, ChallengeInputData.Action.SELECT, -1);
        challenge(lesson, classroom, ChallengeInputData.Action.START, -1);
        for (int answer : new int[]{0, 1, 0}) {
            challenge(lesson, classroom, ChallengeInputData.Action.ANSWER, answer);
            challenge(lesson, classroom, ChallengeInputData.Action.NEXT, -1);
        }
        assertEquals(2, classroom.latest.result.getPreparationPoints());
        String classAttempt = classroom.latest.attemptId;
        assertEquals(80, state.getPlayer().getEnergy());
        assertFalse(state.hasActiveActivity());

        ExamRecorder battle = new ExamRecorder();
        ExamInteractor finalExam = new ExamInteractor(state, questions, battle);
        exam(finalExam, battle, ExamInputData.Action.SELECT, -1);
        exam(finalExam, battle, ExamInputData.Action.START, -1);
        assertTrue(battle.latest.hintAvailable);
        assertTrue(battle.latest.shieldAvailable);
        exam(finalExam, battle, ExamInputData.Action.HINT, -1);
        exam(finalExam, battle, ExamInputData.Action.SHIELD, -1);
        exam(finalExam, battle, ExamInputData.Action.ANSWER, 2);
        assertEquals(60, battle.latest.playerHp);
        exam(finalExam, battle, ExamInputData.Action.NEXT, -1);
        for (int index = 0; index < 3; index++) {
            exam(finalExam, battle, ExamInputData.Action.ANSWER, 0);
            exam(finalExam, battle, ExamInputData.Action.NEXT, -1);
        }
        assertEquals(ExamOutputData.Stage.RESULT, battle.latest.stage);
        assertEquals("B", battle.latest.result.getGrade());
        assertEquals(2, battle.latest.preparationPoints);
        assertEquals(40, state.getPlayer().getEnergy());
        AtomicReference<RestOutputData> rested = new AtomicReference<>();
        new RestInteractor(state, date -> new DailyWeather(date, WeatherCondition.CLEAR, 20, 10),
                new RestOutputBoundary() {
                    @Override public void prepareSuccessView(RestOutputData output) { rested.set(output); }
                    @Override public void prepareFailView(String message) { fail(message); }
                    @Override public void prepareSaveRetryView(boolean saved) { fail("No retry is needed."); }
                }).execute();
        assertTrue(rested.get().isSaved());
        assertEquals(100, state.getPlayer().getEnergy());
        Player beforeLoad = state.getPlayer();
        AcademicRecord recordBeforeLoad = state.getAcademicRecord();
        state.startChallenge("CS201", "unsaved", 20);
        state.completeChallenge("CS201", "unsaved", new ChallengeResult(1, 3));
        AtomicReference<ContinueGameOutputData> resumed = new AtomicReference<>();
        new ContinueGameInteractor(state, new ContinueGameOutputBoundary() {
            @Override public void prepareSuccessView(ContinueGameOutputData output) { resumed.set(output); }
            @Override public void prepareFailView(String message) { fail(message); }
        }).execute();
        assertEquals(2, resumed.get().getDayNumber());
        assertEquals(100, resumed.get().getEnergy());
        assertNotSame(beforeLoad, state.getPlayer());
        assertNotSame(recordBeforeLoad, state.getAcademicRecord());
        assertThrows(ChallengeStateException.class,
                () -> state.completeChallenge("CS101", classAttempt, new ChallengeResult(2, 3)));
        challenge(lesson, classroom, ChallengeInputData.Action.OPEN, -1);
        assertEquals(100, classroom.latest.overview.getEnergy());
        assertTrue(classroom.latest.overview.getCourses().get(1).getResult().isEmpty());
        challenge(lesson, classroom, ChallengeInputData.Action.SELECT, -1);
        challenge(lesson, classroom, ChallengeInputData.Action.START, -1);
        assertEquals(ChallengeOutputData.Stage.RESULT, classroom.latest.stage);
        assertEquals(2, classroom.latest.result.getPreparationPoints());
        exam(finalExam, battle, ExamInputData.Action.OPEN, -1);
        exam(finalExam, battle, ExamInputData.Action.SELECT, -1);
        exam(finalExam, battle, ExamInputData.Action.START, -1);
        assertEquals("B", battle.latest.result.getGrade());
        assertEquals(2, loads.get(), "Viewing saved results must not reload questions.");
        assertEquals(100, state.getPlayer().getEnergy());
        assertTrue(state.getAcademicRecord().getCourseProgress().get("CS201").getChallengeResult().isEmpty());
    }

    @Test void repeatedLeaveDoesNotReleaseALaterExamOrAllowTheAbandonedAttemptToRestart() throws Exception {
        FileGameStateDataAccessObject state = game();
        state.startChallenge("CS101", "class", 20);
        state.abandonChallenge("class");
        state.startExam("CS101", "exam", 40);
        assertDoesNotThrow(() -> state.abandonChallenge("class"));
        assertTrue(state.getPlayer().hasActiveActivity());
        state.abandonExam("exam");
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("CS101", "class", 20));
        assertEquals(40, state.getPlayer().getEnergy());
        assertEquals(0, state.getAcademicRecord().getCourseProgress().get("CS101").getPreparationPoints());
    }

    @Test void completedAttemptsCannotAcceptAnotherCourseOrAChangedResult() throws Exception {
        FileGameStateDataAccessObject state = game();
        state.startChallenge("CS101", "class", 20);
        state.completeChallenge("CS101", "class", new ChallengeResult(2, 3));
        state.startExam("CS101", "exam", 40);
        assertThrows(ChallengeStateException.class,
                () -> state.completeChallenge("CS201", "class", new ChallengeResult(2, 3)));
        assertThrows(ChallengeStateException.class,
                () -> state.completeChallenge("CS101", "class", new ChallengeResult(3, 3)));
        state.completeChallenge("CS101", "class", new ChallengeResult(2, 3));
        assertTrue(state.getPlayer().hasActiveActivity());
        assertEquals(2, state.getAcademicRecord().getCourseProgress().get("CS101").getPreparationPoints());
        assertTrue(state.getAcademicRecord().getCourseProgress().get("CS201").getChallengeResult().isEmpty());
        state.abandonExam("exam");
    }

    @Test void anUnmappedAssignedCourseFailsBeforeThePlayerIsChargedOrLocked() {
        FileGameStateDataAccessObject state = new FileGameStateDataAccessObject(directory.resolve("save.json"),
                new GameDay(1, LocalDate.of(2025, 9, 2)), new Player(100, 100));
        state.getAcademicRecord().confirmMajor(new Major("custom", "Custom", "Unsupported topic.", List.of(
                new Course("CS101", "Programming Foundation", "computing"),
                new Course("unmapped", "Unknown course", "computing"))));
        assertThrows(ChallengeStateException.class, () -> state.startChallenge("CS101", "class", 20));
        assertEquals(100, state.getPlayer().getEnergy());
        assertFalse(state.hasActiveActivity());
    }

    @Test void theIntegratedQuestionFailureLeavesEnergyProgressAndTheSharedLockUnchanged() throws Exception {
        FileGameStateDataAccessObject state = game();
        ChallengeRecorder recorder = new ChallengeRecorder();
        ClassChallengeInteractor lesson = new ClassChallengeInteractor(state,
                (topic, purpose, count) -> { throw new QuestionLoadException("offline"); }, recorder);
        challenge(lesson, recorder, ChallengeInputData.Action.SELECT, -1);
        assertThrows(QuestionLoadException.class,
                () -> challenge(lesson, recorder, ChallengeInputData.Action.START, -1));
        assertEquals(100, state.getPlayer().getEnergy());
        assertFalse(state.hasActiveActivity());
        assertTrue(state.getAcademicRecord().getCourseProgress().get("CS101").getChallengeResult().isEmpty());
    }

    @Test void allSixAssignedCoursesUseTheSameTopicForClassChallengesAndExams() throws Exception {
        Map<String, QuestionTopic> expected = Map.of(
                "CS101", QuestionTopic.PROGRAMMING_FOUNDATION, "CS201", QuestionTopic.SOFTWARE_DESIGN,
                "BIO101", QuestionTopic.CELL_BIOLOGY, "BIO201", QuestionTopic.ECOLOGY,
                "HIST101", QuestionTopic.WORLD_HISTORY, "HIST201", QuestionTopic.HISTORICAL_PERSPECTIVE);
        for (Major major : InMemoryMajorCatalog.predefined().getMajors()) {
            FileGameStateDataAccessObject state = new FileGameStateDataAccessObject(directory.resolve("save.json"),
                    new GameDay(1, LocalDate.of(2025, 9, 2)), new Player(100, 100));
            state.getAcademicRecord().confirmMajor(major);
            for (var course : state.loadOverview().getCourses()) {
                assertEquals(expected.get(course.getId()), course.getTopic());
                assertEquals(course.getTopic(), state.getQuestionTopic(course.getId()));
            }
        }
    }
}
