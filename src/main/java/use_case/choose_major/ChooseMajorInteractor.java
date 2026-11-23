package use_case.choose_major;

import entity.AcademicRecord;
import entity.Major;
import java.util.List;
import java.util.stream.Collectors;

/** No Swing, JSON, filesystem, or concrete catalogue dependency. */
public final class ChooseMajorInteractor implements ChooseMajorInputBoundary {
    private final MajorCatalogDataAccessInterface catalog;
    private final AcademicRecordAccess records;
    private final ChooseMajorOutputBoundary presenter;

    public ChooseMajorInteractor(MajorCatalogDataAccessInterface catalog,
                                 AcademicRecordAccess records, ChooseMajorOutputBoundary presenter) {
        this.catalog = java.util.Objects.requireNonNull(catalog);
        this.records = java.util.Objects.requireNonNull(records);
        this.presenter = java.util.Objects.requireNonNull(presenter);
    }

    @Override
    public void loadMajors() {
        AcademicRecord record = records.getAcademicRecord();
        show(record.getSelectedMajor(), record.hasConfirmedMajor()
                ? "Your major is confirmed. It cannot be changed in this playthrough."
                : "Select a major to preview its attached courses.");
    }

    @Override
    public void preview(ChooseMajorInputData input) {
        AcademicRecord record = records.getAcademicRecord();
        if (record.hasConfirmedMajor()) {
            loadMajors();
            return;
        }
        Major major = find(input);
        if (major != null) {
            show(major, major.isPlayable() ? "Preview only. Confirm to assign these courses."
                    : "This major is not available in the game yet. Choose CS, Biology, or History.");
        }
    }

    @Override
    public void confirm(ChooseMajorInputData input) {
        AcademicRecord record = records.getAcademicRecord();
        if (record.hasConfirmedMajor()) {
            presenter.presentError("Your major has already been confirmed.");
            return;
        }
        Major major = find(input);
        if (major == null) { return; }
        if (!major.isPlayable()) {
            presenter.presentError("This major is not available in the game yet. Choose CS, Biology, or History.");
            return;
        }
        record.confirmMajor(major);
        show(major, "Major confirmed. Its courses have been assigned.");
    }

    @Override
    public void cancel() {
        AcademicRecord record = records.getAcademicRecord();
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
                selected == null ? null : toData(selected), records.getAcademicRecord().hasConfirmedMajor(), message));
    }

    private MajorData toData(Major major) {
        List<String> courses = major.getCourses().stream()
                .map(course -> course.getId() + " - " + course.getName()).collect(Collectors.toList());
        String sourceLabel;
        switch (major.getSource()) {
            case WEB: sourceLabel = "Fetched from U of T Academic Calendar"; break;
            case LOCAL_FALLBACK: sourceLabel = "Local fallback - live page not loaded"; break;
            default: sourceLabel = "Local catalogue";
        }
        return new MajorData(major.getId(), major.getName(), major.getDescription(), courses,
                major.isPlayable(), major.getSourceUrl(), sourceLabel);
    }
}
