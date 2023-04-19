package simulation.core;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import simulation.core.config.SimulationConfig;
import simulation.core.config.SpeciesContext;

public class Board {

    SpeciesOnField[][] speziesBoard;

    SimulationConfig context;

    private final Random random = new Random();

    public Board(SimulationConfig simulationConfig) {
        int count = simulationConfig.width * simulationConfig.height;

        ArrayList<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, count).boxed().toList());
        Collections.shuffle(range);
        List<Integer> filledFields = range.subList(0, ((simulationConfig.width * simulationConfig.height) / 100)
                * simulationConfig.filledFields);

        this.speziesBoard = new SpeciesOnField[simulationConfig.width][simulationConfig.height];
        this.context = simulationConfig;

        for (int row = 0; row < simulationConfig.width; row++) {
            for (int col = 0; col < simulationConfig.height; col++) {
                if (filledFields.contains(row * col)) {
                    this.speziesBoard[row][col] = this.chooseRandomSpecies(context.species);
                }
            }
        }
    }

    public void run(SimulationConfig simulationConfig, Function<Integer, Boolean> callback) {
        for (int i = 0; i < simulationConfig.maxIterations; i++) {
            System.out.println(i);
            for (int index = 0; index < simulationConfig.width * simulationConfig.height; index++) {
                    int randomColumn = random.nextInt(simulationConfig.width);
                    int randomRow = random.nextInt(simulationConfig.height);

                    this.action(randomColumn, randomRow, simulationConfig);
            }

            callback.apply(i);
        }
    }

    public void action(int x, int y, SimulationConfig simulationConfig) {
        int all = simulationConfig.probabilityOfReproduction
                + simulationConfig.probabilityOfSelection
                + simulationConfig.probabilityOfMovement;


        var randomValue = RandomWrapper.getRandom().nextInt(all);

        randomValue -= simulationConfig.probabilityOfReproduction;
        if (randomValue < 0) {
            this.reproduction(x, y, simulationConfig);
            return;
        }

        randomValue -= simulationConfig.probabilityOfSelection;
        if (randomValue < 0) {
            this.selection(x, y, simulationConfig);
            return;
        }

        this.move(x, y, simulationConfig);
    }

    public void reproduction(int x, int y, SimulationConfig simulationConfig) {
        Direction choosenDirection = Direction.randomLetter();
        if (hasSpeciesAtCell(x, y)) {
            SpeciesOnField cellSpeciesOnField = this.getSpeciesAtCell(x, y);

            this.executeActionAt(x, y, simulationConfig, choosenDirection, (xOther, yOther) -> {
                if (!this.hasSpeciesAtCell(xOther, yOther)) {
                    this.speziesBoard[xOther][yOther] = new SpeciesOnField(cellSpeciesOnField.context);
                }

                return true;
            });
        } else  {
            this.executeActionAt(x, y, simulationConfig, choosenDirection, (xOther, yOther) -> {
                if (this.hasSpeciesAtCell(xOther, yOther)) {
                    SpeciesOnField argumentSpeciesOnField = this.getSpeciesAtCell(xOther, yOther);
                    this.speziesBoard[x][y] = new SpeciesOnField(argumentSpeciesOnField.context);
                }

                return true;
            });
        }
    }

    public void selection(int x, int y, SimulationConfig simulationConfig) {
        Direction choosenDirection = Direction.randomLetter();

        if (!hasSpeciesAtCell(x, y)) {
            return;
        }

        SpeciesOnField cellSpeciesOnField = this.getSpeciesAtCell(x, y);

        this.executeActionAt(x, y, simulationConfig, choosenDirection, (xOther, yOther) -> {
            if (!this.hasSpeciesAtCell(xOther, yOther)) {
                return true;
            }

            SpeciesOnField otherSpeciesOnField = this.getSpeciesAtCell(xOther, yOther);

            if (otherSpeciesOnField.context.isEating(cellSpeciesOnField.context)) {
                this.speziesBoard[x][y] = null;
            }

            if (cellSpeciesOnField.context.isEating(otherSpeciesOnField.context)) {
                this.speziesBoard[xOther][yOther] = null;
            }

            return true;
        });
    }

    public void move(int x, int y, SimulationConfig simulationConfig) {
        Direction choosenDirection = Direction.randomLetter();

        this.executeActionAt(x, y, simulationConfig, choosenDirection, (xOther, yOther) -> {
            var content = this.speziesBoard[xOther][yOther];
            this.speziesBoard[xOther][yOther] = this.speziesBoard[x][y];
            this.speziesBoard[x][y] = content;
            return true;
        });
    }

    private void executeActionAt(int x, int y, SimulationConfig simulationConfig, Direction direction, BiFunction<Integer, Integer, Boolean> callback) {
        try {
            switch (direction) {
                case RIGHT -> {
                    if ((x + 1) >= simulationConfig.width) {
                        throw new RuntimeException("Invalid width");
                    }
                    callback.apply(x + 1, y);
                }
                case LEFT -> {
                    if ((x - 1) < 0) {
                        throw new RuntimeException("Invalid width");
                    }
                    callback.apply(x - 1, y);
                }
                case TOP -> {
                    if ((y - 1) < 0) {
                        throw new RuntimeException("Invalid height");
                    }
                    callback.apply(x, y - 1);
                }
                case DOWN -> {
                    if ((y + 1) >= simulationConfig.height) {
                        throw new RuntimeException("Invalid height");
                    }
                    callback.apply(x, y + 1);
                }
                default -> throw new IllegalArgumentException();
            }
        } catch (RuntimeException ignored) {
        }
    }

    public SpeciesOnField chooseRandomSpecies(SpeciesContext[] speciesContexts) {
        int rand = this.random.nextInt(speciesContexts.length);
        return new SpeciesOnField(speciesContexts[rand]);
    }

    public boolean hasSpeciesAtCell(int xAches, int yAches) {
        return this.speziesBoard[xAches][yAches] != null;
    }

    public SpeciesOnField getSpeciesAtCell(int xAches, int yAches) {
        return this.speziesBoard[xAches][yAches];
    }
}
