package app;

import data_access.BundledWeatherDataAccessObject;
import data_access.InMemoryGameStateDataAccessObject;
import data_access.OpenMeteoWeatherDataAccessObject;
import entity.GameDay;
import entity.Player;
import interface_adapter.rest.RestController;
import interface_adapter.rest.RestPresenter;
import interface_adapter.rest.RestViewModel;
import use_case.rest.RestInteractor;
import view.ResidenceView;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.time.LocalDate;

/**
 * Temporary entry point that opens the residence screen. To be replaced by the shared campus navigation.
 */
public class Main {
    /** Real date matched to day 1 (team rule still to be finalized). */
    private static final LocalDate START_DATE = LocalDate.of(2025, 9, 2);
    private static final int MAX_ENERGY = 100;

    public static void main(String[] args) {
        InMemoryGameStateDataAccessObject gameState =
                new InMemoryGameStateDataAccessObject(new GameDay(1, START_DATE), new Player(MAX_ENERGY, MAX_ENERGY));
        OpenMeteoWeatherDataAccessObject weather = new OpenMeteoWeatherDataAccessObject(
                OpenMeteoWeatherDataAccessObject.DEFAULT_BASE_URL, new BundledWeatherDataAccessObject());

        RestViewModel viewModel = new RestViewModel();
        RestController controller =
                new RestController(new RestInteractor(gameState, weather, new RestPresenter(viewModel)));

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Stressdew Valley");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new ResidenceView(viewModel, controller));
            viewModel.setState("Day 1 (" + START_DATE + ")", "Weather: -", "Energy: " + MAX_ENERGY + " / " + MAX_ENERGY);
            frame.pack();
            frame.setSize(420, 220);
            frame.setVisible(true);
        });
    }
}
