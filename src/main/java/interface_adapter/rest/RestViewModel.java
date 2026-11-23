package interface_adapter.rest;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Holds the text shown by the residence view and notifies it of changes.
 */
public class RestViewModel {
    public static final String STATE_PROPERTY = "state";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private String dayText = "";
    private String weatherText = "";
    private String energyText = "";

    public void setState(String dayText, String weatherText, String energyText) {
        this.dayText = dayText;
        this.weatherText = weatherText;
        this.energyText = energyText;
        support.firePropertyChange(STATE_PROPERTY, null, this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public String getDayText() {
        return dayText;
    }

    public String getWeatherText() {
        return weatherText;
    }

    public String getEnergyText() {
        return energyText;
    }
}
