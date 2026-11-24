package interface_adapter.choose_major;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public final class ChooseMajorViewModel {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private ChooseMajorState state = new ChooseMajorState(List.of(), null, false, "", false);

    public ChooseMajorState getState() { return state; }
    public void setState(ChooseMajorState state) {
        ChooseMajorState old = this.state;
        this.state = state;
        changes.firePropertyChange("state", old, state);
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }
}
