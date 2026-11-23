package data_access;

import entity.GameDay;
import entity.Player;
import use_case.rest.RestGameStateDataAccessInterface;

/**
 * Keeps the game state in memory for the current session. To be replaced by file-based saving (UC4 step 7, UC5).
 */
public class InMemoryGameStateDataAccessObject implements RestGameStateDataAccessInterface {
    private GameDay currentDay;
    private final Player player;

    public InMemoryGameStateDataAccessObject(GameDay currentDay, Player player) {
        this.currentDay = currentDay;
        this.player = player;
    }

    @Override
    public GameDay getCurrentDay() {
        return currentDay;
    }

    @Override
    public void setCurrentDay(GameDay day) {
        this.currentDay = day;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
