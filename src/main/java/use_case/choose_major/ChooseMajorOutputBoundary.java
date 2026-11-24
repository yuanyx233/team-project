package use_case.choose_major;

public interface ChooseMajorOutputBoundary {
    void present(ChooseMajorOutputData output);
    void presentError(String message);
}
