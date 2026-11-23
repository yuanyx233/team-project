package use_case.choose_major;

import entity.AcademicRecord;
import entity.Course;
import entity.Major;
import java.util.List;
import java.util.stream.Collectors;

/** No Swing, JSON, filesystem, or concrete catalogue dependency. */
public final class ChooseMajorInteractor implements ChooseMajorInputBoundary {
    private final MajorCatalogDataAccessInterface catalog;
    private final AcademicRecord record;
    private final ChooseMajorOutputBoundary presenter;

    public ChooseMajorInteractor(MajorCatalogDataAccessInterface catalog,
                                 AcademicRecord record, ChooseMajorOutputBoundary presenter) {
        this.catalog = java.util.Objects.requireNonNull(catalog);
        this.record = java.util.Objects.requireNonNull(record);
        this.presenter = java.util.Objects.requireNonNull(presenter);
    }

    @Override
    public void loadMajors() {
        show(record.getSelectedMajor(), record.hasConfirmedMajor()
                ? "Your major is confirmed. It cannot be changed in this playthrough."
                : "Select a major to preview its attached courses.");
    }

    @Override
    public void preview(ChooseMajorInputData input) {
        if (record.hasConfirmedMajor()) {
            loadMajors();
            return;
        }
        Major major = find(input);
        if (major != null) { show(major, "Preview only. Confirm to assign these courses."); }
    }

    @Override
    public void confirm(ChooseMajorInputData input) {
        if (record.hasConfirmedMajor()) {
            presenter.presentError("Your major has already been confirmed.");
            return;
        }
        Major major = find(input);
        if (major == null) { return; }
        record.confirmMajor(major);
        show(major, "Major confirmed. Its courses have been assigned.");
    }

    @Override
    public void cancel() {
        // Clear the preview, not the already confirmed academic record.
        show(record.getSelectedMajor(), record.hasConfirmedMajor()
                ? "Your confirmed academic plan is unchanged."
                : "Selection cancelled. No major or courses have been assigned.");
    }

    private Major find(ChooseMajorInputData input) {
        if (input == null || input.getMajorId() == null || input.getMajorId().isBlank()) {
            presenter.presentError("Please select a major.");
            return null;
        }
        Major major = catalog.findById(input.getMajorId()).orElse(null);
        if (major == null) { presenter.presentError("That major is unavailable. Choose another."); }
        return major;
    }

    private void show(Major selected, String message) {
        List<MajorData> majors = catalog.getMajors().stream()
                .map(this::toData).collect(Collectors.toList());
        presenter.present(new ChooseMajorOutputData(majors,
                selected == null ? null : toData(selected), record.hasConfirmedMajor(), message));
    }

    private MajorData toData(Major major) {
        List<String> courses = major.getCourses().stream()
                .map(course -> course.getId() + " - " + course.getName()).collect(Collectors.toList());
        return new MajorData(major.getId(), major.getName(), major.getDescription(), courses);
    }
}
