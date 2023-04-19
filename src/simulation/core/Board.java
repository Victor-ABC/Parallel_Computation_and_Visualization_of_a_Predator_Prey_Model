package simulation.core;

import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Board {

    Species[][] spezies;

    SimulationContext context;

    private Random random = new Random();

    public Board(SimulationContext simulationContext) {
        int count = simulationContext.width * simulationContext.height;

        var range = new ArrayList<>(IntStream.rangeClosed(0, count).boxed().toList());
        Collections.shuffle(range);
        var filledFields = range.subList(0, ((int) ((simulationContext.width * simulationContext.height) / 100)) * simulationContext.filledFields);

        this.spezies = new Species[simulationContext.width][simulationContext.height];
        this.context = simulationContext;

        for (int row = 0; row < simulationContext.width; row++) {
            for (int col = 0; col < simulationContext.height; col++) {
                if (filledFields.contains(row * col)) {
                    this.spezies[row][col] = this.getSpezies(context.spezies, col, row);
                }
            }
        }
    }

    public void run(SimulationContext simulationContext, Function<Integer, Boolean> callback) {
        for (int i = 0; i < simulationContext.maxIterations; i++) {
            System.out.println(i);
        for (int row = 0; row < simulationContext.width; row++) {
            for (int col = 0; col < simulationContext.height; col++) {
                int nextCol = random.nextInt(simulationContext.width);
                int nextRow = random.nextInt(simulationContext.height);

                this.action(nextCol, nextRow, simulationContext);

            }
        }

            callback.apply(i);
        }
    }

    public void action(int x, int y, SimulationContext simulationContext) {
        int all = simulationContext.probabilityOfReproduction
                + simulationContext.probabilityOfSelection
                + simulationContext.probabilityOfMovement;


        var randomValue = RandomWrapper.getRandom().nextInt(all);

        randomValue -= simulationContext.probabilityOfReproduction;
        if (randomValue < 0) {
            this.reproduction(x, y, simulationContext);
            return;
        }

        randomValue -= simulationContext.probabilityOfSelection;
        if (randomValue < 0) {
            this.selection(x, y, simulationContext);
            return;
        }

        this.move(x, y, simulationContext);
    }

    public void reproduction(int x, int y, SimulationContext simulationContext) {
        Direction choosenDirection = Direction.randomLetter();
        if (hasSpeciesAtCell(x, y)) {
            Species cellSpecies = this.getSpeciesAtCell(x, y);

            this.executeActionAt(x, y, simulationContext, choosenDirection, (xArguemnt, yArgument) -> {
                if (!this.hasSpeciesAtCell(xArguemnt, yArgument)) {
                    this.spezies[xArguemnt][yArgument] = new Species(cellSpecies.context, xArguemnt, yArgument);
                }

                return true;
            });
        }

        this.executeActionAt(x, y, simulationContext, choosenDirection, (xArguemnt, yArgument) -> {
            if (this.hasSpeciesAtCell(xArguemnt, yArgument)) {
                Species argumentSpecies = this.getSpeciesAtCell(xArguemnt, yArgument);
                this.spezies[x][y] = new Species(argumentSpecies.context, x, y);
            }

            return true;
        });
    }

    public void selection(int x, int y, SimulationContext simulationContext) {
        Direction choosenDirection = Direction.randomLetter();

        if (!hasSpeciesAtCell(x, y)) {
            return;
        }
        Species cellSpecies = this.getSpeciesAtCell(x, y);

        this.executeActionAt(x, y, simulationContext, choosenDirection, (xArguemnt, yArgument) -> {
            if (!this.hasSpeciesAtCell(xArguemnt, yArgument)) {
                return true;
            }

            Species argumentSpecies = this.getSpeciesAtCell(xArguemnt, yArgument);

            if (argumentSpecies.context.isEating(cellSpecies.context)) {
                this.spezies[x][y] = null;
            }

            if (cellSpecies.context.isEating(argumentSpecies.context)) {
                this.spezies[xArguemnt][yArgument] = null;
            }

            return true;
        });
    }

    public void move(int x, int y, SimulationContext simulationContext) {
        Direction choosenDirection = Direction.randomLetter();

        this.executeActionAt(x, y, simulationContext, choosenDirection, (xArgument, yArgument) -> {
            var content = this.spezies[xArgument][yArgument];
            this.spezies[xArgument][yArgument] = this.spezies[x][y];
            this.spezies[x][y] = content;
            return true;
        });
    }

    private void executeActionAt(int x, int y, SimulationContext simulationContext, Direction direction, BiFunction<Integer, Integer, Boolean> callback) {
        try {
            switch (direction) {
                case RIGHT:
                    if ((x + 1) > simulationContext.width) {
                        throw new RuntimeException("Invalid width");
                    }

                    callback.apply(x + 1, y);
                    return;
                case LEFT:
                    if ((x - 1) < 0) {
                        throw new RuntimeException("Invalid width");
                    }


                    callback.apply(x - 1, y);
                    return;
                case DOWN:
                    if ((y - 1) < 0) {
                        throw new RuntimeException("Invalid height");
                    }

                    callback.apply(x, y - 1);
                    return;

                case TOP:
                    if ((y + 1) > simulationContext.height) {
                        throw new RuntimeException("Invalid height");
                    }


                    callback.apply(x, y + 1);
                    return;

                default:
                    throw new IllegalArgumentException();
            }
        } catch (RuntimeException $e) {

        }

    }

    public Species getSpezies(SpeciesContext[] speciesContexts, int xAches, int yAches) {
        int rand = this.random.nextInt(speciesContexts.length);
        return new Species(speciesContexts[rand], xAches, yAches);
    }

    public boolean hasSpeciesAtCell(int xAches, int yAches) {
        return this.spezies[xAches][yAches] instanceof Species;
    }

    public Species getSpeciesAtCell(int xAches, int yAches) {
        return this.spezies[xAches][yAches];
    }
}
