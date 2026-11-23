package entity;

/** Initial progress record. US2 and US3 can extend this agreed shared model. */
public final class CourseProgress {
    private final String courseId;

    public CourseProgress(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course ID cannot be blank.");
        }
        this.courseId = courseId;
    }

    public String getCourseId() { return courseId; }
}
