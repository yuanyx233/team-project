package view;

import interface_adapter.ViewManagerModel;
import java.awt.CardLayout;
import java.util.Objects;
import javax.swing.JPanel;

public class GameView extends JPanel {
    private final CardLayout layout = new CardLayout();

    public GameView(TitleView title, ResidenceView residence, ViewManagerModel navigation) {
        this(title, residence, navigation, defaultLecturePage(navigation));
    }

    /** Allows the composition root to supply UC2's page with the shared state already injected. */
    public GameView(TitleView title, ResidenceView residence, ViewManagerModel navigation,
                    JPanel lecture) {
        this(title, residence, navigation, lecture, new StreetView(navigation::setActiveView));
    }

    /**
     * Allows the composition root to supply the street page as well, such as the walkable campus map.
     * The street page must send the player to the building page names used here.
     */
    public GameView(TitleView title, ResidenceView residence, ViewManagerModel navigation,
                    JPanel lecture, JPanel street) {
        setLayout(layout);
        addPage("title", title);
        addPage("street", Objects.requireNonNull(street, "Street page is required"));
        addPage("registrar", new BuildingView("Registrar's Office",
                "Choose your major at Sidney Smith.", () -> navigation.setActiveView("street")));
        addPage("lecture", Objects.requireNonNull(lecture, "Lecture Hall page is required"));
        addPage("exam", new BuildingView("Exam Centre",
                "Take exams for your courses.", () -> navigation.setActiveView("street")));
        addPage("residence", residence);
        navigation.addPropertyChangeListener(event -> layout.show(this, navigation.getActiveView()));
    }

    /** The placeholder Lecture Hall page used until UC2's page is injected. */
    public static JPanel defaultLecturePage(ViewManagerModel navigation) {
        return new BuildingView("Lecture Hall",
                "Complete class challenges at Bahen Centre.", () -> navigation.setActiveView("street"));
    }

    private void addPage(String name, JPanel page) {
        page.setName(name);
        add(page, name);
    }
}
