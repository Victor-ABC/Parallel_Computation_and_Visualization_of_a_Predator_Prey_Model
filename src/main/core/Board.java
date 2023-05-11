package main.core;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import main.core.config.SimulationConfig;
import main.core.config.SpeciesContext;

public class Board {

    public SpeciesContext[][] speziesBoard;

    public SimulationConfig simulationConfig;

    public final Random randomSpeciesGenerator = new Random();
    private int overallProbability;

    public Board(SimulationConfig simulationConfig) {
        int count = simulationConfig.width * simulationConfig.height;

        this.overallProbability = simulationConfig.probabilityOfReproduction
                + simulationConfig.probabilityOfSelection
                + simulationConfig.probabilityOfMovement;

        ArrayList<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, count ).boxed().toList());
        Collections.shuffle(range);
        List<Integer> filledFields = range.subList(0, (count / 100) * simulationConfig.filledFields);

        this.speziesBoard = new SpeciesContext[simulationConfig.width][simulationConfig.height];
        this.simulationConfig = simulationConfig;

        filledFields.forEach(integer -> {
            var fieldWith = (integer - 1) / simulationConfig.width;
            var fieldHeight = (integer) % simulationConfig.width;
            this.speziesBoard[fieldWith][fieldHeight] = this.chooseRandomSpecies(simulationConfig.species);
        });
    }

    public void run(Function<Integer, Boolean> callback) {
        var random = new Random();

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
        var randomValue = this.randomSpeciesGenerator.nextInt(this.overallProbability);

        if (randomValue < simulationConfig.probabilityOfReproduction) {
            this.reproduction(x, y, direction);
            return;
        }

        if (randomValue < simulationConfig.probabilityOfSelection + simulationConfig.probabilityOfReproduction) {
            this.selection(x, y, direction);
            return;
        }

        this.move(x, y, direction);
    }

    public void reproduction(int x, int y, Direction choosenDirection) {
        SpeciesContext cellSpeciesOnField = this.getSpeciesAtCell(x, y);

        if (cellSpeciesOnField != null) {
            this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
                if (!this.hasSpeciesAtCell(xOther, yOther)) {
                    this.speziesBoard[xOther][yOther] = cellSpeciesOnField;
                }

                return true;
            });
        } else  {
            this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
                SpeciesContext argumentSpeciesOnField = this.getSpeciesAtCell(xOther, yOther);
                this.speziesBoard[x][y] = argumentSpeciesOnField;

                return true;
            });
        }
    }

    public void selection(int x, int y, Direction choosenDirection) {
        SpeciesContext cellSpeciesOnField = this.getSpeciesAtCell(x, y);

        if (cellSpeciesOnField == null) {
            return;
        }

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            SpeciesContext otherSpeciesOnField = this.getSpeciesAtCell(xOther, yOther);

            if (otherSpeciesOnField == null) {
                return Boolean.TRUE;
            }


            if (otherSpeciesOnField.isEating(cellSpeciesOnField)) {
                this.speziesBoard[x][y] = null;
            }

            if (cellSpeciesOnField.isEating(otherSpeciesOnField)) {
                this.speziesBoard[xOther][yOther] = null;
            }

            return Boolean.TRUE;
        });
    }

    public void move(int x, int y, Direction choosenDirection) {
        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            SpeciesContext content = this.speziesBoard[xOther][yOther];
            this.speziesBoard[xOther][yOther] = this.speziesBoard[x][y];
            this.speziesBoard[x][y] = content;
            return Boolean.TRUE;
        });
    }

    public boolean executeActionAt(int x, int y, Direction direction, BiFunction<Integer, Integer, Boolean> callback) {
        switch (direction) {
            case RIGHT -> {
                if ((x + 1) >= this.simulationConfig.width) {
                    return callback.apply(0, y);
                }
                return callback.apply(x + 1, y);
            }
            case LEFT -> {
                if ((x - 1) < 0) {
                    return callback.apply(this.simulationConfig.width - 1, y);
                }
                return callback.apply(x - 1, y);
            }
            case TOP -> {
                if ((y - 1) < 0) {
                    return callback.apply(x, this.simulationConfig.height - 1);
                }
                return callback.apply(x, y - 1);
            }
            case DOWN -> {
                if ((y + 1) >= this.simulationConfig.height) {
                    return callback.apply(x, 0);
                }
                return callback.apply(x, y + 1);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public SpeciesContext chooseRandomSpecies(SpeciesContext[] speciesContexts) {
        int rand = this.randomSpeciesGenerator.nextInt(speciesContexts.length);
        return speciesContexts[rand];
    }

    public boolean hasSpeciesAtCell(int xAches, int yAches) {
        return this.speziesBoard[xAches][yAches] != null;
    }

    public SpeciesContext getSpeciesAtCell(int xAches, int yAches) {
        return this.speziesBoard[xAches][yAches];
    }
}
