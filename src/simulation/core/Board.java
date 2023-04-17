package simulation.core;

import java.util.Random;

public class Board {

    Spezies[][] spezies;

    SimulationContext context;

    private Random random = new Random();

    public Board(SimulationContext simulationContext) {
        this.spezies = new Spezies[simulationContext.xAches][simulationContext.yAchses];
        this.context = simulationContext;

        for (int row = 0; row < simulationContext.xAches; row++) {
            for (int col = 0; col < simulationContext.yAchses; col++) {
                this.spezies[row][col] = this.getSpezies(context.spezies, col, row);
            }
        }
    }

    public Spezies getSpezies(SpeziesContext[] speziesContexts, int xAches, int yAches) {
        int rand = this.random.nextInt(speziesContexts.length);
        return new Spezies(speziesContexts[rand], xAches, yAches);
    }

    public boolean hasSpeziesAtCell(int xAches, int yAches) {
        return this.spezies[xAches][yAches] instanceof Spezies;
    }


    public Spezies getSpeziesAtCell(int xAches, int yAches) {
        return this.spezies[xAches][yAches];
    }

    public void unsetCell(int xAches, int yAches) {
        this.spezies[xAches][yAches] = null;
    }
}
