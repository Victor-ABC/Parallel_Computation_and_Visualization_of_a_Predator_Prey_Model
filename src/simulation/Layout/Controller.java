package simulation.Layout;

import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import simulation.core.Board;
import simulation.core.SimulationContext;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    public CategoryAxis xAxis;
    public NumberAxis yAxis;

    public Canvas centerCanvas;

    public HBox centerContent;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAxis.setLabel("Zeitpunkt");
        yAxis.setLabel("Anzahl");
    }

    public void printGame(SimulationContext context, Board board) {
        this.centerCanvas.setHeight(context.height);
        this.centerCanvas.setWidth(context.width);
        this.centerContent.setOnScroll((ScrollEvent event) -> {
            System.out.println(event.getDeltaY());
            centerCanvas.setScaleX(centerCanvas.getScaleX() + (event.getDeltaY()  * 0.02));
            centerCanvas.setScaleY(centerCanvas.getScaleY() + (event.getDeltaY() * 0.02));
        });
        this.centerCanvas.setScaleX(2);
        this.centerCanvas.setScaleY(2);
        GraphicsContext gc = centerCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        System.out.println(context.height);
        System.out.println(context.width);
        this.repaint(context, board, gc);

    }

    public void startOrStop() {

    }

    private void repaint(SimulationContext context, Board board, GraphicsContext gc) {
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
