package view;

import interface_adapter.choose_major.ChooseMajorController;
import interface_adapter.choose_major.ChooseMajorState;
import interface_adapter.choose_major.ChooseMajorViewModel;
import use_case.choose_major.MajorData;
import javax.swing.*;
import java.awt.*;

/** Swing-only UI; it cannot mutate the player's record. */
public final class ChooseMajorView extends JPanel {
    private final DefaultListModel<MajorData> listModel = new DefaultListModel<>();
    private final JList<MajorData> majors = new JList<>(listModel);
    private final JTextArea preview = new JTextArea();
    private final JTextArea message = new JTextArea();
    private final JButton confirm = new JButton("Confirm Major");
    private boolean rendering;
    private boolean loading;
    private ChooseMajorState latestState;

    public ChooseMajorView(ChooseMajorController controller, ChooseMajorViewModel model,
                           Runnable returnToStreet) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(new JLabel("Sidney Smith - Registrar's Office"), BorderLayout.NORTH);
        majors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        majors.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list,
                        value instanceof MajorData ? ((MajorData) value).getName()
                                + (((MajorData) value).isPlayable() ? "" : " (Not available)") : value,
                        index, selected, focus);
            }
        });
        preview.setEditable(false); preview.setLineWrap(true); preview.setWrapStyleWord(true);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(majors), new JScrollPane(preview));
        split.setResizeWeight(0.35); add(split, BorderLayout.CENTER);
        message.setEditable(false); message.setLineWrap(true); message.setWrapStyleWord(true);
        message.setOpaque(false); message.setRows(3);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(message, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel Preview");
        JButton back = new JButton("Back to Street");
        buttons.add(cancel); buttons.add(confirm); buttons.add(back);
        bottom.add(buttons, BorderLayout.SOUTH); add(bottom, BorderLayout.SOUTH);
        majors.addListSelectionListener(event -> {
            if (!rendering && !event.getValueIsAdjusting() && majors.getSelectedValue() != null) {
                controller.preview(majors.getSelectedValue().getId());
            }
        });
        confirm.addActionListener(event -> {
            MajorData selected = model.getState().getSelected();
            controller.confirm(selected == null ? null : selected.getId());
        });
        cancel.addActionListener(event -> controller.cancel());
        back.addActionListener(event -> { controller.cancel(); returnToStreet.run(); });
        model.addPropertyChangeListener(event -> {
            if (SwingUtilities.isEventDispatchThread()) { render(model.getState()); }
            else { SwingUtilities.invokeLater(() -> render(model.getState())); }
        });
        render(model.getState());
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        if (latestState != null) { render(latestState); }
    }

    private void render(ChooseMajorState state) {
        latestState = state;
        rendering = true;
        try {
            listModel.clear();
            for (MajorData major : state.getMajors()) { listModel.addElement(major); }
            MajorData selected = state.getSelected();
            if (selected == null) {
                majors.clearSelection(); preview.setText("Select a major to see its courses.");
            }
            else {
                for (int i = 0; i < listModel.size(); i++) {
                    if (listModel.get(i).getId().equals(selected.getId())) { majors.setSelectedIndex(i); }
                }
                String courses = selected.isPlayable()
                        ? "\n\nFictional game courses automatically assigned:\n- "
                            + String.join("\n- ", selected.getCourseNames())
                        : "\n\nThis major is not available in the game yet.";
                preview.setText(selected.getName() + "\n\nGame description (local):\n"
                        + selected.getDescription() + courses + "\n\nData source: "
                        + selected.getSourceLabel() + "\nOfficial program page: " + selected.getSourceUrl());
            }
            majors.setEnabled(!state.isConfirmed() && !loading);
            confirm.setEnabled(selected != null && selected.isPlayable() && !state.isConfirmed() && !loading);
            message.setForeground(state.isError() ? new Color(150, 20, 20) : Color.DARK_GRAY);
            message.setText(loading ? "Fetching eight U of T program titles. Local fallback is ready if unavailable..." : state.getMessage());
        }
        finally { rendering = false; }
    }
}
