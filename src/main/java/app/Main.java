package app;

import data_access.BundledWeatherDataAccessObject;
import data_access.CampusMapResourceLoader;
import data_access.FileGameStateDataAccessObject;
import data_access.OpenMeteoWeatherDataAccessObject;
import data_access.InMemoryMajorCatalog;
import data_access.question.HuggingFaceQuestionGateway;
import entity.Building;
import entity.CampusMap;
import entity.GameDay;
import entity.Player;
import entity.Walker;
import interface_adapter.ViewManagerModel;
import interface_adapter.map.MapController;
import interface_adapter.map.MapViewModel;
import interface_adapter.continue_game.ContinueGameController;
import interface_adapter.continue_game.ContinueGamePresenter;
import interface_adapter.continue_game.TitleViewModel;
import interface_adapter.rest.ResidenceFormat;
import interface_adapter.rest.RestController;
import interface_adapter.rest.RestPresenter;
import interface_adapter.rest.RestViewModel;
import interface_adapter.choose_major.ChooseMajorController;
import interface_adapter.choose_major.ChooseMajorPresenter;
import interface_adapter.choose_major.ChooseMajorViewModel;
import interface_adapter.exam.ExamController;
import interface_adapter.exam.ExamPresenter;
import interface_adapter.exam.ExamViewModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.file.Path;
import java.time.LocalDate;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import use_case.continue_game.ContinueGameInteractor;
import use_case.rest.RestInteractor;
import use_case.choose_major.ChooseMajorInteractor;
import use_case.exam.ExamInteractor;
import use_case.question.QuestionGateway;
import view.GameView;
import view.map.MapView;
import view.ResidenceView;
import view.TitleView;
import view.ChooseMajorView;
import view.ClassChallengeView;
import view.ExamView;

public class Main {
    private static final LocalDate START_DATE = LocalDate.of(2025, 9, 2);
    private static final int MAX_ENERGY = 100;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Stressdew Valley");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Path saveFile = Path.of(System.getProperty("user.home"), ".stressdew-valley", "checkpoint.json");
            window.setContentPane(createView(saveFile));
            window.setSize(900, 650);
            window.setMinimumSize(new Dimension(720, 480));
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }

    public static JPanel createView(Path saveFile) {
        return createView(saveFile, new HuggingFaceQuestionGateway());
    }

    public static JPanel createView(Path saveFile, QuestionGateway questions) {
        JPanel root = new JPanel(new BorderLayout());
        showGame(root, saveFile, questions, "title");
        return root;
    }

    private static void showGame(JPanel root, Path saveFile, QuestionGateway questions, String firstPage) {
        // New Game starts fresh in memory; the checkpoint stays unchanged.
        FileGameStateDataAccessObject gameState = new FileGameStateDataAccessObject(
                saveFile, new GameDay(1, START_DATE), new Player(MAX_ENERGY, MAX_ENERGY));
        OpenMeteoWeatherDataAccessObject weather = new OpenMeteoWeatherDataAccessObject(
                OpenMeteoWeatherDataAccessObject.DEFAULT_BASE_URL, new BundledWeatherDataAccessObject());
        ViewManagerModel navigation = new ViewManagerModel();
        RestViewModel residenceModel = new RestViewModel();
        TitleViewModel titleModel = new TitleViewModel();
        // Building callbacks return to their own door; New Game starts outside Residence.
        MapViewModel mapModel = new MapViewModel();
        CampusMap campusMap = CampusMapResourceLoader.load(CampusMapResourceLoader.DEFAULT_MAP);
        Walker walker = new Walker(0, 0);
        walker.placeBelow(campusMap.doorOf(Building.RESIDENCE));
        MapController map = new MapController(campusMap, walker, mapModel, navigation);

        RestController rest = new RestController(
                new RestInteractor(gameState, weather, new RestPresenter(residenceModel)));
        ContinueGameController continueGame = new ContinueGameController(new ContinueGameInteractor(
                gameState, new ContinueGamePresenter(titleModel, residenceModel, navigation)));

        TitleView title = new TitleView(titleModel, continueGame,
                () -> showGame(root, saveFile, questions, "street"));
        ResidenceView residence = new ResidenceView(residenceModel, rest,
                () -> map.returnTo(Building.RESIDENCE));
        ChooseMajorViewModel majorModel = new ChooseMajorViewModel();
        ChooseMajorController chooseMajor = new ChooseMajorController(new ChooseMajorInteractor(
                InMemoryMajorCatalog.predefined(), gameState, new ChooseMajorPresenter(majorModel)));
        ChooseMajorView registrar = new ChooseMajorView(chooseMajor, majorModel,
                () -> map.returnTo(Building.REGISTRAR));
        chooseMajor.loadMajors();
        ClassChallengeView lecture = ClassChallengeFactory.create(gameState, questions,
                () -> map.returnTo(Building.LECTURE_HALL));
        ExamViewModel examModel = new ExamViewModel();
        ExamView exam = new ExamView(new ExamController(new ExamInteractor(gameState, questions,
                new ExamPresenter(examModel))), examModel, () -> map.returnTo(Building.EXAM_CENTRE));
        MapView street = new MapView(mapModel, map, () -> navigation.setActiveView("title"));
        GameView game = new GameView(title, residence, navigation, registrar, lecture, exam, street);
        navigation.addPropertyChangeListener(event -> {
            switch (navigation.getActiveView()) {
                case "registrar": chooseMajor.loadMajors(); break;
                case "lecture": lecture.refresh(); break;
                case "exam": exam.refresh(); break;
                case "residence": refreshResidence(gameState, residenceModel); break;
                default: break;
            }
        });
        residenceModel.setState(ResidenceFormat.day(1, START_DATE), ResidenceFormat.noWeather(),
                ResidenceFormat.energy(MAX_ENERGY, MAX_ENERGY));
        navigation.setActiveView(firstPage);

        root.removeAll();
        root.add(game, BorderLayout.CENTER);
        root.revalidate();
        root.repaint();
    }

    private static void refreshResidence(FileGameStateDataAccessObject state, RestViewModel model) {
        boolean retry = model.isRetryAvailable();
        String status = model.getSaveText();
        model.setState(ResidenceFormat.day(state.getCurrentDay().getDayNumber(), state.getCurrentDay().getRealDate()),
                state.getCurrentWeather() == null ? ResidenceFormat.noWeather()
                        : ResidenceFormat.weather(state.getCurrentWeather().getCondition(),
                                state.getCurrentWeather().getHighTemperature(), state.getCurrentWeather().getLowTemperature()),
                ResidenceFormat.energy(state.getPlayer().getEnergy(), state.getPlayer().getMaxEnergy()));
        if (retry) { model.setSaveStatus(status, true); }
    }
}
