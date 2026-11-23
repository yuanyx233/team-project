package interface_adapter.rest;

import use_case.rest.RestOutputBoundary;
import use_case.rest.RestOutputData;

/**
 * Formats the new day for display.
 */
public class RestPresenter implements RestOutputBoundary {
    private final RestViewModel viewModel;

    public RestPresenter(RestViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(RestOutputData data) {
        viewModel.setState(
                String.format("Day %d (%s)", data.getDayNumber(), data.getDate()),
                String.format("Weather: %s, %.1f°C / %.1f°C",
                        data.getCondition(), data.getHighTemperature(), data.getLowTemperature()),
                String.format("Energy: %d / %d", data.getEnergy(), data.getMaxEnergy()));
    }
}
