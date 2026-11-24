package view;

import interface_adapter.ViewManagerModel;
import java.awt.CardLayout;
import java.util.Objects;
import javax.swing.JPanel;

public class GameView extends JPanel {
    private final CardLayout layout = new CardLayout();

    public GameView(TitleView title, ResidenceView residence, ViewManagerModel navigation) {
        this(title, residence, navigation, new BuildingView("Lecture Hall",
                "Complete class challenges at Bahen Centre.", () -> navigation.setActiveView("street")));
    }

    /** Allows the composition root to supply UC2's page with the shared state already injected. */
    public GameView(TitleView title, ResidenceView residence, ViewManagerModel navigation,
                    JPanel lecture) {
        this(title, residence, navigation, new BuildingView("Registrar's Office",
                "Choose your major at Sidney Smith.", () -> navigation.setActiveView("street")), lecture,
                new BuildingView("Exam Centre", "Take exams for your courses.",
                        () -> navigation.setActiveView("street")));
    }

    public GameView(TitleView title, ResidenceView residence, ViewManagerModel navigation,
                    JPanel registrar, JPanel lecture, JPanel exam) {
        setLayout(layout);
        addPage("title", title);
        addPage("street", new StreetView(navigation::setActiveView));
        addPage("registrar", Objects.requireNonNull(registrar));
        addPage("lecture", Objects.requireNonNull(lecture, "Lecture Hall page is required"));
        addPage("exam", Objects.requireNonNull(exam));
        addPage("residence", residence);
        navigation.addPropertyChangeListener(event -> layout.show(this, navigation.getActiveView()));
    }

    private void addPage(String name, JPanel page) {
        page.setName(name);
        add(page, name);
    }
}
