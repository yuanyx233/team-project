package data_access;

import entity.DailyWeather;
import entity.GameDay;
import entity.Player;
import entity.WeatherCondition;
import entity.AcademicRecord;
import entity.Course;
import entity.CourseProgress;
import entity.ChallengeResult;
import entity.ExamResult;
import java.util.ArrayList;
import java.util.List;
import use_case.challenge.ChallengeCourse;
import use_case.challenge.ChallengeOverview;
import use_case.challenge.ChallengeStateException;
import use_case.challenge.ChallengeStateGateway;
import use_case.choose_major.AcademicRecordAccess;
import use_case.exam.ExamStateGateway;
import use_case.question.QuestionTopic;
import org.json.JSONException;
import org.json.JSONObject;
import use_case.continue_game.ContinueGameDataAccessInterface;
import use_case.continue_game.InvalidCheckpointException;
import use_case.rest.RestGameStateDataAccessInterface;
import use_case.rest.SaveFailedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Holds the current game state and saves it to / loads it from a JSON checkpoint file.
 * Only completed progress is saved; the academic record is to be added once the team's entities exist.
 */
public class FileGameStateDataAccessObject
        implements RestGameStateDataAccessInterface, ContinueGameDataAccessInterface,
        AcademicRecordAccess, ChallengeStateGateway, ExamStateGateway {
    static final int FORMAT_VERSION = 2;

    private final Path file;
    private GameDay currentDay;
    private Player player;
    private DailyWeather currentWeather;
    private AcademicRecord academicRecord = new AcademicRecord();
    private String activeAttempt = "";
    private String activeCourse = "";
    private String activeKind = "";
    private String completedAttempt = "";

    /**
     * @param file where the checkpoint is stored
     * @param currentDay the day a new game starts on
     * @param player the player a new game starts with
     */
    public FileGameStateDataAccessObject(Path file, GameDay currentDay, Player player) {
        this.file = file;
        this.currentDay = currentDay;
        this.player = player;
    }

    @Override
    public synchronized void saveCheckpoint() throws SaveFailedException {
        if (hasActiveActivity() && !activeKind.equals("rest")) {
            throw new SaveFailedException("Finish or leave the activity before saving.", null);
        }
        JSONObject json = new JSONObject()
                .put("version", FORMAT_VERSION)
                .put("dayNumber", currentDay.getDayNumber())
                .put("startDate", currentDay.getStartDate().toString())
                .put("energy", player.getEnergy())
                .put("maxEnergy", player.getMaxEnergy());
        json.put("academic", academicJson());
        if (currentWeather != null) {
            json.put("weather", new JSONObject()
                    .put("date", currentWeather.getDate().toString())
                    .put("condition", currentWeather.getCondition().name())
                    .put("high", currentWeather.getHighTemperature())
                    .put("low", currentWeather.getLowTemperature()));
        }
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            // Write to a temporary file first so a failed save never damages the previous checkpoint.
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, json.toString(2), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SaveFailedException("Could not save progress to " + file, e);
        }
    }

    @Override
    public boolean hasCheckpoint() {
        return Files.isRegularFile(file);
    }

    @Override
    public synchronized void loadCheckpoint() throws InvalidCheckpointException {
        if (hasActiveActivity()) {
            throw new InvalidCheckpointException("Finish or leave the activity before loading.");
        }
        try {
            JSONObject json = new JSONObject(Files.readString(file, StandardCharsets.UTF_8));
            if (json.getInt("version") != 1 && json.getInt("version") != FORMAT_VERSION) {
                throw new InvalidCheckpointException("Unsupported checkpoint version: " + json.getInt("version"));
            }
            GameDay day = new GameDay(json.getInt("dayNumber"), LocalDate.parse(json.getString("startDate")));
            int energy = json.getInt("energy");
            int maxEnergy = json.getInt("maxEnergy");
            if (maxEnergy <= 0 || energy < 0 || energy > maxEnergy) {
                throw new InvalidCheckpointException("Invalid energy in checkpoint");
            }
            DailyWeather weather = null;
            if (json.has("weather")) {
                JSONObject w = json.getJSONObject("weather");
                weather = new DailyWeather(LocalDate.parse(w.getString("date")),
                        WeatherCondition.valueOf(w.getString("condition")),
                        w.getDouble("high"), w.getDouble("low"));
            }
            AcademicRecord restoredRecord = readAcademic(json.getInt("version") == FORMAT_VERSION
                    ? json.getJSONObject("academic") : json.optJSONObject("academic"));
            // Only replace the state once everything has been read successfully.
            currentDay = day;
            player = new Player(energy, maxEnergy);
            currentWeather = weather;
            academicRecord = restoredRecord;
            completedAttempt = "";
        } catch (IOException | JSONException | DateTimeException | IllegalArgumentException e) {
            throw new InvalidCheckpointException("Checkpoint is damaged: " + e.getMessage(), e);
        }
    }

    @Override
    public GameDay getCurrentDay() {
        return currentDay;
    }

    @Override
    public void setCurrentDay(GameDay day) {
        this.currentDay = day;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public DailyWeather getCurrentWeather() {
        return currentWeather;
    }

    @Override
    public void setCurrentWeather(DailyWeather weather) {
        this.currentWeather = weather;
    }

    @Override
    public AcademicRecord getAcademicRecord() { return academicRecord; }

    @Override
    public synchronized boolean hasActiveActivity() { return !activeAttempt.isEmpty(); }

    @Override
    public synchronized void beginRest() {
        if (hasActiveActivity()) {
            throw new IllegalStateException("Finish or leave the current activity before resting.");
        }
        activeAttempt = "rest";
        activeKind = "rest";
    }

    @Override
    public synchronized void endRest() {
        if (activeKind.equals("rest")) { clearActivity(); }
    }

    @Override
    public QuestionTopic getQuestionTopic(String courseId) { return topicFor(courseId); }

    public static QuestionTopic topicFor(String courseId) {
        switch (courseId) {
            case "CS101": return QuestionTopic.PROGRAMMING_FOUNDATION;
            case "CS201": return QuestionTopic.SOFTWARE_DESIGN;
            case "BIO101": return QuestionTopic.CELL_BIOLOGY;
            case "BIO201": return QuestionTopic.ECOLOGY;
            case "HIST101": return QuestionTopic.WORLD_HISTORY;
            case "HIST201": return QuestionTopic.HISTORICAL_PERSPECTIVE;
            default: throw new IllegalStateException("Unknown course: " + courseId);
        }
    }

    @Override
    public synchronized ChallengeOverview loadOverview() {
        List<ChallengeCourse> courses = new ArrayList<>();
        if (academicRecord.hasConfirmedMajor()) {
            for (Course course : academicRecord.getSelectedMajor().getCourses()) {
                courses.add(new ChallengeCourse(course.getId(), course.getName(), topicFor(course.getId()),
                        progress(course.getId()).getChallengeResult()));
            }
        }
        return new ChallengeOverview(player.getEnergy(), courses);
    }

    private CourseProgress progress(String id) {
        CourseProgress progress = academicRecord.getCourseProgress().get(id);
        if (progress == null) {
            throw new IllegalStateException("Choose a major at the Registrar and select an assigned course.");
        }
        return progress;
    }

    private void startActivity(String courseId, String attemptId, int cost, String kind) {
        if (attemptId == null || attemptId.isBlank()) {
            throw new IllegalStateException("Missing activity attempt.");
        }
        if (activeAttempt.equals(attemptId) && activeCourse.equals(courseId) && activeKind.equals(kind)) {
            return;
        }
        if (hasActiveActivity()) {
            throw new IllegalStateException("Finish or leave the current activity first.");
        }
        if (completedAttempt.equals(attemptId)) {
            throw new IllegalStateException("This attempt is already completed.");
        }
        CourseProgress progress = progress(courseId);
        if ((kind.equals("exam") && progress.getExamResult() != null)
                || (kind.equals("challenge") && progress.getChallengeResult() != null)) {
            throw new IllegalStateException("This activity is already completed.");
        }
        if (cost < 0 || player.getEnergy() < cost) {
            throw new IllegalStateException("Not enough energy. Rest at Residence first.");
        }
        player.spendEnergy(cost);
        activeAttempt = attemptId;
        activeCourse = courseId;
        activeKind = kind;
    }

    private void requireAttempt(String courseId, String attemptId, String kind) {
        if (!activeAttempt.equals(attemptId) || !activeCourse.equals(courseId) || !activeKind.equals(kind)) {
            throw new IllegalStateException("This activity is no longer active.");
        }
    }

    private void clearActivity() {
        activeAttempt = "";
        activeCourse = "";
        activeKind = "";
    }

    @Override
    public synchronized void startExam(String courseId, String attemptId, int energyCost) {
        startActivity(courseId, attemptId, energyCost, "exam");
    }

    @Override
    public synchronized void completeExam(String courseId, String attemptId, ExamResult result) {
        if (completedAttempt.equals(attemptId)) { return; }
        requireAttempt(courseId, attemptId, "exam");
        progress(courseId).completeExam(result);
        completedAttempt = attemptId;
        clearActivity();
    }

    @Override
    public synchronized void abandonExam(String attemptId) {
        requireAttempt(activeCourse, attemptId, "exam");
        clearActivity();
    }

    @Override
    public synchronized ChallengeOverview startChallenge(String courseId, String attemptId, int energyCost)
            throws ChallengeStateException {
        try {
            startActivity(courseId, attemptId, energyCost, "challenge");
            return loadOverview();
        } catch (IllegalStateException exception) {
            throw new ChallengeStateException(exception.getMessage());
        }
    }

    @Override
    public synchronized ChallengeOverview completeChallenge(String courseId, String attemptId, ChallengeResult result)
            throws ChallengeStateException {
        try {
            if (!completedAttempt.equals(attemptId)) {
                requireAttempt(courseId, attemptId, "challenge");
                progress(courseId).completeChallenge(result);
                completedAttempt = attemptId;
                clearActivity();
            }
            return loadOverview();
        } catch (IllegalStateException exception) {
            throw new ChallengeStateException(exception.getMessage());
        }
    }

    @Override
    public synchronized ChallengeOverview abandonChallenge(String attemptId) throws ChallengeStateException {
        try {
            requireAttempt(activeCourse, attemptId, "challenge");
            clearActivity();
            return loadOverview();
        } catch (IllegalStateException exception) {
            throw new ChallengeStateException(exception.getMessage());
        }
    }

    private JSONObject academicJson() {
        JSONObject academic = new JSONObject();
        if (!academicRecord.hasConfirmedMajor()) { return academic; }
        academic.put("majorId", academicRecord.getSelectedMajor().getId());
        JSONObject courses = new JSONObject();
        for (CourseProgress progress : academicRecord.getCourseProgress().values()) {
            JSONObject entry = new JSONObject();
            ChallengeResult challenge = progress.getChallengeResult();
            if (challenge != null) {
                entry.put("challenge", new JSONObject().put("correct", challenge.getCorrectCount())
                        .put("total", challenge.getQuestionCount()));
            }
            ExamResult exam = progress.getExamResult();
            if (exam != null) {
                entry.put("exam", new JSONObject().put("correct", exam.getCorrectCount())
                        .put("answered", exam.getAnsweredCount()).put("won", exam.hasWon()));
            }
            courses.put(progress.getCourseId(), entry);
        }
        return academic.put("courses", courses);
    }

    private AcademicRecord readAcademic(JSONObject json) {
        AcademicRecord record = new AcademicRecord();
        if (json == null || json.isEmpty()) { return record; }
        record.confirmMajor(InMemoryMajorCatalog.predefined().findById(json.getString("majorId"))
                .orElseThrow(() -> new IllegalArgumentException("Unknown saved major.")));
        JSONObject courses = json.getJSONObject("courses");
        if (!courses.keySet().equals(record.getCourseProgress().keySet())) {
            throw new IllegalArgumentException("Saved courses do not match the major.");
        }
        for (String id : courses.keySet()) {
            CourseProgress progress = record.getCourseProgress().get(id);
            JSONObject entry = courses.getJSONObject(id);
            if (entry.has("challenge")) {
                JSONObject challenge = entry.getJSONObject("challenge");
                if (challenge.getInt("total") != 3) { throw new IllegalArgumentException("Invalid class length."); }
                progress.completeChallenge(new ChallengeResult(challenge.getInt("correct"), 3));
            }
            if (entry.has("exam")) {
                JSONObject exam = entry.getJSONObject("exam");
                progress.completeExam(new ExamResult(exam.getInt("correct"), exam.getInt("answered"),
                        exam.getBoolean("won")));
            }
        }
        return record;
    }
}
