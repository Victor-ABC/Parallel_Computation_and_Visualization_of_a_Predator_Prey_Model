package simulation.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Board {

    Species[][] spezies;

    SimulationContext context;

    private Random random = new Random();

    public Board(SimulationContext simulationContext) {
        int count = simulationContext.width * simulationContext.height;

        var range = new ArrayList<>(IntStream.rangeClosed(0, count).boxed().toList());
        Collections.shuffle(range);
        var filledFields = range.subList(0,  ((int) ((simulationContext.width * simulationContext.height) / 100)) * simulationContext.filledFields);

        this.spezies = new Species[simulationContext.width][simulationContext.height];
        this.context = simulationContext;

        for (int row = 0; row < simulationContext.width; row++) {
            for (int col = 0; col < simulationContext.height; col++) {
                if(filledFields.contains(row * col)) {
                    this.spezies[row][col] = this.getSpezies(context.spezies, col, row);
                }
            }
        }
    }

    public Species getSpezies(SpeciesContext[] speciesContexts, int xAches, int yAches) {
        int rand = this.random.nextInt(speciesContexts.length);
        return new Species(speciesContexts[rand], xAches, yAches);
    }

    public boolean hasSpeziesAtCell(int xAches, int yAches) {
        return this.spezies[xAches][yAches] instanceof Species;
    }


    public Species getSpeziesAtCell(int xAches, int yAches) {
        return this.spezies[xAches][yAches];
    }

    public void unsetCell(int xAches, int yAches) {
        this.spezies[xAches][yAches] = null;
    }
}
