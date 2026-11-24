package interface_adapter.choose_major;

import use_case.choose_major.ChooseMajorOutputBoundary;
import use_case.choose_major.ChooseMajorOutputData;

public final class ChooseMajorPresenter implements ChooseMajorOutputBoundary {
    private final ChooseMajorViewModel model;
    public ChooseMajorPresenter(ChooseMajorViewModel model) { this.model = model; }
    @Override
    public void present(ChooseMajorOutputData output) {
        model.setState(new ChooseMajorState(output.getMajors(), output.getSelected(),
                output.isConfirmed(), output.getMessage(), false));
    }
    @Override
    public void presentError(String message) {
        ChooseMajorState old = model.getState();
        model.setState(new ChooseMajorState(old.getMajors(), old.getSelected(),
                old.isConfirmed(), message, true));
    }
}
