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

    public final SplittableRandom randomSpeciesGenerator = new SplittableRandom();
    private int overallProbability;

    public Board(SimulationConfig simulationConfig) {
        int count = simulationConfig.width * simulationConfig.height;

        this.overallProbability = simulationConfig.probabilityOfReproduction
                + simulationConfig.probabilityOfSelection
                + simulationConfig.probabilityOfMovement;

        ArrayList<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, count ).boxed().toList());
        Collections.shuffle(range);
        List<Integer> filledFields = range.subList(0, (count / 100) * simulationConfig.filledFieldsInPercent);

        this.speziesBoard = new SpeciesContext[simulationConfig.width][simulationConfig.height];
        this.simulationConfig = simulationConfig;


            filledFields.forEach(integer -> {
                var fieldWith = (integer - 1) / simulationConfig.height;
                var fieldHeight = (integer - fieldWith * simulationConfig.width) % simulationConfig.height;
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
                if (this.speziesBoard[xOther][yOther] == null) {
                    this.speziesBoard[xOther][yOther] = cellSpeciesOnField;
                }

                return true;
            });
        } else  {
            this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
                this.speziesBoard[x][y] = this.getSpeciesAtCell(xOther, yOther);

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

            if (otherSpeciesOnField == null | otherSpeciesOnField == cellSpeciesOnField) {
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
            if(this.speziesBoard[xOther][yOther] == this.speziesBoard[x][y]) {
                return Boolean.TRUE;
            }

            SpeciesContext content = this.speziesBoard[xOther][yOther];
            this.speziesBoard[xOther][yOther] = this.speziesBoard[x][y];
            this.speziesBoard[x][y] = content;
            return Boolean.TRUE;
        });
    }

    public boolean executeActionAt(int x, int y, Direction direction, BiFunction<Integer, Integer, Boolean> callback) {
        switch (direction) {
            case RIGHT -> {
                return callback.apply((x + 1) % this.simulationConfig.width, y);
            }
            case LEFT -> {
                return callback.apply((x - 1 + this.simulationConfig.width) % this.simulationConfig.width , y);
            }
            case TOP -> {
                return callback.apply(x, (y - 1 + this.simulationConfig.height) % this.simulationConfig.height);
            }
            case DOWN -> {
                return callback.apply(x, (y + 1 + this.simulationConfig.height) % this.simulationConfig.height);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public SpeciesContext chooseRandomSpecies(SpeciesContext[] speciesContexts) {
        int rand = this.randomSpeciesGenerator.nextInt(speciesContexts.length);
        return speciesContexts[rand];
    }

    public SpeciesContext getSpeciesAtCell(int xAches, int yAches) {
        return this.speziesBoard[xAches][yAches];
    }
}
