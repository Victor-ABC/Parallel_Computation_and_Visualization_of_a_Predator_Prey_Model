package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.Layout.Controller;
import main.core.Board;
import main.core.BoardParallel;
import main.core.BoardParallelZones;
import main.core.config.Config;


public class Main extends Application {

    public static Config config;
    public static final String PATH_TO_CONFIG = "config.json";

    public static void main(String[] args) {
        config = loadConfig(PATH_TO_CONFIG);
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        config.mode = "parallel_with_zones";
        Board board = getBoard();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Layout/view.fxml"));
        Parent root = loader.load();

        Controller myController = loader.getController();
        primaryStage.setTitle("PVR");
        primaryStage.setScene(new Scene(root, 1600, 900));
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(750);
        primaryStage.show();
        myController.printGame(config, board);
    }

    private Board getBoard() {
        return switch (config.mode) {
            case "sequential" -> new Board(config);
            case "parallel_without_zones" -> new BoardParallel(config);
            case "parallel_with_zones" -> new BoardParallelZones(config);
            default -> throw new IllegalArgumentException("Board-Type is not supported!");
        };
    }

    private static Config loadConfig(String path) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File(path), Config.class);
        } catch (Exception e) {
            throw new RuntimeException("Json file could not be parsed: " + e);
        }
    }

}