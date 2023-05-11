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

    private HashMap<String, Color> colors = new HashMap<String, Color>();

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

        this.createCanvas(context, board, this.gc, 0);

        this.animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                createCanvas(context, board, gc,  now);
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

    private void createCanvas(SimulationConfig context, Board board, GraphicsContext gc, long timeNow) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        for (SpeciesContext species : context.species) {
            hashMap.put(species.getName(), 0);
        }

        for (int row = 0; row < context.width; row++) {
            for (int column = 0; column < context.height; column++) {
                var species = board.getSpeciesAtCell(column, row);

                if (species != null) {
                    String speciesName = species.getName();
                    hashMap.put(speciesName, hashMap.get(speciesName) + 1);
                }
                gc.getPixelWriter().setColor(column, row, this.getColor(column, row, board));
            }
        }
        if (this.isStarted) {
            hashMap.forEach((s, integer) -> {
                this.series.get(s).getData().add(new Data<Integer, Integer>(tick, hashMap.get(s)));
            });
            tick++;
    }
    }

    private Color getColor(int column, int row, Board board) {
       SpeciesContext speciesAtCell =  board.getSpeciesAtCell(column, row);

        if(speciesAtCell == null)  {
            if(this.colors.containsKey("#fff")) {
                return this.colors.get("#fff");
            }

            Color whiteColor = Color.web("#fff");
            this.colors.put("#fff", whiteColor);
            return whiteColor;
        }

        String speciesColorString = speciesAtCell.color;

        if(this.colors.containsKey(speciesColorString)) {
            return this.colors.get(speciesColorString);
        }

        Color speciesColor = Color.web(speciesColorString);
        this.colors.put(speciesColorString, speciesColor);
        return speciesColor;
    }
}
