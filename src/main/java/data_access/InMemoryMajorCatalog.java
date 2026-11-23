package data_access;

import entity.Course;
import entity.Major;
import use_case.choose_major.MajorCatalogDataAccessInterface;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class InMemoryMajorCatalog implements MajorCatalogDataAccessInterface {
    private final List<Major> majors;

    public InMemoryMajorCatalog(List<Major> majors) {
        this.majors = List.copyOf(majors);
        Set<String> ids = new HashSet<>();
        for (Major major : this.majors) {
            if (!ids.add(major.getId())) { throw new IllegalArgumentException("Duplicate major ID."); }
        }
    }

    @Override
    public List<Major> getMajors() { return majors; }
    @Override
    public Optional<Major> findById(String id) {
        return majors.stream().filter(major -> major.getId().equals(id)).findFirst();
    }

    /** The agreed three-major fictional catalogue; category values are MMLU subject IDs. */
    public static InMemoryMajorCatalog predefined() {
        return new InMemoryMajorCatalog(List.of(
                new Major("cs", "Computer Science", "Explore computing foundations and advanced concepts.", List.of(
                        new Course("CS101", "Computing Foundations", "high_school_computer_science"),
                        new Course("CS201", "College Computer Science", "college_computer_science"))),
                new Major("biology", "Biology", "Explore living systems and biological concepts.", List.of(
                        new Course("BIO101", "Biology Foundations", "high_school_biology"),
                        new Course("BIO201", "College Biology", "college_biology"))),
                new Major("history", "History", "Explore world and European history.", List.of(
                        new Course("HIST101", "World History", "high_school_world_history"),
                        new Course("HIST201", "European History", "high_school_european_history")))));
    }
}
