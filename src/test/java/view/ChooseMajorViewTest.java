package view;

import data_access.InMemoryMajorCatalog;
import entity.AcademicRecord;
import interface_adapter.choose_major.*;
import use_case.choose_major.ChooseMajorInteractor;
import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/** Headless event-dispatch-thread integration check for the actual Swing controls. */
public final class ChooseMajorViewTest {
    @Test
    void previewConfirmCancelAndReenter() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AcademicRecord record = new AcademicRecord();
            ChooseMajorViewModel model = new ChooseMajorViewModel();
            ChooseMajorController controller = new ChooseMajorController(new ChooseMajorInteractor(
                    InMemoryMajorCatalog.predefined(), () -> record, new ChooseMajorPresenter(model)));
            AtomicBoolean returned = new AtomicBoolean();
            ChooseMajorView view = new ChooseMajorView(controller, model, () -> returned.set(true));
            controller.loadMajors();
            JList<?> list = find(view, JList.class, null);
            JButton confirm = find(view, JButton.class, "Confirm Major");
            JButton cancel = find(view, JButton.class, "Cancel Preview");
            check(!confirm.isEnabled(), "Confirmation disabled before a preview");
            list.setSelectedIndex(0);
            check(confirm.isEnabled() && !record.hasConfirmedMajor(), "Selection only previews");
            cancel.doClick();
            check(list.getSelectedIndex() == -1 && !confirm.isEnabled(), "Cancel clears screen selection");
            list.setSelectedIndex(1);
            confirm.doClick();
            check(record.getSelectedMajor().getId().equals("biology"), "Button confirms selected major");
            check(!list.isEnabled() && !confirm.isEnabled(), "Confirmed controls locked");
            find(view, JButton.class, "Back to Street").doClick();
            check(returned.get() && record.hasConfirmedMajor(), "Back navigates without erasing record");
            controller.loadMajors();
            check(list.getSelectedIndex() == 1 && !confirm.isEnabled(), "Re-entry retains confirmed state");
        });
    }
    @Test
    void browsingOnlyEntryDisablesConfirmationAndShowsFallbackSource() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ChooseMajorViewModel model = new ChooseMajorViewModel();
            AcademicRecord record = new AcademicRecord();
            data_access.UoftMajorCatalog catalog = new data_access.UoftMajorCatalog(
                    uri -> { throw new java.io.IOException("Offline"); }, java.time.Clock.systemUTC());
            ChooseMajorController controller = new ChooseMajorController(new ChooseMajorInteractor(
                    catalog, () -> record, new ChooseMajorPresenter(model)));
            ChooseMajorView view = new ChooseMajorView(controller, model, () -> { });
            controller.loadMajors();
            JList<?> list = find(view, JList.class, null);
            list.setSelectedIndex(3);
            check(!find(view, JButton.class, "Confirm Major").isEnabled(), "Display-only cannot confirm");
            check(model.getState().getMessage().contains("not available"), "Availability message shown");
            check(model.getState().getSelected().getSourceLabel().contains("fallback"), "Fallback disclosed");
            list.setSelectedIndex(0);
            view.setLoading(true);
            check(!list.isEnabled() && !find(view, JButton.class, "Confirm Major").isEnabled(), "Loading locks input");
            view.setLoading(false);
            check(list.isEnabled() && find(view, JButton.class, "Confirm Major").isEnabled(), "Input restored");
        });
    }

    private static void check(boolean valid, String text) {
        if (!valid) { throw new AssertionError(text); }
    }
    private static <T extends Component> T find(Container parent, Class<T> type, String text) {
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)
                    && (text == null || child instanceof JButton && text.equals(((JButton) child).getText()))) {
                return type.cast(child);
            }
            if (child instanceof Container) {
                T result = find((Container) child, type, text);
                if (result != null) { return result; }
            }
        }
        return null;
    }
}
