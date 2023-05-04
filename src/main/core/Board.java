package main.core;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import main.core.config.SimulationConfig;
import main.core.config.SpeciesContext;

public class Board {

    public SpeciesOnField[][] speziesBoard;

    public SimulationConfig simulationConfig;

    public final Random random = new Random();

    public Board(SimulationConfig simulationConfig) {
        int count = simulationConfig.width * simulationConfig.height;

        ArrayList<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, count).boxed().toList());
        Collections.shuffle(range);
        List<Integer> filledFields = range.subList(0, (count / 100) * simulationConfig.filledFields);

        this.speziesBoard = new SpeciesOnField[simulationConfig.width][simulationConfig.height];
        this.simulationConfig = simulationConfig;

        for (int row = 0; row < simulationConfig.width; row++) {
            for (int col = 0; col < simulationConfig.height; col++) {
                if (filledFields.contains(row * col)) {
                    this.speziesBoard[row][col] = this.chooseRandomSpecies(simulationConfig.species);
                }
            }
        }

    }

    public void run(Function<Integer, Boolean> callback) {
        new Thread(() -> {
            for (int i = 0; i < this.simulationConfig.maxIterations; i++) {
                System.out.println(i);
                for (int index = 0; index < this.simulationConfig.width * this.simulationConfig.height; index++) {
                        int randomColumn = random.nextInt(this.simulationConfig.width);
                        int randomRow = random.nextInt(this.simulationConfig.height);
                        Direction choosenDirection = Direction.randomLetter();

                        this.action(randomColumn, randomRow, choosenDirection);
                }

                callback.apply(i);
            }
        }).start();
    }

    public void action(int x, int y, Direction direction) {
        int all = this.simulationConfig.probabilityOfReproduction
                + this.simulationConfig.probabilityOfSelection
                + this.simulationConfig.probabilityOfMovement;

        var randomValue = RandomWrapper.getRandom().nextInt(all);

        randomValue -= simulationConfig.probabilityOfReproduction;
        if (randomValue < 0) {
            this.reproduction(x, y, direction);
            return;
        }

        randomValue -= simulationConfig.probabilityOfSelection;
        if (randomValue < 0) {
            this.selection(x, y, direction);
            return;
        }

        this.move(x, y, direction);
    }

    public void reproduction(int x, int y, Direction choosenDirection) {
        if (hasSpeciesAtCell(x, y)) {
            SpeciesOnField cellSpeciesOnField = this.getSpeciesAtCell(x, y);

            this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
                if (!this.hasSpeciesAtCell(xOther, yOther)) {
                    this.speziesBoard[xOther][yOther] = new SpeciesOnField(cellSpeciesOnField.context);
                }

                return true;
            });
        } else  {
            this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
                if (this.hasSpeciesAtCell(xOther, yOther)) {
                    SpeciesOnField argumentSpeciesOnField = this.getSpeciesAtCell(xOther, yOther);
                    this.speziesBoard[x][y] = new SpeciesOnField(argumentSpeciesOnField.context);
                }

                return true;
            });
        }
    }

    public void selection(int x, int y, Direction choosenDirection) {
        if (!hasSpeciesAtCell(x, y)) {
            return;
        }

        SpeciesOnField cellSpeciesOnField = this.getSpeciesAtCell(x, y);

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
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

    public void move(int x, int y, Direction choosenDirection) {
        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            var content = this.speziesBoard[xOther][yOther];
            this.speziesBoard[xOther][yOther] = this.speziesBoard[x][y];
            this.speziesBoard[x][y] = content;
            return true;
        });
    }

    public boolean executeActionAt(int x, int y, Direction direction, BiFunction<Integer, Integer, Boolean> callback) {
        try {
            switch (direction) {
                case RIGHT -> {
                    if ((x + 1) >= this.simulationConfig.width) {
                        throw new RuntimeException("Invalid width");
                    }
                    return callback.apply(x + 1, y);
                }
                case LEFT -> {
                    if ((x - 1) < 0) {
                        throw new RuntimeException("Invalid width");
                    }
                    return callback.apply(x - 1, y);
                }
                case TOP -> {
                    if ((y - 1) < 0) {
                        throw new RuntimeException("Invalid height");
                    }
                    return callback.apply(x, y - 1);
                }
                case DOWN -> {
                    if ((y + 1) >= this.simulationConfig.height) {
                        throw new RuntimeException("Invalid height");
                    }
                    return callback.apply(x, y + 1);
                }
                default -> throw new IllegalArgumentException();
            }
        } catch (RuntimeException ignored) {
        }

        return true;
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
