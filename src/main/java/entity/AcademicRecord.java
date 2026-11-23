package entity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Holds one player's selection. Validation finishes before state changes. */
public final class AcademicRecord {
    private Major selectedMajor;
    private Map<String, CourseProgress> courseProgress = new LinkedHashMap<>();

    public boolean hasConfirmedMajor() { return selectedMajor != null; }
    public Major getSelectedMajor() { return selectedMajor; }

    public void confirmMajor(Major major) {
        if (hasConfirmedMajor()) {
            throw new IllegalStateException("Your major has already been confirmed.");
        }
        Objects.requireNonNull(major, "Major cannot be null.");
        Map<String, CourseProgress> initial = new LinkedHashMap<>();
        for (Course course : major.getCourses()) {
            initial.put(course.getId(), new CourseProgress(course.getId()));
        }
        courseProgress = initial;
        selectedMajor = major;
    }

    public Map<String, CourseProgress> getCourseProgress() {
        return Collections.unmodifiableMap(courseProgress);
    }
}
