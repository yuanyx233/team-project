package use_case.exam;

import entity.AcademicRecord;
import entity.Course;
import entity.ExamBattle;
import entity.ExamResult;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ExamOutputData {
    public enum Stage { COURSES, CONFIRM, QUESTION, FEEDBACK, RESULT, LEFT }

    public final Stage stage;
    public final List<Course> courses;
    public final Set<String> completedCourses;
    public final int energy;
    public final String courseId;
    public final String courseName;
    public final int preparationPoints;
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
    public final ExamResult result;

    public ExamOutputData(Stage stage, AcademicRecord record, int energy, Course course, ExamBattle battle,
                          String attemptId, int questionIndex, String question, List<String> options,
                          int eliminatedOption, String feedback, ExamResult result) {
        this.stage = stage;
        courses = record.hasConfirmedMajor() ? record.getSelectedMajor().getCourses() : List.of();
        completedCourses = record.getCourseProgress().values().stream()
                .filter(progress -> progress.getExamResult() != null).map(progress -> progress.getCourseId())
                .collect(Collectors.toUnmodifiableSet());
        this.energy = energy;
        courseId = course == null ? "" : course.getId();
        courseName = course == null ? "" : course.getName();
        preparationPoints = course == null ? 0 : record.getCourseProgress().get(courseId).getPreparationPoints();
        this.attemptId = attemptId;
        this.questionIndex = questionIndex;
        this.question = question;
        this.options = List.copyOf(options);
        this.eliminatedOption = eliminatedOption;
        playerHp = battle == null ? 60 : battle.getPlayerHp();
        bossHp = battle == null ? 60 : battle.getBossHp();
        hintAvailable = battle != null && battle.canUseHint();
        shieldAvailable = battle != null && battle.canUseShield();
        shieldActive = battle != null && battle.isShieldActive();
        finished = battle != null && battle.isFinished();
        this.feedback = feedback;
        this.result = result;
    }
}
