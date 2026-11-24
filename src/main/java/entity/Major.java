package entity;

import java.util.HashSet;
import java.util.List;

/** A fixed major with automatically assigned courses. */
public final class Major {
    private final String id;
    private final String name;
    private final String description;
    private final List<Course> courses;
    private final String sourceUrl;
    private final Source source;

    public enum Source { LOCAL, WEB, LOCAL_FALLBACK }

    public Major(String id, String name, String description, List<Course> courses) {
        this(id, name, description, courses, "", Source.LOCAL);
    }

    public Major(String id, String name, String description, List<Course> courses,
                 String sourceUrl, Source source) {
        if (id == null || id.isBlank() || name == null || name.isBlank()
                || description == null || description.isBlank()
                || courses == null) {
            throw new IllegalArgumentException("Major requires identifying data and a course list.");
        }
        this.courses = List.copyOf(courses);
        if (new HashSet<>(this.courses).size() != this.courses.size()) {
            throw new IllegalArgumentException("Course IDs must be unique within a major.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.sourceUrl = java.util.Objects.requireNonNull(sourceUrl);
        this.source = java.util.Objects.requireNonNull(source);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    /** Only entries with implemented game courses can be confirmed. */
    public boolean isPlayable() { return !courses.isEmpty(); }
    public String getSourceUrl() { return sourceUrl; }
    public Source getSource() { return source; }
    public List<Course> getCourses() { return courses; }
}
