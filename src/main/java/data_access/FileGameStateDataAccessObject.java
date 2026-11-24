package data_access;

import entity.AcademicRecord;
import entity.Course;
import entity.DailyWeather;
import entity.GameDay;
import entity.Major;
import entity.Player;
import entity.WeatherCondition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import use_case.choose_major.AcademicRecordAccess;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the current game state and saves it to / loads it from a JSON checkpoint file.
 * Only completed progress is saved: the day, energy, weather, and the confirmed major with its courses.
 * Version 1 checkpoints (saved before the academic record existed) load with no major.
 */
public class FileGameStateDataAccessObject
        implements RestGameStateDataAccessInterface, ContinueGameDataAccessInterface, AcademicRecordAccess {
    static final int FORMAT_VERSION = 2;
    private static final int OLDEST_SUPPORTED_VERSION = 1;

    private final Path file;
    private GameDay currentDay;
    private Player player;
    private DailyWeather currentWeather;
    private AcademicRecord academicRecord = new AcademicRecord();

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
    public void saveCheckpoint() throws SaveFailedException {
        JSONObject json = new JSONObject()
                .put("version", FORMAT_VERSION)
                .put("dayNumber", currentDay.getDayNumber())
                .put("startDate", currentDay.getStartDate().toString())
                .put("energy", player.getEnergy())
                .put("maxEnergy", player.getMaxEnergy());
        if (currentWeather != null) {
            json.put("weather", new JSONObject()
                    .put("date", currentWeather.getDate().toString())
                    .put("condition", currentWeather.getCondition().name())
                    .put("high", currentWeather.getHighTemperature())
                    .put("low", currentWeather.getLowTemperature()));
        }
        if (academicRecord.hasConfirmedMajor()) {
            json.put("major", toJson(academicRecord.getSelectedMajor()));
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
    public void loadCheckpoint() throws InvalidCheckpointException {
        try {
            JSONObject json = new JSONObject(Files.readString(file, StandardCharsets.UTF_8));
            int version = json.getInt("version");
            if (version < OLDEST_SUPPORTED_VERSION || version > FORMAT_VERSION) {
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
            AcademicRecord record = new AcademicRecord();
            if (json.has("major")) {
                record.confirmMajor(majorFromJson(json.getJSONObject("major")));
            }
            // Only replace the state once everything has been read successfully.
            currentDay = day;
            player = new Player(energy, maxEnergy);
            currentWeather = weather;
            academicRecord = record;
        } catch (IOException | JSONException | DateTimeException | IllegalArgumentException e) {
            throw new InvalidCheckpointException("Checkpoint is damaged: " + e.getMessage(), e);
        }
    }

    private static JSONObject toJson(Major major) {
        JSONArray courses = new JSONArray();
        for (Course course : major.getCourses()) {
            courses.put(new JSONObject()
                    .put("id", course.getId())
                    .put("name", course.getName())
                    .put("questionCategory", course.getQuestionCategory()));
        }
        return new JSONObject()
                .put("id", major.getId())
                .put("name", major.getName())
                .put("description", major.getDescription())
                .put("sourceUrl", major.getSourceUrl())
                .put("source", major.getSource().name())
                .put("courses", courses);
    }

    private static Major majorFromJson(JSONObject json) {
        JSONArray array = json.getJSONArray("courses");
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject course = array.getJSONObject(i);
            courses.add(new Course(course.getString("id"), course.getString("name"),
                    course.getString("questionCategory")));
        }
        return new Major(json.getString("id"), json.getString("name"), json.getString("description"),
                courses, json.getString("sourceUrl"), Major.Source.valueOf(json.getString("source")));
    }

    /** @return the current academic record; a new object after each successful load */
    @Override
    public AcademicRecord getAcademicRecord() {
        return academicRecord;
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
}
