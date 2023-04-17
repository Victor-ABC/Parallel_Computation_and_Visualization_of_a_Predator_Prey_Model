package simulation.Layout;

import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import simulation.core.Board;
import simulation.core.SimulationContext;
import simulation.core.SpeziesContext;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
        this.centerCanvas.isResizable()
        this.centerCanvas.setHeight(context.xAches);
        this.centerCanvas.setWidth(context.yAchses);
        GraphicsContext gc = centerCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        System.out.println(context.xAches);
        System.out.println(context.yAchses);
        for (int row = 0; row < context.xAches; row++) {
            for (int column = 0; column < context.yAchses; column++) {
                    gc.getPixelWriter().setColor(column, row, this.getColorForIdentifier(board.getSpeziesAtCell(column, row).context.id));
            }
        }
    }

    private Color getColorForIdentifier(int id) {
        try {
            return this.colorCache.get(id);
        } catch (IndexOutOfBoundsException $e) {
            Color newColor = new Color(this.random.nextFloat(1), this.random.nextFloat(1), this.random.nextFloat(1), 1);
            this.colorCache.add(newColor);
            return newColor;
        }
    }
}
