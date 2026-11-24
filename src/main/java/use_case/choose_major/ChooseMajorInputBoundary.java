package use_case.choose_major;

/** Several actions implement one coherent goal: choosing a major. */
public interface ChooseMajorInputBoundary {
    void loadMajors();
    void preview(ChooseMajorInputData input);
    void confirm(ChooseMajorInputData input);
    void cancel();
}
