package simulation.Layout;

import simulation.core.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Layout extends JFrame {

    private Timer timer;
    private Grid canvas;

    private Simulation simulation;
    private static final int CANVAS_WIDTH = 600;
    private static final int CANVAS_HEIGHT = 400;

    private static final int DELAY = 100; // Milliseconds

    public Layout(Simulation simulation) {
        this.simulation = simulation;

        setTitle("Lotka-Volterra App");
        setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.canvas = new Grid(this);
        simulation.addCallback(() -> {
            this.canvas.repaint();
        });

        add(this.canvas, BorderLayout.CENTER);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSimulation();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        add(buttonPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }

    public Simulation getSimulation() {
        return this.simulation;
    }

    private void startSimulation() {

    }


    private void executeCallbacks() {
        this.canvas.repaint();
    }

    private void stopSimulation() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
