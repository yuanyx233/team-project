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

    /** Fictional sample content, not approved courses or confirmed API mappings. */
    public static InMemoryMajorCatalog demo() {
        return new InMemoryMajorCatalog(List.of(
                new Major("computing", "Computing", "Explore programming and software design.", List.of(
                        new Course("COMP1", "Programming Foundations", "computing"),
                        new Course("COMP2", "Software Design", "computing"))),
                new Major("history", "History", "Explore historical events and societies.", List.of(
                        new Course("HIST1", "World History", "history"),
                        new Course("HIST2", "Historical Perspectives", "history"))),
                new Major("biology", "Biology", "Explore life and living systems.", List.of(
                        new Course("BIO1", "Cell Biology", "biology"),
                        new Course("BIO2", "Ecology", "biology")))));
    }
}
