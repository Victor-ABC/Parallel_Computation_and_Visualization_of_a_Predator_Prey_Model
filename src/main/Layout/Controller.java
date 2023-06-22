package main.Layout;

import javafx.animation.AnimationTimer;
import javafx.beans.InvalidationListener;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import main.core.Board;
import main.core.config.Config;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import main.core.config.Species;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller implements Initializable {

    public NumberAxis xAxis;
    public NumberAxis yAxis;

    public Label iterationCount;

    public LineChart lineChart;

    public HashMap<String, Series> series = new HashMap<>();

    public Canvas centerCanvas;

    public HBox centerContent;

    public Board board;

    private GraphicsContext gc;

    private volatile Boolean isStarted = false;

    private Integer tick = 0;

    private final AtomicInteger iteration = new AtomicInteger(0);

    private final HashMap<String, Color> colors = new HashMap<>();

    private Species[][] speciesOnField;

    private long startTime;

    private Config config;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAxis.setLabel("Zeitpunkt");
        yAxis.setLabel("Verteilung der Spezies");
    }

    public void printGame(Config config, Board board) {
        this.config = config;
        this.centerCanvas.setHeight(config.height);
        this.centerCanvas.setWidth(config.width);
        yAxis.setLowerBound((100 * ((double) 1 / config.species.length)) - 10);
        yAxis.setUpperBound(100 * ((double) 1 / config.species.length));
        yAxis.setTickUnit((double) ((config.height * config.width) / (config.species.length)) / 2);

        this.speciesOnField = new Species[config.width][config.height];
        this.centerContent.setOnScroll((ScrollEvent event) -> {
            centerCanvas.setScaleX(centerCanvas.getScaleX() + (event.getDeltaY()  * 0.02));
            centerCanvas.setScaleY(centerCanvas.getScaleY() + (event.getDeltaY() * 0.02));
        });
        this.centerCanvas.setScaleX(2);
        this.centerCanvas.setScaleY(2);
        this.gc = centerCanvas.getGraphicsContext2D();
        this.gc.setFill(Color.WHITE);
        this.board = board;
        for (Species species : config.species) {
            var dataSeries = new Series();
            dataSeries.setName(species.getName());
            this.series.put(species.getName(), dataSeries);
        }

        this.lineChart.getData().addAll(this.series.values());

        this.createCanvas(config, board, this.gc);
    }

    public void startOrStop() {
        if(this.isStarted) {
            this.isStarted = false;
            return;
        }

        this.isStarted = true;

        this.board.run((i) -> {
            this.iteration.incrementAndGet();
            return true;
        });
        this.startTime = System.currentTimeMillis();
        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                createCanvas(config, board, gc);
            }
        };
        animationTimer.start();
    }

    private void createCanvas(Config config, Board board, GraphicsContext gc) {
        this.iterationCount.setText(Integer.toString(this.iteration.get()));

        HashMap<String, Integer> hashMap = new HashMap<>();

        for (int row = 0; row < config.height; row++) {
            for (int column = 0; column < config.width; column++) {
                var species = board.getSpeciesAtCell(column, row);

                if (species != null) {
                    String speciesName = species.getName();
                    hashMap.merge(speciesName, 1, Integer::sum);
                }

                if(this.speciesOnField[column][row] == species) {
                    continue;
                }

                this.speciesOnField[column][row] = species;
                gc.getPixelWriter().setColor(column, row, this.getColor(species));
            }
        }
        if (this.isStarted) {
            double totalAmountOfFields = config.width * config.height;
            hashMap.forEach((s, integer) -> {
                double speciesCount = hashMap.get(s);
                    this.series.get(s).getData().add(new Data<>(System.currentTimeMillis() - this.startTime, speciesCount / totalAmountOfFields * 100));
                    hashMap.put(s, 0);
            });
            tick++;
        }
    }

    private Color getColor(Species speciesAtCell) {
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
