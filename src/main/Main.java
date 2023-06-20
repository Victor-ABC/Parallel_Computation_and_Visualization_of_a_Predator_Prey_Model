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
import main.core.config.SimulationConfig;


public class Main extends Application {

    public static SimulationConfig simulationConfig;

    public static void main(String[] args) {
        simulationConfig = loadSimulationContext("config.json");
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Board board = getBoard();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Layout/view.fxml"));
        Parent root = loader.load();

        Controller myController = loader.getController();
        primaryStage.setTitle("PVR");
        primaryStage.setScene(new Scene(root, 1600, 900));
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(750);
        primaryStage.show();
        myController.printGame(simulationConfig, board);
    }

    private Board getBoard() {
        return switch (simulationConfig.mode) {
            case "sequential" -> new Board(simulationConfig);
            case "parallel_without_zones" -> new BoardParallel(simulationConfig);
            case "parallel_with_zones" -> new BoardParallelZones(simulationConfig);
            default -> throw new IllegalArgumentException("Board-Type is not supported!");
        };
    }

    private static SimulationConfig loadSimulationContext(String path) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File(path), SimulationConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Json file could not be parsed: " + e);
        }
    }

}