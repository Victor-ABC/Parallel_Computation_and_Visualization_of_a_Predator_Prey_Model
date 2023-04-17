package simulation.core;

import com.beust.jcommander.Parameter;

public class SimulationContext {

    public int initialPredatorPopulation = 100;

    public int initialPreyPopulation = 100;

    public double initialPreyGrowthRate = 0.02;

    public double initialPreyDecayRate = 0.01;

    public double initialPreyMoveRate = 0.01;

    public double initialPredatorGrowthRate = 0.01;
    public double initialPredatorDecayRate = 0.03;
    public double initialPredatorMoveRate = 0.01;

    public int speziesCount = 3;

    public int iterationCount = 100;

    public int xAches = 100;
    public int yAchses = 100;

    public SpeziesContext[] spezies = new SpeziesContext[] {
            new SpeziesContext(0, "rabbit", 0.01, 0.01, 0.01),
            new SpeziesContext(1, "dog", 0.01, 0.01, 0.01),
            new SpeziesContext(2, "fox", 0.01, 0.01, 0.01)
    };
}
