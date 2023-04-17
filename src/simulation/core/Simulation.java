package simulation.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Simulation extends JFrame {
//    private static final double ALPHA = 0.1;  // Räuber-Wachstumsrate
//    private static final double BETA = 0.02;  // Beute-Abnahme-Rate
//    private static final double GAMMA = 0.3;  // Räuber-Abnahme-Rate
//    private static final double DELTA = 0.01;  // Beute-Wachstumsrate
//    private static final int NUM_STEPS = 100;  // Anzahl der Simulationsschritte
//
//    public void start() {
//        double preyPopulation = 100;  // Anfangsbevölkerung der Beute
//        double predatorPopulation = 20;  // Anfangsbevölkerung der Räuber
//
//        System.out.println("Start der Lotka-Volterra-Simulation");
//        System.out.println("Anfangsbevölkerung der Beute: " + preyPopulation);
//        System.out.println("Anfangsbevölkerung der Räuber: " + predatorPopulation);
//
//        for (int i = 1; i <= NUM_STEPS; i++) {
//            // Berechnung der Veränderungen der Bevölkerungszahlen
//            double preyChange = (ALPHA * preyPopulation) - (BETA * preyPopulation * predatorPopulation);
//            double predatorChange = (DELTA * preyPopulation * predatorPopulation) - (GAMMA * predatorPopulation);
//
//            // Aktualisierung der Bevölkerungszahlen
//            preyPopulation += preyChange;
//            predatorPopulation += predatorChange;
//
//            // Ausgabe der aktuellen Bevölkerungszahlen
//            System.out.println("Schritt " + i + ": Beute = " + preyPopulation + ", Räuber = " + predatorPopulation);
//        }
//
//        System.out.println("Ende der Lotka-Volterra-Simulation");
//    }

    public int preyPopulation;
    public int predatorPopulation;

    private Runnable callback;

    private double preyGrowthRate;

    private double preyDecayRate;

    private double predatorGrowthRate;

    private double predatorDecayRate;

    private SimulationContext context;

    public Simulation(SimulationContext context) {
        this.context = context;

    }

    public void run() {
        this.preyPopulation = this.context.initialPreyPopulation;
        this.predatorPopulation = this.context.initialPredatorPopulation;
        this.preyGrowthRate = this.context.initialPreyGrowthRate;
        this.preyDecayRate = this.context.initialPreyDecayRate;
        this.predatorGrowthRate = this.context.initialPredatorGrowthRate;
        this.predatorDecayRate = this.context.initialPredatorDecayRate;

        while(preyPopulation > 0 && predatorPopulation> 0) {
            this.populate();
            if (this.callback instanceof Runnable) {
                this.callback.run();
            }
        }
    }

    public void addCallback(Runnable callback) {
        this.callback = callback;
    }

    private void populate() {
        int preyChange = (int) ((this.preyGrowthRate * this.preyPopulation) - (this.preyDecayRate * this.preyPopulation * this.predatorPopulation));
        int predatorChange = (int) ((this.predatorGrowthRate * this.preyPopulation * this.predatorPopulation) - (this.predatorDecayRate * this.predatorPopulation));
        this.preyPopulation += preyChange;
        this.predatorPopulation += predatorChange;
    }
}
