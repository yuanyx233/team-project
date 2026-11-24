package entity;

/** Initial progress record. US2 and US3 can extend this agreed shared model. */
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

    public ChallengeResult getChallengeResult() { return challengeResult; }
    public ExamResult getExamResult() { return examResult; }
    public int getPreparationPoints() {
        return challengeResult == null ? 0 : challengeResult.getPreparationPoints();
    }

    public void completeChallenge(ChallengeResult result) {
        if (challengeResult != null) {
            throw new IllegalStateException("This challenge is already completed.");
        }
        challengeResult = java.util.Objects.requireNonNull(result);
    }

    public void completeExam(ExamResult result) {
        if (examResult != null) {
            throw new IllegalStateException("This exam is already completed.");
        }
        examResult = java.util.Objects.requireNonNull(result);
    }
}
