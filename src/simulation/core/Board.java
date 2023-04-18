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
//
//    public boolean canBreed(Species species) {
//
//    }
//
//    public boolean isStarving(Species species) {
//
//    }
//
//    public boolean canEar(Species species) {
//
//    }

    public void run(SimulationContext simulationContext) {
        for (var i = 0; i < simulationContext.height * simulationContext.width; i++) {
            this.iterationPhase(simulationContext);
        }

        for (int row = 0; row < simulationContext.width; row++) {
            for (int col = 0; col < simulationContext.height; col++) {
                this.developmentPhase(col, row);
            }
        }
    }

    public void developmentPhase(int x, int y) {
        if (!hasSpeciesAtCell(x, y)) {
            return;
        }

        Species cellSpecies = this.getSpeciesAtCell(x, y);

        if (cellSpecies.isLowerThanBreed()) {
            cellSpecies.power = +1;
            return;
        }

        if (cellSpecies.shouldBreed()) {
            return;
        }
    }

    public void iterationPhase(SimulationContext simulationContext) {
        int x = RandomWrapper.getRandom().nextInt(simulationContext.width);
        int y = RandomWrapper.getRandom().nextInt(simulationContext.height);

        if (!hasSpeciesAtCell(x, y)) {
            return;
        }

        Species cellSpecies = this.getSpeciesAtCell(x, y);

        if (cellSpecies.shouldMove()) {
            this.executeAtEmpty(x, y, simulationContext, cellSpecies, (xArgument, yArgumentc, simulationContextArgument, direction, argumentCellSpecies) -> {
                this.swapWithDirection(xArgument, yArgumentc, simulationContextArgument, direction, argumentCellSpecies);
            });
            return;
        }

        if (cellSpecies.shouldBreed()) {
            this.executeAtEmpty(x, y, simulationContext, cellSpecies, (xArgument, yArgumentc, simulationContextArgument, direction, argumentCellSpecies) -> {
                this.breedAtDirection(xArgument, yArgumentc, simulationContextArgument, direction, argumentCellSpecies);
            });
            return;
        }

        if (this.tryToEat(x, y, simulationContext, cellSpecies)) {
            return;
        }
        ;

        this.executeAtEmpty(x, y, simulationContext, cellSpecies, (xArgument, yArgumentc, simulationContextArgument, direction, argumentCellSpecies) -> {
            this.swapWithDirection(xArgument, yArgumentc, simulationContextArgument, direction, argumentCellSpecies);
        });
    }

    private boolean tryToEat(int x, int y, SimulationContext simulationContext, Species cellSpecies) {
        for (Direction direction : Direction.getRandomDirections()) {
            boolean isEmpty = !this.hasSpeciesForDirection(x, y, simulationContext, direction);
            if (isEmpty) {
                continue;
            }
            if (cellSpecies.context.isEating(this.getSpeciesForDirection(x, y, simulationContext, direction).context)) {
                this.eatDirection(x, y, simulationContext, direction, cellSpecies);
                return true;
            }
        }


        return false;
    }

    private void executeAtEmpty(int x, int y, SimulationContext simulationContext, Species cellSpecies, FiFunction<Integer, Integer, SimulationContext, Direction, Species> callback) {
        for (Direction direction : Direction.getRandomDirections()) {
            boolean isEmpty = !this.hasSpeciesForDirection(x, y, simulationContext, direction);
            if (isEmpty) {
                callback.apply(x, y, simulationContext, direction, cellSpecies);
                return;
            }
        }
    }

    private void swapWithDirection(int x, int y, SimulationContext simulationContext, Direction direction, Species cellSpecies) {
        this.<Boolean>executeActionAt(x, y, simulationContext, direction, (xArguemnt, yArgument) -> {
            this.spezies[xArguemnt][yArgument] = cellSpecies;
            return true;
        });
    }

    private void eatDirection(int x, int y, SimulationContext simulationContext, Direction direction, Species cellSpecies) {
        Species eaten = this.<Species>executeActionAt(x, y, simulationContext, direction, (xArguemnt, yArgument) -> {
            Species innerEaten = this.spezies[xArguemnt][yArgument];
            this.spezies[xArguemnt][yArgument] = null;
            return innerEaten;
        });

        cellSpecies.power = +eaten.power;
    }

    private void breedAtDirection(int x, int y, SimulationContext simulationContext, Direction direction, Species cellSpecies) {
        this.<Boolean>executeActionAt(x, y, simulationContext, direction, (xArguemnt, yArgument) -> {
            this.spezies[xArguemnt + 1][yArgument] = new Species(cellSpecies.context, xArguemnt, yArgument);
            return true;
        });
    }

    private boolean hasSpeciesForDirection(int x, int y, SimulationContext simulationContext, Direction direction) {
        return this.<Boolean>executeActionAt(x, y, simulationContext, direction, (xArguemnt, yArgument) -> {
            return this.hasSpeciesAtCell(xArguemnt, yArgument);
        });
    }

    private Species getSpeciesForDirection(int x, int y, SimulationContext simulationContext, Direction direction) {
        return this.<Species>executeActionAt(x, y, simulationContext, direction, (xArguemnt, yArgument) -> {
            return this.getSpeciesAtCell(xArguemnt, yArgument);
        });
    }

    private <T> T executeActionAt(int x, int y, SimulationContext simulationContext, Direction direction, BiFunction<Integer, Integer, T> callback) {
        switch (direction) {
            case RIGHT:
                if ((x + 1) > simulationContext.width) {
                    throw new RuntimeException("Invalid width");
                }

                return (T) callback.<T>apply(x + 1, y);
            case LEFT:
                if ((x - 1) < 0) {
                    throw new RuntimeException("Invalid width");
                }


                return (T) callback.<T>apply(x - 1, y);
            case DOWN:
                if ((y - 1) > 0) {
                    throw new RuntimeException("Invalid height");
                }

                return (T) callback.<T>apply(x, y - 1);

            case TOP:
                if ((y + 1) > simulationContext.height) {
                    throw new RuntimeException("Invalid height");
                }


                return (T) callback.<T>apply(x, y + 1);

            default:
                throw new IllegalArgumentException();
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

    @FunctionalInterface
    public interface FiFunction<A, B, C, D, E> {

        /**
         * Applies this function to the given arguments.
         *
         * @param t the first function argument
         * @param u the second function argument
         * @return the function result
         */
        void apply(A a, B b, C c, D d, E e);
    }

}
