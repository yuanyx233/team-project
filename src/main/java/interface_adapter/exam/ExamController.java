package interface_adapter.exam;

import use_case.exam.ExamInputBoundary;
import use_case.exam.ExamInputData;
import static use_case.exam.ExamInputData.Action;

public final class ExamController {
    private final ExamInputBoundary input;

    public ExamController(ExamInputBoundary input) { this.input = input; }

    public void open() { send(Action.OPEN, "", "", 0, -1, false); }
    public void select(String courseId) { send(Action.SELECT, courseId, "", 0, -1, false); }
    public void start(String courseId) { send(Action.START, courseId, "", 0, -1, false); }
    public void answer(String attempt, int question, int answer) {
        send(Action.ANSWER, "", attempt, question, answer, false);
    }
    public void next(String attempt, int question) { send(Action.NEXT, "", attempt, question, -1, false); }
    public void hint(String attempt, int question) { send(Action.HINT, "", attempt, question, -1, false); }
    public void shield(String attempt, int question) { send(Action.SHIELD, "", attempt, question, -1, false); }
    public void leave(boolean confirmed) { send(Action.LEAVE, "", "", 0, -1, confirmed); }

    private void send(Action action, String course, String attempt, int question, int answer, boolean confirmed) {
        input.execute(new ExamInputData(action, course, attempt, question, answer, confirmed));
    }
}
