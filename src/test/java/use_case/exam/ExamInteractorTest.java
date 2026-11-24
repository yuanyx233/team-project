package use_case.exam;

import data_access.FileGameStateDataAccessObject;
import data_access.InMemoryMajorCatalog;
import entity.GameDay;
import entity.Player;
import entity.Question;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.question.QuestionLoadException;
import use_case.question.QuestionPurpose;
import static use_case.exam.ExamInputData.Action;
import static org.junit.jupiter.api.Assertions.*;

class ExamInteractorTest implements ExamOutputBoundary {
    @TempDir Path directory;
    private FileGameStateDataAccessObject state;
    private ExamInteractor exam;
    private ExamOutputData data;
    private String error;

    @BeforeEach
    void setup() {
        state = new FileGameStateDataAccessObject(directory.resolve("save.json"),
                new GameDay(1, LocalDate.of(2025, 9, 2)), new Player(100, 100));
        state.getAcademicRecord().confirmMajor(InMemoryMajorCatalog.predefined().findById("cs").orElseThrow());
        exam = new ExamInteractor(state, (topic, purpose, count) -> {
            assertEquals(QuestionPurpose.EXAM, purpose);
            assertEquals(5, count);
            return questions();
        }, this);
    }

    private List<Question> questions() {
        return IntStream.range(0, 5).mapToObj(i -> new Question("q" + i, "Question " + i,
                List.of("Correct", "Wrong", "Also wrong", "Wrong too"), 0, null)).collect(Collectors.toList());
    }

    private void act(Action action, int answer) {
        exam.execute(new ExamInputData(action, "CS101", data == null ? "" : data.attemptId,
                data == null ? 0 : data.questionIndex, answer, true));
    }

    private void start() {
        act(Action.SELECT, -1);
        act(Action.START, -1);
    }

    @Test
    void winRecordsResultAndCannotRestart() {
        start();
        assertEquals(60, state.getPlayer().getEnergy());
        for (int i = 0; i < 3; i++) {
            act(Action.ANSWER, 0);
            act(Action.NEXT, -1);
        }
        assertEquals(ExamOutputData.Stage.RESULT, data.stage);
        assertEquals(100, data.result.getScore());
        assertNotNull(state.getAcademicRecord().getCourseProgress().get("CS101").getExamResult());
        act(Action.START, -1);
        assertEquals(60, state.getPlayer().getEnergy());
    }

    @Test
    void failureIsRecordedBeforeLeavingFinalFeedback() {
        start();
        for (int i = 0; i < 3; i++) {
            act(Action.ANSWER, 1);
            if (i < 2) { act(Action.NEXT, -1); }
        }
        assertFalse(data.result.hasWon());
        assertNotNull(state.getAcademicRecord().getCourseProgress().get("CS101").getExamResult());
        act(Action.LEAVE, -1);
        assertEquals(ExamOutputData.Stage.LEFT, data.stage);
        assertFalse(state.hasActiveActivity());
    }

    @Test
    void blankAndRepeatedAnswersDoNotCauseExtraDamage() {
        start();
        act(Action.ANSWER, -1);
        assertNotNull(error);
        assertEquals(60, data.playerHp);
        act(Action.ANSWER, 1);
        act(Action.ANSWER, 1);
        assertEquals(40, data.playerHp);
    }

    @Test
    void apiFailureDoesNotChargeOrLockState() {
        exam = new ExamInteractor(state, (topic, purpose, count) -> {
            throw new QuestionLoadException("offline");
        }, this);
        start();
        assertEquals("offline", error);
        assertEquals(100, state.getPlayer().getEnergy());
        assertFalse(state.hasActiveActivity());
    }

    @Test
    void cancellationKeepsEnergyCostButNoGrade() {
        start();
        act(Action.LEAVE, -1);
        assertEquals(60, state.getPlayer().getEnergy());
        assertNull(state.getAcademicRecord().getCourseProgress().get("CS101").getExamResult());
        start();
        assertEquals(20, state.getPlayer().getEnergy());
    }

    @Test
    void coursePreparationUnlocksBothSkills() throws Exception {
        state.startChallenge("CS101", "class", 20);
        state.completeChallenge("CS101", "class", new entity.ChallengeResult(2, 3));
        start();
        act(Action.HINT, -1);
        assertEquals(1, data.eliminatedOption);
        act(Action.SHIELD, -1);
        act(Action.ANSWER, 2);
        assertEquals(60, data.playerHp);
        assertFalse(data.shieldActive);
    }

    @Override public void present(ExamOutputData next) { data = next; }
    @Override public void presentError(String message) { error = message; }
}
