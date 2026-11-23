package use_case.choose_major;

import data_access.InMemoryMajorCatalog;
import entity.AcademicRecord;
import entity.CourseProgress;
import interface_adapter.choose_major.*;
import use_case.choose_major.ChooseMajorInteractor;

import java.util.concurrent.atomic.AtomicInteger;

/** Dependency-free executable tests. Run with the commands in README.md. */
public final class ChooseMajorFeatureTest {
    private static int tests;
    private ChooseMajorFeatureTest() { }

    public static void main(String[] args) {
        previewAndCancelDoNotAssign();
        confirmationAssignsOnlyAttachedCourses();
        invalidInputDoesNotMutateState();
        repeatedConfirmationPreservesProgress();
        returningToScreenPreservesConfirmation();
        catalogueAndRecordCollectionsAreProtected();
        System.out.println("Passed " + tests + " major-selection checks.");
    }

    private static final class Fixture {
        private final AcademicRecord record = new AcademicRecord();
        private final ChooseMajorViewModel model = new ChooseMajorViewModel();
        private final ChooseMajorInteractor useCase = new ChooseMajorInteractor(
                InMemoryMajorCatalog.demo(), record, new ChooseMajorPresenter(model));
        private final ChooseMajorController controller = new ChooseMajorController(useCase);
    }

    private static void previewAndCancelDoNotAssign() {
        Fixture f = new Fixture();
        AtomicInteger changes = new AtomicInteger();
        f.model.addPropertyChangeListener(event -> changes.incrementAndGet());
        f.controller.loadMajors();
        check(f.model.getState().getMajors().size() == 3, "Catalogue is displayed");
        f.controller.preview("computing");
        check(f.model.getState().getSelected().getCourseNames().size() == 2,
                "Preview displays attached courses");
        check(!f.record.hasConfirmedMajor() && f.record.getCourseProgress().isEmpty(),
                "Preview does not assign a major or courses");
        f.controller.cancel();
        check(f.model.getState().getSelected() == null && !f.record.hasConfirmedMajor(),
                "Cancel clears preview without assignment");
        check(changes.get() == 3, "Presenter publishes each successful action");
    }

    private static void confirmationAssignsOnlyAttachedCourses() {
        Fixture f = new Fixture();
        f.controller.confirm("history");
        check(f.record.getSelectedMajor().getId().equals("history"), "Confirmed correct major");
        check(f.record.getCourseProgress().keySet().equals(java.util.Set.of("HIST1", "HIST2")),
                "Assigned exactly the selected major's courses");
        check(f.model.getState().isConfirmed(), "Presenter shows confirmation");
    }

    private static void invalidInputDoesNotMutateState() {
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

    private static void repeatedConfirmationPreservesProgress() {
        Fixture f = new Fixture();
        f.controller.confirm("biology");
        CourseProgress initial = f.record.getCourseProgress().get("BIO1");
        f.controller.confirm("biology");
        f.controller.confirm("history");
        check(f.record.getSelectedMajor().getId().equals("biology"), "Major cannot change");
        check(f.record.getCourseProgress().size() == 2
                        && f.record.getCourseProgress().get("BIO1") == initial,
                "Repeated confirmation cannot recreate or duplicate progress");
        check(f.model.getState().isError() && f.model.getState().isConfirmed(),
                "Error preserves confirmed screen state");
    }

    private static void returningToScreenPreservesConfirmation() {
        Fixture f = new Fixture();
        f.controller.confirm("computing");
        f.controller.cancel();
        f.controller.loadMajors();
        f.controller.preview("biology");
        check(f.model.getState().isConfirmed()
                        && f.model.getState().getSelected().getId().equals("computing"),
                "Returning or previewing cannot change confirmed selection");
    }

    private static void catalogueAndRecordCollectionsAreProtected() {
        Fixture f = new Fixture();
        f.controller.confirm("computing");
        boolean blocked = false;
        try { f.record.getCourseProgress().clear(); }
        catch (UnsupportedOperationException expected) { blocked = true; }
        check(blocked, "Callers cannot clear assigned progress through getter");
        blocked = false;
        try { f.record.getSelectedMajor().getCourses().clear(); }
        catch (UnsupportedOperationException expected) { blocked = true; }
        check(blocked, "Callers cannot alter attached courses through getter");
    }

    private static void check(boolean condition, String description) {
        if (!condition) { throw new AssertionError(description); }
        tests++;
    }
}
