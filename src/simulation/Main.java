package simulation;

import simulation.Layout.Controller;
import simulation.core.*;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import simulation.core.config.SimulationConfig;


public class Main extends Application {

    public static SimulationConfig simulationConfig;

    public static void main(String[] args) {
        simulationConfig = (new SimulationContextJsonFactory()).CreateSimulationContextFromJsonFile("config.json");
        RandomWrapper.createRandom(simulationConfig.seed);
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Board board = new Board(simulationConfig);

        FXMLLoader  loader   = new FXMLLoader(getClass().getResource("Layout/view.fxml"));
        Parent root = loader.load();

        Controller myController = loader.getController();
        primaryStage.setTitle("PVR");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(750);
        primaryStage.show();
        myController.printGame(simulationConfig, board);
    }

}