package entity;

import java.util.Objects;

/** Immutable catalogue data; never stores a player's results. */
public final class Course {
    private final String id;
    private final String name;
    private final String questionCategory;

    public Course(String id, String name, String questionCategory) {
        this.id = requireText(id);
        this.name = requireText(name);
        this.questionCategory = requireText(questionCategory);
    }

    private static String requireText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Course fields cannot be blank.");
        }
        return value;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getQuestionCategory() { return questionCategory; }

    @Override
    public boolean equals(Object other) {
        return other instanceof Course && id.equals(((Course) other).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
