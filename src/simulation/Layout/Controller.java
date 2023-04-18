package simulation.Layout;

import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
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

    private Random random = new Random();

    private ArrayList<Color> colorCache = new ArrayList<Color>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAxis.setLabel("Zeitpunkt");
        yAxis.setLabel("Anzahl");
    }

    public void printGame(SimulationContext context, Board board) {
        this.centerCanvas.setHeight(context.height);
        this.centerCanvas.setWidth(context.width);
        GraphicsContext gc = centerCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        System.out.println(context.height);
        System.out.println(context.width);
        for (int row = 0; row < context.width; row++) {
            for (int column = 0; column < context.height; column++) {
                    gc.getPixelWriter().setColor(column, row, Color.web(board.getSpeziesAtCell(column, row).context.color));
            }
        }
    }
}
