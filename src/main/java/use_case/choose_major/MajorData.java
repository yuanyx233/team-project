package use_case.choose_major;

import java.util.List;

/** Output DTO: the interface adapters never receive mutable domain state. */
public final class MajorData {
    private final String id;
    private final String name;
    private final String description;
    private final List<String> courseNames;
    private final boolean playable;
    private final String sourceUrl;
    private final String sourceLabel;

    public MajorData(String id, String name, String description, List<String> courseNames) {
        this(id, name, description, courseNames, !courseNames.isEmpty(), "", "Local catalogue");
    }

    public MajorData(String id, String name, String description, List<String> courseNames,
                     boolean playable, String sourceUrl, String sourceLabel) {
        this.playable = playable;
        this.sourceUrl = sourceUrl;
        this.sourceLabel = sourceLabel;
        this.id = id;
        this.name = name;
        this.description = description;
        this.courseNames = List.copyOf(courseNames);
    }
    public boolean isPlayable() { return playable; }
    public String getSourceUrl() { return sourceUrl; }
    public String getSourceLabel() { return sourceLabel; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getCourseNames() { return courseNames; }
}
