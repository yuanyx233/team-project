package use_case.exam;

import entity.Course;
import entity.CourseProgress;
import entity.ExamBattle;
import entity.ExamResult;
import entity.Question;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import use_case.question.QuestionGateway;
import use_case.question.QuestionLoadException;
import use_case.question.QuestionPurpose;
import static use_case.exam.ExamOutputData.Stage;

public final class ExamInteractor implements ExamInputBoundary {
    public static final int ENERGY_COST = 40;
    public static final int QUESTION_COUNT = 5;

    private final ExamStateGateway state;
    private final QuestionGateway questions;
    private final ExamOutputBoundary output;
    private Stage stage = Stage.COURSES;
    private Course course;
    private List<Question> loaded = List.of();
    private ExamBattle battle;
    private ExamResult result;
    private boolean resultRecorded;
    private String attemptId = "";
    private int questionIndex;
    private int eliminatedOption = -1;
    private String feedback = "";

    public ExamInteractor(ExamStateGateway state, QuestionGateway questions, ExamOutputBoundary output) {
        this.state = state;
        this.questions = questions;
        this.output = output;
    }

    @Override
    public synchronized void execute(ExamInputData input) {
        try {
            dispatch(input);
            present();
        } catch (QuestionLoadException | IllegalArgumentException | IllegalStateException exception) {
            present();
            output.presentError(exception.getMessage());
        }
    }

    private void dispatch(ExamInputData input) throws QuestionLoadException {
        switch (input.action) {
            case OPEN:
                if (battle == null || battle.isFinished()) {
                    if (battle != null) { recordResult(); }
                    battle = null;
                    course = null;
                    result = null;
                    feedback = "";
                    stage = Stage.COURSES;
                }
                break;
            case SELECT: select(input.courseId); break;
            case START: start(input.courseId); break;
            case ANSWER:
                if (matches(input) && stage == Stage.QUESTION) { answer(input.answerIndex); }
                break;
            case NEXT:
                if (matches(input) && stage == Stage.FEEDBACK) {
                    if (battle.isFinished()) {
                        recordResult();
                        stage = Stage.RESULT;
                    } else {
                        questionIndex++;
                        eliminatedOption = -1;
                        feedback = "";
                        stage = Stage.QUESTION;
                    }
                }
                break;
            case HINT:
                if (matches(input) && stage == Stage.QUESTION) {
                    eliminatedOption = battle.useHint(currentQuestion().getCorrectAnswer());
                }
                break;
            case SHIELD:
                if (matches(input) && stage == Stage.QUESTION) { battle.useShield(); }
                break;
            case LEAVE: leave(input.confirmed); break;
            default: throw new IllegalArgumentException("Unknown exam action.");
        }
    }

    private void select(String id) {
        if (battle != null && !battle.isFinished()) {
            throw new IllegalStateException("Finish or leave this exam first.");
        }
        if (battle != null) { recordResult(); }
        if (!state.getAcademicRecord().hasConfirmedMajor()) {
            throw new IllegalStateException("Choose your major at the Registrar first.");
        }
        course = state.getAcademicRecord().getSelectedMajor().getCourses().stream()
                .filter(assigned -> assigned.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Select one of your assigned courses."));
        result = progress().getExamResult();
        battle = null;
        feedback = "";
        stage = result == null ? Stage.CONFIRM : Stage.RESULT;
    }

    private void start(String id) throws QuestionLoadException {
        if (stage != Stage.CONFIRM || course == null || !course.getId().equals(id)) { return; }
        if (progress().getExamResult() != null) {
            result = progress().getExamResult();
            stage = Stage.RESULT;
            return;
        }
        if (state.getPlayer().getEnergy() < ENERGY_COST) {
            throw new IllegalStateException("Not enough energy. Rest at Residence first.");
        }
        List<Question> candidate = questions.getQuestions(state.getQuestionTopic(id), QuestionPurpose.EXAM,
                QUESTION_COUNT);
        if (candidate == null || candidate.size() != QUESTION_COUNT || candidate.stream().anyMatch(q -> q == null
                || q.getOptions().size() != 4) || new HashSet<>(candidate.stream()
                .map(Question::getId).collect(Collectors.toList())).size() != QUESTION_COUNT) {
            throw new QuestionLoadException("The service must return five distinct four-option questions.");
        }
        String newAttempt = UUID.randomUUID().toString();
        state.startExam(id, newAttempt, ENERGY_COST);
        loaded = List.copyOf(candidate);
        battle = new ExamBattle(progress().getPreparationPoints());
        resultRecorded = false;
        attemptId = newAttempt;
        questionIndex = 0;
        eliminatedOption = -1;
        stage = Stage.QUESTION;
    }

    private void answer(int answerIndex) {
        if (answerIndex < 0 || answerIndex >= 4 || answerIndex == eliminatedOption) {
            throw new IllegalArgumentException("Select an available answer before submitting.");
        }
        boolean correct = currentQuestion().isCorrect(answerIndex);
        boolean shield = battle.isShieldActive();
        battle.submitAnswer(correct);
        feedback = correct ? "Correct! The Final Monster takes 20 damage."
                : shield ? "Incorrect. Your Shield blocked the attack."
                : "Incorrect. You take 20 damage.";
        stage = Stage.FEEDBACK;
        if (battle.isFinished()) {
            result = new ExamResult(battle);
            // Record before the player can leave the final feedback screen.
            recordResult();
        }
    }

    private void leave(boolean confirmed) {
        if (battle != null) {
            if (battle.isFinished()) {
                recordResult();
            } else {
                if (!confirmed) { throw new IllegalStateException("Confirm leaving this exam first."); }
                state.abandonExam(attemptId);
            }
        }
        battle = null;
        stage = Stage.LEFT;
    }

    private boolean matches(ExamInputData input) {
        return battle != null && attemptId.equals(input.attemptId) && questionIndex == input.questionIndex;
    }

    private void recordResult() {
        if (!resultRecorded) {
            state.completeExam(course.getId(), attemptId, result);
            resultRecorded = true;
        }
    }

    private CourseProgress progress() {
        return state.getAcademicRecord().getCourseProgress().get(course.getId());
    }

    private Question currentQuestion() { return loaded.get(questionIndex); }

    private void present() {
        boolean showQuestion = battle != null && (stage == Stage.QUESTION || stage == Stage.FEEDBACK);
        output.present(new ExamOutputData(stage, state.getAcademicRecord(), state.getPlayer().getEnergy(), course,
                battle, attemptId, questionIndex, showQuestion ? currentQuestion().getText() : "",
                showQuestion ? currentQuestion().getOptions() : List.of(), eliminatedOption, feedback, result));
    }
}
