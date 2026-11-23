package use_case.rest;

import entity.DailyWeather;
import entity.GameDay;
import entity.Player;

/**
 * UC4: the player rests, the game advances to the next day, and energy is restored according to that day's weather.
 * Saving the checkpoint (UC4 step 7) is not implemented yet.
 */
public class RestInteractor implements RestInputBoundary {
    private final RestGameStateDataAccessInterface gameState;
    private final RestWeatherDataAccessInterface weatherSource;
    private final RestOutputBoundary presenter;

    public RestInteractor(RestGameStateDataAccessInterface gameState,
                          RestWeatherDataAccessInterface weatherSource,
                          RestOutputBoundary presenter) {
        this.gameState = gameState;
        this.weatherSource = weatherSource;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        GameDay newDay = gameState.getCurrentDay().next();
        gameState.setCurrentDay(newDay);

        DailyWeather weather = weatherSource.getWeather(newDay.getRealDate());
        Player player = gameState.getPlayer();
        player.restoreEnergyToPercent(weather.getEnergyRestorePercent());

        presenter.prepareSuccessView(new RestOutputData(
                newDay.getDayNumber(), newDay.getRealDate(), weather.getCondition(),
                weather.getHighTemperature(), weather.getLowTemperature(),
                player.getEnergy(), player.getMaxEnergy()));
    }
}
