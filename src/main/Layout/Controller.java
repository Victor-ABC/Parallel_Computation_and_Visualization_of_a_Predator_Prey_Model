package main.Layout;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import main.core.Board;
import main.core.config.SimulationConfig;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    public CategoryAxis xAxis;
    public NumberAxis yAxis;

    public Canvas centerCanvas;

    public HBox centerContent;

    public Board board;
    private SimulationConfig context;

    private GraphicsContext gc;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAxis.setLabel("Zeitpunkt");
        yAxis.setLabel("Anzahl");
    }

    public void printGame(SimulationConfig context, Board board) {
        this.context = context;
        this.centerCanvas.setHeight(context.height);
        this.centerCanvas.setWidth(context.width);
        this.centerContent.setOnScroll((ScrollEvent event) -> {
            System.out.println(event.getDeltaY());
            centerCanvas.setScaleX(centerCanvas.getScaleX() + (event.getDeltaY()  * 0.02));
            centerCanvas.setScaleY(centerCanvas.getScaleY() + (event.getDeltaY() * 0.02));
        });
        this.centerCanvas.setScaleX(2);
        this.centerCanvas.setScaleY(2);
        this.gc = centerCanvas.getGraphicsContext2D();
        this.gc.setFill(Color.WHITE);
        System.out.println(context.height);
        this.board = board;
        this.createCanvas(context, board, this.gc);
    }

    public void startOrStop() {
        new Thread(() -> {
            this.board.run(this.context, (i) -> {
                Platform.runLater(() -> {
                    this.createCanvas(this.context, this.board, this.gc);
                });

                return true;
            });
        }).start();
    }

    private void createCanvas(SimulationConfig context, Board board, GraphicsContext gc) {
        for (int row = 0; row < context.width; row++) {
            for (int column = 0; column < context.height; column++) {
                gc.getPixelWriter().setColor(column, row, Color.web(this.getColor(column,  row, board)));
            }
        }
    }

    private String getColor(int column, int row, Board board) {
        if(!board.hasSpeciesAtCell(column, row))  {
            return "#fff";
        }

        return board.getSpeciesAtCell(column, row).context.color;
    }
}
