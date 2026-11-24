package use_case.exam;

public interface ExamOutputBoundary {
    void present(ExamOutputData data);
    void presentError(String message);
}
