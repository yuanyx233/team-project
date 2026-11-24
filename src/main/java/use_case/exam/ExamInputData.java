package use_case.exam;

public final class ExamInputData {
    public enum Action { OPEN, SELECT, START, ANSWER, NEXT, HINT, SHIELD, LEAVE }

    public final Action action;
    public final String courseId;
    public final String attemptId;
    public final int questionIndex;
    public final int answerIndex;
    public final boolean confirmed;

    public ExamInputData(Action action, String courseId, String attemptId,
                         int questionIndex, int answerIndex, boolean confirmed) {
        this.action = action;
        this.courseId = courseId;
        this.attemptId = attemptId;
        this.questionIndex = questionIndex;
        this.answerIndex = answerIndex;
        this.confirmed = confirmed;
    }
}
