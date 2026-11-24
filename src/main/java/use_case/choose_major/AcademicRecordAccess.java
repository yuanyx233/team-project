package use_case.choose_major;

import entity.AcademicRecord;

/**
 * Returns the current academic record on every action, including after a load.
 * The app can implement this by delegating to the team's GameStateAccess.
 * This interface does not define or replace the team's GameState classes.
 */
@FunctionalInterface
public interface AcademicRecordAccess {
    AcademicRecord getAcademicRecord();
}
