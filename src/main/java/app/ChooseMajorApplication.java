package app;

import data_access.InMemoryMajorCatalog;
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
            ChooseMajorInteractor interactor = new ChooseMajorInteractor(
                    InMemoryMajorCatalog.demo(), record, presenter);
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
            });
            JFrame frame = new JFrame("Stressdew Valley - Choose a Major Demo");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setContentPane(screens); frame.setSize(820, 540);
            frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }
}
