package interface_adapter.exam;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import use_case.exam.ExamOutputData.Stage;

public final class ExamViewModel {
    public static final class CourseItem {
        public final String id;
        public final String label;
        public CourseItem(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    public static final class State {
        public final Stage stage;
        public final List<CourseItem> courses;
        public final String courseId;
        public final String courseName;
        public final String instructions;
        public final String energy;
        public final String attemptId;
        public final int questionIndex;
        public final String question;
        public final List<String> options;
        public final int eliminatedOption;
        public final int playerHp;
        public final int bossHp;
        public final boolean hintAvailable;
        public final boolean shieldAvailable;
        public final boolean shieldActive;
        public final boolean finished;
        public final String feedback;
        public final String result;

        public State(Stage stage, List<CourseItem> courses, String courseId, String courseName,
                     String instructions, String energy, String attemptId, int questionIndex,
                     String question, List<String> options, int eliminatedOption, int playerHp, int bossHp,
                     boolean hintAvailable, boolean shieldAvailable, boolean shieldActive, boolean finished,
                     String feedback, String result) {
            this.stage = stage;
            this.courses = List.copyOf(courses);
            this.courseId = courseId;
            this.courseName = courseName;
            this.instructions = instructions;
            this.energy = energy;
            this.attemptId = attemptId;
            this.questionIndex = questionIndex;
            this.question = question;
            this.options = List.copyOf(options);
            this.eliminatedOption = eliminatedOption;
            this.playerHp = playerHp;
            this.bossHp = bossHp;
            this.hintAvailable = hintAvailable;
            this.shieldAvailable = shieldAvailable;
            this.shieldActive = shieldActive;
            this.finished = finished;
            this.feedback = feedback;
            this.result = result;
        }
    }

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private volatile State state;
    private volatile String error = "";

    public State getState() { return state; }
    public String getError() { return error; }
    public void addListener(PropertyChangeListener listener) { changes.addPropertyChangeListener(listener); }

    void setState(State next) {
        state = next;
        error = "";
        changes.firePropertyChange("state", null, next);
    }

    void setError(String message) {
        error = message;
        changes.firePropertyChange("error", null, message);
    }
}
