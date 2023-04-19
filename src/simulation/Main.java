package simulation;

import com.beust.jcommander.JCommander;
import simulation.Layout.Controller;
import simulation.core.*;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    public static SimulationContext simulationContext;

    public static void main(String[] args) {
        simulationContext = (new SimulationContextJsonFactory()).CreateSimulationContextFromJsonFile("config.json");
        RandomWrapper.createRandom(simulationContext.seed);
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Board board = new Board(simulationContext);

        FXMLLoader  loader   = new FXMLLoader(getClass().getResource("Layout/view.fxml"));
        Parent root = loader.load();

        Controller myController = loader.getController();
        primaryStage.setTitle("PVR");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(750);
        primaryStage.show();
        myController.printGame(simulationContext, board);
    }

}