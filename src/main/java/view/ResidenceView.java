package view;

import interface_adapter.rest.RestController;
import interface_adapter.rest.RestViewModel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Component;

/**
 * Innis Residence screen: lets the player rest and shows the new day.
 */
public class ResidenceView extends JPanel {
    private final JLabel dayLabel = new JLabel();
    private final JLabel weatherLabel = new JLabel();
    private final JLabel energyLabel = new JLabel();
    private final JLabel saveLabel = new JLabel();
    private final JButton retryButton = new JButton("Retry save");
    private final JButton restButton = new JButton("Rest");
    private final JButton backButton = new JButton("Back to Street");

    public ResidenceView(RestViewModel viewModel, RestController controller, Runnable back) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Innis Residence");
        restButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Resting ends the day, restores energy based on the next day's weather, "
                            + "and saves completed progress. Rest now?",
                    "Rest", JOptionPane.OK_CANCEL_OPTION);
            if (choice == JOptionPane.OK_OPTION) {
                run(controller::execute);
            }
        });
        retryButton.addActionListener(e -> run(controller::retrySave));
        retryButton.setVisible(false);

        backButton.addActionListener(e -> back.run());

        for (JComponent c : new JComponent[]{title, dayLabel, weatherLabel, energyLabel, restButton,
                saveLabel, retryButton, backButton}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(c);
        }

        Runnable render = () -> {
            dayLabel.setText(viewModel.getDayText());
            weatherLabel.setText(viewModel.getWeatherText());
            energyLabel.setText(viewModel.getEnergyText());
            saveLabel.setText(viewModel.getSaveText());
            retryButton.setVisible(viewModel.isRetryAvailable());
        };
        viewModel.addPropertyChangeListener(evt -> {
            if (SwingUtilities.isEventDispatchThread()) { render.run(); }
            else { SwingUtilities.invokeLater(render); }
        });
    }

    private void run(Runnable action) {
        restButton.setEnabled(false);
        backButton.setEnabled(false);
        retryButton.setEnabled(false);
        saveLabel.setText("Working...");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() { action.run(); return null; }
            @Override protected void done() {
                try { get(); }
                catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    saveLabel.setText("Interrupted. Please try again.");
                }
                catch (java.util.concurrent.ExecutionException exception) {
                    saveLabel.setText("Could not rest or save. Please try again.");
                    java.util.logging.Logger.getLogger(ResidenceView.class.getName())
                            .log(java.util.logging.Level.WARNING, "Residence action failed", exception.getCause());
                }
                restButton.setEnabled(true);
                backButton.setEnabled(true);
                retryButton.setEnabled(true);
            }
        }.execute();
    }
}
