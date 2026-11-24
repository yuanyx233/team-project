package interface_adapter.exam;

import java.util.List;
import java.util.stream.Collectors;
import use_case.exam.ExamOutputBoundary;
import use_case.exam.ExamOutputData;

public final class ExamPresenter implements ExamOutputBoundary {
    private final ExamViewModel model;

    public ExamPresenter(ExamViewModel model) { this.model = model; }

    @Override
    public void present(ExamOutputData data) {
        List<ExamViewModel.CourseItem> courses = data.courses.stream().map(course ->
                new ExamViewModel.CourseItem(course.getId(), course.getName()
                        + (data.completedCourses.contains(course.getId()) ? " (completed)" : "")))
                .collect(Collectors.toList());
        String instructions = "Up to 5 questions. Entry costs 40 Energy. Both fighters start at 60 HP.\n"
                + "Correct: deal 20 damage. Incorrect: take 20 damage.\n"
                + "Preparation points: " + data.preparationPoints + ". Hint needs 1; Shield needs 2.\n"
                + "Each skill is usable once. Win or lose, this exam cannot be retaken.";
        String result = data.result == null ? "" : String.format("%s\n%d / %d correct\nScore: %.1f%%\nGrade: %s",
                data.result.hasWon() ? "Final Monster defeated!" : "Exam battle lost.",
                data.result.getCorrectCount(), data.result.getAnsweredCount(),
                data.result.getScore(), data.result.getGrade());
        model.setState(new ExamViewModel.State(data.stage, courses, data.courseId, data.courseName,
                instructions, "Energy: " + data.energy, data.attemptId, data.questionIndex, data.question,
                data.options, data.eliminatedOption, data.playerHp, data.bossHp, data.hintAvailable,
                data.shieldAvailable, data.shieldActive, data.finished, data.feedback, result));
    }

    @Override public void presentError(String message) { model.setError(message); }
}
