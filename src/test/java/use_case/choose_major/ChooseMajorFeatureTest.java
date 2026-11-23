package use_case.choose_major;

import data_access.InMemoryMajorCatalog;
import entity.AcademicRecord;
import entity.CourseProgress;
import interface_adapter.choose_major.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** JUnit 5 checks for the UC1 contract and catalogue. */
public final class ChooseMajorFeatureTest {
    private static final class Fixture {
        private AcademicRecord record = new AcademicRecord();
        private final ChooseMajorViewModel model = new ChooseMajorViewModel();
        private final ChooseMajorInteractor useCase = new ChooseMajorInteractor(
                InMemoryMajorCatalog.predefined(), () -> record, new ChooseMajorPresenter(model));
        private final ChooseMajorController controller = new ChooseMajorController(useCase);
    }

    @Test
    void previewAndCancelDoNotAssign() {
        Fixture f = new Fixture();
        AtomicInteger changes = new AtomicInteger();
        f.model.addPropertyChangeListener(event -> changes.incrementAndGet());
        f.controller.loadMajors();
        check(f.model.getState().getMajors().size() == 3, "Catalogue is displayed");
        f.controller.preview("cs");
        check(f.model.getState().getSelected().getCourseNames().size() == 2,
                "Preview displays attached courses");
        check(!f.record.hasConfirmedMajor() && f.record.getCourseProgress().isEmpty(),
                "Preview does not assign a major or courses");
        f.controller.cancel();
        check(f.model.getState().getSelected() == null && !f.record.hasConfirmedMajor(),
                "Cancel clears preview without assignment");
        check(changes.get() == 3, "Presenter publishes each successful action");
    }

    @Test
    void confirmationAssignsOnlyAttachedCourses() {
        Fixture f = new Fixture();
        f.controller.confirm("biology");
        check(f.record.getSelectedMajor().getId().equals("biology"), "Confirmed correct major");
        check(f.record.getCourseProgress().keySet().equals(java.util.Set.of("BIO101", "BIO201")),
                "Assigned exactly the selected major's courses");
        check(f.model.getState().isConfirmed(), "Presenter shows confirmation");
    }

    @Test
    void historyAssignsItsOwnCourses() {
        Fixture f = new Fixture();
        f.controller.confirm("history");
        check(f.record.getSelectedMajor().getId().equals("history"), "History is selectable");
        check(f.record.getCourseProgress().keySet().equals(java.util.Set.of("HIST101", "HIST201")),
                "History receives only its attached courses");
    }

    @Test
    void invalidInputDoesNotMutateState() {
        Fixture f = new Fixture();
        for (String id : new String[]{null, "", " ", "missing-major"}) {
            f.controller.confirm(id);
            check(!f.record.hasConfirmedMajor() && f.record.getCourseProgress().isEmpty(),
                    "Invalid ID leaves academic record unchanged");
            check(f.model.getState().isError(), "Invalid ID produces visible error");
        }
        f.useCase.confirm(null);
        check(!f.record.hasConfirmedMajor(), "Null request is rejected");
    }

    @Test
    void repeatedConfirmationPreservesProgress() {
        Fixture f = new Fixture();
        f.controller.confirm("cs");
        CourseProgress initial = f.record.getCourseProgress().get("CS101");
        f.controller.confirm("cs");
        f.controller.confirm("biology");
        check(f.record.getSelectedMajor().getId().equals("cs"), "Major cannot change");
        check(f.record.getCourseProgress().size() == 2
                        && f.record.getCourseProgress().get("CS101") == initial,
                "Repeated confirmation cannot recreate or duplicate progress");
        check(f.model.getState().isError() && f.model.getState().isConfirmed(),
                "Error preserves confirmed screen state");
    }

    @Test
    void returningToScreenPreservesConfirmation() {
        Fixture f = new Fixture();
        f.controller.confirm("cs");
        f.controller.cancel();
        f.controller.loadMajors();
        f.controller.preview("cs");
        check(f.model.getState().isConfirmed()
                        && f.model.getState().getSelected().getId().equals("cs"),
                "Returning or previewing cannot change confirmed selection");
    }

