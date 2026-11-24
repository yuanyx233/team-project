package view;

import data_access.FileGameStateDataAccessObject;
import data_access.InMemoryMajorCatalog;
import entity.ChallengeResult;
import entity.GameDay;
import entity.Player;
import entity.Question;
import interface_adapter.exam.ExamController;
import interface_adapter.exam.ExamPresenter;
import interface_adapter.exam.ExamViewModel;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.exam.ExamInteractor;
import use_case.exam.ExamOutputData.Stage;

import static org.junit.jupiter.api.Assertions.*;

class ExamViewTest {
    @TempDir Path directory;
    private ExamView view;
    private ExamViewModel model;

    @Test
    void playBattleWithSkillsThroughSwingButtons() throws Exception {
        FileGameStateDataAccessObject state = new FileGameStateDataAccessObject(directory.resolve("save.json"),
                new GameDay(1, LocalDate.of(2025, 9, 2)), new Player(100, 100));
        state.getAcademicRecord().confirmMajor(InMemoryMajorCatalog.predefined().findById("cs").orElseThrow());
        state.startChallenge("CS101", "class", 20);
        state.completeChallenge("CS101", "class", new ChallengeResult(2, 3));
        List<Question> questions = IntStream.range(0, 5).mapToObj(i -> new Question("q" + i, "Question " + i,
                List.of("Correct", "Wrong", "Wrong two", "Wrong three"), 0, null)).collect(Collectors.toList());
        AtomicBoolean returned = new AtomicBoolean();
        SwingUtilities.invokeAndWait(() -> {
            model = new ExamViewModel();
            ExamPresenter presenter = new ExamPresenter(model);
            view = new ExamView(new ExamController(new ExamInteractor(state,
                    (topic, purpose, count) -> questions, presenter)), model, () -> returned.set(true));
            view.refresh();
        });
        waitFor(() -> ready("View Exam"));
        click("View Exam");
        waitFor(() -> ready("Start Boss Battle"));
        click("Start Boss Battle");
        waitFor(() -> ready("Hint"));
        click("Hint");
        waitFor(() -> model.getState().eliminatedOption == 1 && ready("Shield"));
        SwingUtilities.invokeAndWait(() -> assertFalse(button(view, "B. Wrong").isEnabled()));
        click("Shield");
        waitFor(() -> model.getState().shieldActive && ready("Submit Answer"));
        click("C. Wrong two");
        click("Submit Answer");
        waitFor(() -> ready("Next Question"));
        assertEquals(60, model.getState().playerHp);
        assertFalse(model.getState().shieldActive);
        click("Next Question");
        for (int i = 0; i < 3; i++) {
            waitFor(() -> ready("Submit Answer"));
            click("A. Correct");
            click("Submit Answer");
            String next = i == 2 ? "View Result" : "Next Question";
            waitFor(() -> ready(next));
            click(next);
        }
        waitFor(() -> model.getState().stage == Stage.RESULT && ready("Back to Street"));
        assertTrue(model.getState().result.contains("Grade: B"));
        assertEquals(40, state.getPlayer().getEnergy());
        assertNotNull(state.getAcademicRecord().getCourseProgress().get("CS101").getExamResult());
        click("Back to Street");
        waitFor(returned::get);
    }

    private void click(String text) throws Exception {
        SwingUtilities.invokeAndWait(() -> button(view, text).doClick());
    }

    private boolean ready(String text) {
        AbstractButton button = button(view, text);
        return button != null && button.isEnabled();
    }

    private AbstractButton button(Container parent, String text) {
        for (Component component : parent.getComponents()) {
            if (component instanceof AbstractButton && text.equals(((AbstractButton) component).getText())) {
                return (AbstractButton) component;
            }
            if (component instanceof Container) {
                AbstractButton found = button((Container) component, text);
                if (found != null) { return found; }
            }
        }
        return null;
    }

    private void waitFor(BooleanSupplier condition) throws Exception {
        AtomicBoolean ready = new AtomicBoolean();
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (!ready.get() && System.nanoTime() < deadline) {
            SwingUtilities.invokeAndWait(() -> ready.set(condition.getAsBoolean()));
            if (!ready.get()) { Thread.sleep(10); }
        }
        assertTrue(ready.get(), "Exam page did not finish updating");
    }
}
