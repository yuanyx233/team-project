package app;

import data_access.UoftMajorCatalog;
import data_access.HttpProgramPageClient;
import java.time.Clock;
import entity.AcademicRecord;
import interface_adapter.choose_major.*;
import use_case.choose_major.ChooseMajorInteractor;
import view.ChooseMajorView;
import javax.swing.*;
import java.awt.*;

/** Composition root and demo navigation; replace the placeholder street on integration. */
public final class ChooseMajorApplication {
    private ChooseMajorApplication() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AcademicRecord record = new AcademicRecord();
            ChooseMajorViewModel model = new ChooseMajorViewModel();
            ChooseMajorPresenter presenter = new ChooseMajorPresenter(model);
            UoftMajorCatalog catalog = new UoftMajorCatalog(new HttpProgramPageClient(), Clock.systemUTC());
            ChooseMajorInteractor interactor = new ChooseMajorInteractor(
                    catalog, () -> record, presenter);
            ChooseMajorController controller = new ChooseMajorController(interactor);
            CardLayout layout = new CardLayout();
            JPanel screens = new JPanel(layout);
            JPanel street = new JPanel(new BorderLayout(10, 10));
            street.add(new JLabel("US1 demo street - other buildings are not implemented",
                    SwingConstants.CENTER), BorderLayout.CENTER);
            JButton enter = new JButton("Enter Sidney Smith");
            street.add(enter, BorderLayout.SOUTH);
            ChooseMajorView view = new ChooseMajorView(controller, model,
                    () -> layout.show(screens, "street"));
            screens.add(street, "street"); screens.add(view, "choose-major");
            enter.addActionListener(event -> {
                controller.loadMajors(); layout.show(screens, "choose-major");
                enter.setEnabled(false);
                view.setLoading(true);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() { catalog.refresh(); return null; }
                    @Override
                    protected void done() {
                        try { get(); }
                        catch (Exception exception) {
                            // Keep the prior snapshot on an unexpected worker failure.
                        }
                        controller.loadMajors();
                        view.setLoading(false);
                        enter.setEnabled(true);
                    }
                }.execute();
            });
            JFrame frame = new JFrame("Stressdew Valley - Choose a Major Demo");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setContentPane(screens); frame.setSize(820, 540);
            frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }
}