    @Test
    void catalogueAndRecordCollectionsAreProtected() {
        Fixture f = new Fixture();
        f.controller.confirm("cs");
        boolean blocked = false;
        try { f.record.getCourseProgress().clear(); }
        catch (UnsupportedOperationException expected) { blocked = true; }
        check(blocked, "Callers cannot clear assigned progress through getter");
        blocked = false;
        try { f.record.getSelectedMajor().getCourses().clear(); }
        catch (UnsupportedOperationException expected) { blocked = true; }
        check(blocked, "Callers cannot alter attached courses through getter");
    }

    @Test
    void catalogueMatchesAgreedSubjects() {
        InMemoryMajorCatalog catalog = InMemoryMajorCatalog.predefined();
        check(catalog.getMajors().size() == 3, "Exactly three majors");
        java.util.Map<String, String> expected = java.util.Map.of(
                "CS101", "high_school_computer_science",
                "CS201", "college_computer_science",
                "BIO101", "high_school_biology",
                "BIO201", "college_biology",
                "HIST101", "high_school_world_history",
                "HIST201", "high_school_european_history");
        java.util.Map<String, String> actual = new java.util.HashMap<>();
        for (entity.Major major : catalog.getMajors()) {
            check(major.getCourses().size() == 2, "Two fixed courses per major");
            for (entity.Course course : major.getCourses()) {
                check(actual.put(course.getId(), course.getQuestionCategory()) == null,
                        "Course IDs are globally unique");
            }
        }
        check(expected.equals(actual), "Exact MMLU subject mapping");
    }

    @Test
    void replacingStateDoesNotLeaveUseCaseOnTheOldRecord() {
        Fixture f = new Fixture();
        f.controller.confirm("cs");
        AcademicRecord old = f.record;
        AcademicRecord loaded = new AcademicRecord();
        loaded.confirmMajor(InMemoryMajorCatalog.predefined().findById("biology").orElseThrow());
        CourseProgress savedProgress = loaded.getCourseProgress().get("BIO101");
        f.record = loaded;
        f.controller.loadMajors();
        check(f.model.getState().getSelected().getId().equals("biology"),
                "Screen reads newly restored record");
        f.controller.confirm("cs");
        check(f.record.getCourseProgress().get("BIO101") == savedProgress,
                "Confirmed loaded progress is not overwritten");
        check(old.getSelectedMajor().getId().equals("cs"), "Old record unchanged");
        f.record = new AcademicRecord();
        f.controller.loadMajors();
        check(!f.model.getState().isConfirmed() && f.model.getState().getSelected() == null,
                "New game clears old confirmed screen");
        f.controller.confirm("biology");
        check(f.record.getSelectedMajor().getId().equals("biology"),
                "Confirmation updates current game");
    }

    @Test
    void displayOnlyMajorsCanBePreviewedButNeverAssigned() {
        entity.AcademicRecord record = new entity.AcademicRecord();
        ChooseMajorViewModel model = new ChooseMajorViewModel();
        data_access.UoftMajorCatalog catalog = new data_access.UoftMajorCatalog(
                uri -> { throw new java.io.IOException("Offline"); }, java.time.Clock.systemUTC());
        ChooseMajorController controller = new ChooseMajorController(new ChooseMajorInteractor(
                catalog, () -> record, new ChooseMajorPresenter(model)));
        controller.loadMajors();
        for (String id : java.util.List.of("mathematics", "economics", "psychology", "sociology", "philosophy")) {
            controller.preview(id);
            check(!model.getState().getSelected().isPlayable(), "Display-only preview");
            check(model.getState().getMessage().contains("not available"), "Availability explained on selection");
            controller.confirm(id);
            check(model.getState().isError() && !record.hasConfirmedMajor()
                    && record.getCourseProgress().isEmpty(), "Confirmation cannot bypass restriction");
            entity.Major major = catalog.findById(id).orElseThrow();
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                    () -> record.confirmMajor(major));
        }
        controller.confirm("history");
        check(record.getCourseProgress().size() == 2, "Playable confirmation still works offline");
    }

    private static void check(boolean condition, String description) {
        if (!condition) { throw new AssertionError(description); }
    }
}
