package view;

import interface_adapter.exam.ExamController;
import interface_adapter.exam.ExamViewModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import use_case.exam.ExamOutputData.Stage;

public final class ExamView extends JPanel {
    private final ExamController controller;
    private final ExamViewModel model;
    private final Runnable backToStreet;
    private final JLabel energy = new JLabel();
    private final JLabel message = new JLabel(" ");
    private final JPanel body = new JPanel(new BorderLayout(12, 12));
    private final JButton back = new JButton("Back to Street");
    private boolean busy;
    private int selectedAnswer = -1;
    private String displayedQuestion = "";
    private ExamViewModel.State lastLeft;

    public ExamView(ExamController controller, ExamViewModel model, Runnable backToStreet) {
        this.controller = controller;
        this.model = model;
        this.backToStreet = backToStreet;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JPanel heading = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Exam Centre — Final Monster");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        heading.add(title, BorderLayout.WEST);
        heading.add(energy, BorderLayout.EAST);
        JPanel north = new JPanel(new GridLayout(0, 1, 4, 4));
        north.add(heading);
        north.add(message);
        add(north, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(back, BorderLayout.SOUTH);
        back.addActionListener(event -> requestLeave());
        model.addListener(event -> SwingUtilities.invokeLater(this::render));
    }

    public void refresh() { run(controller::open); }

    private void requestLeave() {
        if (busy) { return; }
        ExamViewModel.State state = model.getState();
        boolean active = state != null && !state.finished
                && (state.stage == Stage.QUESTION || state.stage == Stage.FEEDBACK);
        if (active && JOptionPane.showConfirmDialog(this,
                "Leave the exam? Spent energy will not be refunded. No grade will be recorded.",
                "Leave exam", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        run(() -> controller.leave(true));
    }

    private void run(Runnable action) {
        if (busy) { return; }
        busy = true;
        message.setText("Loading...");
        enableControls(this, false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() { action.run(); return null; }
            @Override protected void done() {
                busy = false;
                try {
                    get();
                    render();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    render();
                    message.setText("Interrupted. Please try again.");
                } catch (ExecutionException exception) {
                    render();
                    message.setText("Could not finish the action. Please try again.");
                    java.util.logging.Logger.getLogger(ExamView.class.getName())
                            .log(java.util.logging.Level.WARNING, "Exam action failed", exception.getCause());
                }
            }
        }.execute();
    }

    private void render() {
        ExamViewModel.State state = model.getState();
        if (state == null) { return; }
        energy.setText(state.energy);
        message.setText(busy ? "Loading..." : model.getError().isBlank() ? " " : model.getError());
        body.removeAll();
        back.setEnabled(!busy);
        if (state.stage == Stage.LEFT) {
            if (!busy && lastLeft != state) {
                lastLeft = state;
                backToStreet.run();
            }
        } else if (state.stage == Stage.COURSES) {
            showCourses(state);
        } else if (state.stage == Stage.CONFIRM) {
            body.add(text(state.courseName + "\n\n" + state.instructions, 8), BorderLayout.CENTER);
            JButton start = new JButton("Start Boss Battle");
            start.addActionListener(event -> run(() -> controller.start(state.courseId)));
            body.add(start, BorderLayout.SOUTH);
        } else if (state.stage == Stage.RESULT) {
            body.add(text(state.courseName + "\n\n" + state.result, 8), BorderLayout.CENTER);
            JButton courses = new JButton("Other Courses");
            courses.addActionListener(event -> refresh());
            body.add(courses, BorderLayout.SOUTH);
        } else {
            showBattle(state);
        }
        if (busy) { enableControls(this, false); }
        revalidate();
        repaint();
    }

    private void showCourses(ExamViewModel.State state) {
        if (state.courses.isEmpty()) {
            body.add(text("Choose your major at the Registrar's Office first.", 3), BorderLayout.CENTER);
            return;
        }
        JList<ExamViewModel.CourseItem> courses = new JList<>(
                state.courses.toArray(new ExamViewModel.CourseItem[0]));
        courses.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        courses.setSelectedIndex(0);
        body.add(new JScrollPane(courses), BorderLayout.CENTER);
        JButton select = new JButton("View Exam");
        select.addActionListener(event -> {
            ExamViewModel.CourseItem course = courses.getSelectedValue();
            if (course != null) { run(() -> controller.select(course.id)); }
        });
        body.add(select, BorderLayout.SOUTH);
    }

    private void showBattle(ExamViewModel.State state) {
        JPanel fighters = new JPanel(new GridLayout(0, 1, 4, 4));
        JLabel monster = new JLabel("FINAL MONSTER", JLabel.CENTER);
        monster.setFont(monster.getFont().deriveFont(Font.BOLD, 26f));
        monster.setForeground(new Color(120, 35, 100));
        fighters.add(monster);
        fighters.add(health("Monster", state.bossHp));
        fighters.add(health("You", state.playerHp));
        body.add(fighters, BorderLayout.NORTH);

        JPanel question = new JPanel();
        question.setLayout(new BoxLayout(question, BoxLayout.Y_AXIS));
        question.add(text(state.courseName + " — Question " + (state.questionIndex + 1) + " / 5\n"
                + state.question, 4));
        String key = state.attemptId + ":" + state.questionIndex;
        if (!key.equals(displayedQuestion)) { selectedAnswer = -1; displayedQuestion = key; }
        if (selectedAnswer == state.eliminatedOption) { selectedAnswer = -1; }
        ButtonGroup answers = new ButtonGroup();
        for (int i = 0; i < state.options.size(); i++) {
            final int index = i;
            JRadioButton option = new JRadioButton((char) ('A' + i) + ". " + state.options.get(i));
            option.setName("exam.answer." + i);
            option.setSelected(selectedAnswer == i);
            option.setEnabled(state.stage == Stage.QUESTION && i != state.eliminatedOption);
            option.addActionListener(event -> selectedAnswer = index);
            answers.add(option);
            question.add(option);
        }
        if (!state.feedback.isBlank()) { question.add(text(state.feedback, 2)); }
        if (state.shieldActive) { question.add(new JLabel("Shield ready: blocks your next wrong answer.")); }
        body.add(new JScrollPane(question), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton hint = new JButton("Hint");
        hint.setEnabled(state.stage == Stage.QUESTION && state.hintAvailable);
        hint.addActionListener(event -> run(() -> controller.hint(state.attemptId, state.questionIndex)));
        JButton shield = new JButton("Shield");
        shield.setEnabled(state.stage == Stage.QUESTION && state.shieldAvailable);
        shield.addActionListener(event -> run(() -> controller.shield(state.attemptId, state.questionIndex)));
        actions.add(hint);
        actions.add(shield);
        JButton submit = new JButton(state.stage == Stage.QUESTION ? "Submit Answer"
                : state.finished ? "View Result" : "Next Question");
        submit.addActionListener(event -> {
            int answer = selectedAnswer;
            if (state.stage == Stage.QUESTION) {
                run(() -> controller.answer(state.attemptId, state.questionIndex, answer));
            } else {
                run(() -> controller.next(state.attemptId, state.questionIndex));
            }
        });
        actions.add(submit);
        body.add(actions, BorderLayout.SOUTH);
    }

    private JProgressBar health(String name, int hp) {
        JProgressBar bar = new JProgressBar(0, 60);
        bar.setValue(hp);
        bar.setStringPainted(true);
        bar.setString(name + ": " + hp + " / 60 HP");
        return bar;
    }

    private JTextArea text(String value, int rows) {
        JTextArea text = new JTextArea(value, rows, 30);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setMaximumSize(new Dimension(Integer.MAX_VALUE, text.getPreferredSize().height));
        return text;
    }

    private void enableControls(Container container, boolean enabled) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton || component instanceof JRadioButton || component instanceof JList) {
                component.setEnabled(enabled);
            }
            if (component instanceof Container) { enableControls((Container) component, enabled); }
        }
    }
}
