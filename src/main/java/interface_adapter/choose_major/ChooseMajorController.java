package interface_adapter.choose_major;

import use_case.choose_major.ChooseMajorInputBoundary;
import use_case.choose_major.ChooseMajorInputData;

public final class ChooseMajorController {
    private final ChooseMajorInputBoundary input;
    public ChooseMajorController(ChooseMajorInputBoundary input) { this.input = input; }
    public void loadMajors() { input.loadMajors(); }
    public void preview(String id) { input.preview(new ChooseMajorInputData(id)); }
    public void confirm(String id) { input.confirm(new ChooseMajorInputData(id)); }
    public void cancel() { input.cancel(); }
}
