package entity;

import java.util.HashSet;
import java.util.List;

/** A fixed major with automatically assigned courses. */
public final class Major {
    private final String id;
    private final String name;
    private final String description;
    private final List<Course> courses;

    public Major(String id, String name, String description, List<Course> courses) {
        if (id == null || id.isBlank() || name == null || name.isBlank()
                || description == null || description.isBlank()
                || courses == null || courses.isEmpty()) {
            throw new IllegalArgumentException("Major requires identifying data and courses.");
        }
        this.courses = List.copyOf(courses);
        if (new HashSet<>(this.courses).size() != this.courses.size()) {
            throw new IllegalArgumentException("Course IDs must be unique within a major.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<Course> getCourses() { return courses; }
}
