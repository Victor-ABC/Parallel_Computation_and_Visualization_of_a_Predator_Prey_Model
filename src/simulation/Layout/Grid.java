package simulation.Layout;

import simulation.core.Simulation;

import javax.swing.*;
import java.awt.*;

public class Grid extends JComponent {

    private Simulation simulation = null;

    public Grid(Layout layout) {
        this.simulation = layout.getSimulation();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawPopulations(g);
    }

    private void drawPopulations(Graphics g) {
        int preySize = simulation.preyPopulation / 10;
        int predatorSize = simulation.predatorPopulation / 10;
        g.setColor(Color.GREEN);
        g.fillOval(50, 200 - preySize, preySize, preySize);
        g.setColor(Color.RED);
        g.fillOval(300, 200 - predatorSize, predatorSize, predatorSize);
        g.setColor(Color.BLACK);
        g.drawString("Prey Population: " + simulation.preyPopulation, 50, 250);
        g.drawString("Predator Population: " + simulation.predatorPopulation, 300, 250);
    }
}


