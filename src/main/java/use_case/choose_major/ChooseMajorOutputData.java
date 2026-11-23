package use_case.choose_major;

import java.util.List;

/** Snapshot of the selection screen after a successful action. */
public final class ChooseMajorOutputData {
    private final List<MajorData> majors;
    private final MajorData selected;
    private final boolean confirmed;
    private final String message;

    public ChooseMajorOutputData(List<MajorData> majors, MajorData selected,
                                 boolean confirmed, String message) {
        this.majors = List.copyOf(majors);
        this.selected = selected;
        this.confirmed = confirmed;
        this.message = message;
    }
    public List<MajorData> getMajors() { return majors; }
    public MajorData getSelected() { return selected; }
    public boolean isConfirmed() { return confirmed; }
    public String getMessage() { return message; }
}
