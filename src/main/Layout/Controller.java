package main.Layout;

import javafx.animation.AnimationTimer;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import main.core.Board;
import main.core.config.SimulationConfig;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import main.core.config.SpeciesContext;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    public NumberAxis xAxis;
    public NumberAxis yAxis;

    public LineChart lineChart;

    public HashMap<String, Series> series = new HashMap<>();

    public Canvas centerCanvas;

    public HBox centerContent;

    public Board board;

    private GraphicsContext gc;

    private Boolean isStarted = false;

    private AnimationTimer animationTimer;

    private Integer tick = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAxis.setLabel("Zeitpunkt");
        yAxis.setLabel("Anzahl");
    }

    public void printGame(SimulationConfig context, Board board) {
        this.centerCanvas.setHeight(context.height);
        this.centerCanvas.setWidth(context.width);
        this.centerContent.setOnScroll((ScrollEvent event) -> {
            centerCanvas.setScaleX(centerCanvas.getScaleX() + (event.getDeltaY()  * 0.02));
            centerCanvas.setScaleY(centerCanvas.getScaleY() + (event.getDeltaY() * 0.02));
        });
        this.centerCanvas.setScaleX(2);
        this.centerCanvas.setScaleY(2);
        this.gc = centerCanvas.getGraphicsContext2D();
        this.gc.setFill(Color.WHITE);
        this.board = board;
        for (SpeciesContext species : context.species) {
            var dataSeries = new Series();
            dataSeries.setName(species.getName());

            this.series.put(species.getName(), dataSeries);
        }

        this.lineChart.getData().addAll(this.series.values());

        this.createCanvas(context, board, this.gc);

        this.animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                createCanvas(context, board, gc);
            }
        };
        this.animationTimer.start();
    }

    public void startOrStop() {
        if(this.isStarted) {
            return;
        }

        this.isStarted = true;

            this.board.run((i) -> {
                return true;
            });
    }

    private void createCanvas(SimulationConfig context, Board board, GraphicsContext gc) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        for (SpeciesContext species : context.species) {
            hashMap.put(species.getName(), 0);
        }
        for (int row = 0; row < context.width; row++) {
            for (int column = 0; column < context.height; column++) {
                if(board.hasSpeciesAtCell(column, row)) {
                    var species = board.getSpeciesAtCell(column, row).context.getName();
                    hashMap.put(species, hashMap.get(species) + 1);
                }
                gc.getPixelWriter().setColor(column, row, Color.web(this.getColor(column,  row, board)));
            }
        }

        if(tick % 20 == 0 && this.isStarted) {
            hashMap.forEach((s, integer) -> {
                this.series.get(s).getData().add(new Data<Integer, Integer>(this.tick, hashMap.get(s)));
            });
        }

        if(this.isStarted) {
            this.tick++;
        }
    }

    private String getColor(int column, int row, Board board) {
        if(!board.hasSpeciesAtCell(column, row))  {
            return "#fff";
        }

        return board.getSpeciesAtCell(column, row).context.color;
    }
}
