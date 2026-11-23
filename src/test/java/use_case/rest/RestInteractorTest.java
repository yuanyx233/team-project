package use_case.rest;

import entity.DailyWeather;
import entity.GameDay;
import entity.Player;
import entity.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RestInteractorTest {
    private static final LocalDate START = LocalDate.of(2025, 9, 2);

    private static class FakeGameState implements RestGameStateDataAccessInterface {
        GameDay day = new GameDay(1, START);
        final Player player = new Player(20, 100);

        public GameDay getCurrentDay() { return day; }
        public void setCurrentDay(GameDay day) { this.day = day; }
        public Player getPlayer() { return player; }
    }

    private static RestWeatherDataAccessInterface weatherOf(WeatherCondition condition) {
        return date -> new DailyWeather(date, condition, 5.0, -2.0);
    }

    private static class CapturingPresenter implements RestOutputBoundary {
        RestOutputData output;
        public void prepareSuccessView(RestOutputData output) { this.output = output; }
    }

    @Test
    void restAdvancesDayAndRestoresEnergyByWeather() {
        FakeGameState state = new FakeGameState();
        CapturingPresenter presenter = new CapturingPresenter();

        new RestInteractor(state, weatherOf(WeatherCondition.RAIN), presenter).execute();

        assertEquals(2, state.getCurrentDay().getDayNumber());
        assertEquals(85, state.getPlayer().getEnergy());
        assertEquals(2, presenter.output.getDayNumber());
        assertEquals(START.plusDays(1), presenter.output.getDate());
        assertEquals(WeatherCondition.RAIN, presenter.output.getCondition());
        assertEquals(85, presenter.output.getEnergy());
        assertEquals(100, presenter.output.getMaxEnergy());
    }

    @Test
    void weatherIsLookedUpForTheNewDaysRealDate() {
        FakeGameState state = new FakeGameState();
        LocalDate[] requested = new LocalDate[1];
        RestWeatherDataAccessInterface weather = date -> {
            requested[0] = date;
            return new DailyWeather(date, WeatherCondition.CLEAR, 20.0, 10.0);
        };

        new RestInteractor(state, weather, new CapturingPresenter()).execute();

        assertEquals(START.plusDays(1), requested[0]);
    }
}
