package interface_adapter.choose_major;

import use_case.choose_major.MajorData;
import java.util.List;

/** Immutable presentation state. */
public final class ChooseMajorState {
    private final List<MajorData> majors;
    private final MajorData selected;
    private final boolean confirmed;
    private final String message;
    private final boolean error;

    public ChooseMajorState(List<MajorData> majors, MajorData selected, boolean confirmed,
                            String message, boolean error) {
        this.majors = List.copyOf(majors);
        this.selected = selected;
        this.confirmed = confirmed;
        this.message = message;
        this.error = error;
    }
    public List<MajorData> getMajors() { return majors; }
    public MajorData getSelected() { return selected; }
    public boolean isConfirmed() { return confirmed; }
    public String getMessage() { return message; }
    public boolean isError() { return error; }
}
