package entity;

import java.util.Objects;
import java.util.Optional;

/** Completed work for one assigned course. Exam progress is owned by UC3. */
public final class CourseProgress {
    private final String courseId;
    private ChallengeResult challengeResult;
    private ExamResult examResult;

    public CourseProgress(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course ID cannot be blank.");
        }
        this.courseId = courseId;
    }

    public String getCourseId() { return courseId; }

    public synchronized Optional<ChallengeResult> getChallengeResult() {
        return Optional.ofNullable(challengeResult);
    }

    public synchronized int getPreparationPoints() {
        return challengeResult == null ? 0 : challengeResult.getPreparationPoints();
    }

    /** Records completed work once; callers must handle retries before calling this method. */
    public synchronized void recordChallengeResult(ChallengeResult result) {
        Objects.requireNonNull(result, "Challenge result cannot be null.");
        if (challengeResult != null) {
            throw new IllegalStateException("This course's class challenge is already completed.");
        }
        challengeResult = result;
    }

    public synchronized ExamResult getExamResult() { return examResult; }

    public synchronized void completeExam(ExamResult result) {
        Objects.requireNonNull(result);
        if (examResult != null) {
            throw new IllegalStateException("This exam is already completed.");
        }
        examResult = result;
    }
}
