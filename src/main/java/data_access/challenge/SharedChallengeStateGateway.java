package data_access.challenge;

import entity.AcademicRecord;
import entity.ChallengeResult;
import entity.Course;
import entity.CourseProgress;
import entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import use_case.challenge.ChallengeCourse;
import use_case.challenge.ChallengeOverview;
import use_case.challenge.ChallengeStateException;
import use_case.challenge.ChallengeStateGateway;
import use_case.choose_major.AcademicRecordAccess;
import use_case.question.QuestionTopic;

/** Adapts the team's current entities; course-to-topic decisions are supplied by the caller. */
public final class SharedChallengeStateGateway implements ChallengeStateGateway {
    private final Supplier<Player> players;
    private final AcademicRecordAccess academics;
    private final Map<String, QuestionTopic> topics;
    private final Map<String, Attempt> attempts = new HashMap<>();

    public SharedChallengeStateGateway(Supplier<Player> players, AcademicRecordAccess academics,
                                       Map<String, QuestionTopic> topics) {
        this.players = Objects.requireNonNull(players);
        this.academics = Objects.requireNonNull(academics);
        this.topics = Map.copyOf(topics);
    }

    @Override
    public synchronized ChallengeOverview loadOverview() throws ChallengeStateException {
        return snapshot(currentState());
    }

    @Override
    public synchronized ChallengeOverview startChallenge(String courseId, String attemptId, int energyCost)
            throws ChallengeStateException {
        requireAttemptId(attemptId);
        if (energyCost < 0) { throw new ChallengeStateException("Energy cost cannot be negative."); }
        State state = currentState();
        ChallengeOverview before = snapshot(state);
        Attempt previous = attempts.get(attemptId);
        if (previous != null) {
            requireSameState(previous, state, courseId);
            if (previous.abandoned) {
                throw new ChallengeStateException("This attempt was left. Start a new attempt.");
            }
            return before;
        }
        CourseProgress progress = assignedProgress(state, courseId);
        synchronized (state.player) {
            synchronized (progress) {
                if (progress.getChallengeResult().isPresent()) {
                    throw new ChallengeStateException("This course's class challenge is already completed.");
                }
                if (state.player.hasActiveActivity()) {
                    throw new ChallengeStateException("Finish or leave the current challenge or exam first.");
                }
                if (state.player.getEnergy() < energyCost) {
                    throw new ChallengeStateException("Not enough energy. Rest at Residence and try again.");
                }
                boolean acquired = false;
                try {
                    state.player.startActivity(attemptId);
                    acquired = true;
                    state.player.spendEnergy(energyCost);
                }
                catch (IllegalArgumentException | IllegalStateException exception) {
                    if (acquired) { state.player.finishActivity(attemptId); }
                    throw new ChallengeStateException(exception.getMessage());
                }
                attempts.put(attemptId, new Attempt(state, courseId));
            }
        }
        return new ChallengeOverview(state.player.getEnergy(), before.getCourses());
    }

    @Override
    public synchronized ChallengeOverview completeChallenge(String courseId, String attemptId,
                                                            ChallengeResult result)
            throws ChallengeStateException {
        if (result == null) { throw new ChallengeStateException("A completed challenge result is required."); }
        State state = currentState();
        ChallengeOverview before = snapshot(state);
        Attempt attempt = knownAttempt(attemptId);
        requireSameState(attempt, state, courseId);
        if (attempt.result != null) {
            if (attempt.result.getCorrectCount() != result.getCorrectCount()
                    || attempt.result.getQuestionCount() != result.getQuestionCount()) {
                throw new ChallengeStateException("This attempt already has a different result.");
            }
            return before;
        }
        if (attempt.abandoned) { throw new ChallengeStateException("This challenge was left unfinished."); }
        CourseProgress progress = assignedProgress(state, courseId);
        synchronized (state.player) {
            synchronized (progress) {
                if (progress.getChallengeResult().isPresent()) {
                    throw new ChallengeStateException("This course's class challenge is already completed.");
                }
                if (!state.player.finishActivity(attemptId)) {
                    throw new ChallengeStateException("This attempt no longer owns the active activity.");
                }
                progress.recordChallengeResult(result);
                attempt.result = result;
            }
        }
        return snapshot(state);
    }

    @Override
    public synchronized ChallengeOverview abandonChallenge(String attemptId) throws ChallengeStateException {
        State state = currentState();
        ChallengeOverview before = snapshot(state);
        Attempt attempt = knownAttempt(attemptId);
        requireSameState(attempt, state, attempt.courseId);
        if (attempt.abandoned || attempt.result != null) { return before; }
        if (!state.player.finishActivity(attemptId)) {
            throw new ChallengeStateException("This attempt no longer owns the active activity.");
        }
        attempt.abandoned = true;
        return before;
    }

    private State currentState() throws ChallengeStateException {
        Player player = players.get();
        AcademicRecord record = academics.getAcademicRecord();
        if (player == null || record == null) {
            throw new ChallengeStateException("The current player and academic record are required.");
        }
        return new State(player, record);
    }

    private ChallengeOverview snapshot(State state) throws ChallengeStateException {
        List<ChallengeCourse> courses = new ArrayList<>();
        if (state.record.hasConfirmedMajor()) {
            for (Course course : state.record.getSelectedMajor().getCourses()) {
                QuestionTopic topic = topics.get(course.getId());
                if (topic == null) {
                    throw new ChallengeStateException("No question topic has been configured for course "
                            + course.getId() + ".");
                }
                CourseProgress progress = assignedProgress(state, course.getId());
                courses.add(new ChallengeCourse(course.getId(), course.getName(), topic,
                        progress.getChallengeResult().orElse(null)));
            }
        }
        return new ChallengeOverview(state.player.getEnergy(), courses);
    }

    private CourseProgress assignedProgress(State state, String courseId) throws ChallengeStateException {
        if (state.record.hasConfirmedMajor()) {
            for (Course course : state.record.getSelectedMajor().getCourses()) {
                if (course.getId().equals(courseId)) {
                    CourseProgress progress = state.record.getCourseProgress().get(courseId);
                    if (progress != null && courseId.equals(progress.getCourseId())) { return progress; }
                }
            }
        }
        throw new ChallengeStateException("Choose an assigned course after confirming a major.");
    }

    private Attempt knownAttempt(String attemptId) throws ChallengeStateException {
        requireAttemptId(attemptId);
        Attempt attempt = attempts.get(attemptId);
        if (attempt == null) { throw new ChallengeStateException("Unknown class challenge attempt."); }
        return attempt;
    }

    private static void requireAttemptId(String attemptId) throws ChallengeStateException {
        if (attemptId == null || attemptId.isBlank()) {
            throw new ChallengeStateException("An attempt ID is required.");
        }
    }

    private static void requireSameState(Attempt attempt, State state, String courseId)
            throws ChallengeStateException {
        if (attempt.state.player != state.player || attempt.state.record != state.record
                || !attempt.courseId.equals(courseId)) {
            throw new ChallengeStateException("The player, academic record or course changed. Reopen Lecture Hall.");
        }
    }

    private static final class State {
        private final Player player;
        private final AcademicRecord record;

        private State(Player player, AcademicRecord record) {
            this.player = player;
            this.record = record;
        }
    }

    private static final class Attempt {
        private final State state;
        private final String courseId;
        private ChallengeResult result;
        private boolean abandoned;

        private Attempt(State state, String courseId) {
            this.state = state;
            this.courseId = courseId;
        }
    }
}
