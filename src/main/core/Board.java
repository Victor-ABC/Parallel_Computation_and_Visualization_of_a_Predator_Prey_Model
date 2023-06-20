package main.core;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import main.core.config.Config;
import main.core.config.Species;

public class Board {

    public Species[][] speziesBoard;

    public Config config;

    public final SplittableRandom randomSpeciesGenerator = new SplittableRandom();
    private int overallProbability;

    public Board(Config config) {
        int count = config.width * config.height;

        this.overallProbability = config.probabilityOfReproduction
                + config.probabilityOfSelection
                + config.probabilityOfMovement;

        ArrayList<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, count ).boxed().toList());
        Collections.shuffle(range);
        List<Integer> filledFields = range.subList(0, (count / 100) * config.filledFieldsInPercent);

        this.speziesBoard = new Species[config.width][config.height];
        this.config = config;


            filledFields.forEach(integer -> {
                var fieldWith = (integer - 1) / config.height;
                var fieldHeight = (integer - fieldWith * config.width) % config.height;
                this.speziesBoard[fieldWith][fieldHeight] = this.chooseRandomSpecies(config.species);
            });
    }

    public void run(Function<Integer, Boolean> callback) {
        var random = new Random();

        new Thread(() -> {
            for (int i = 0; i < this.config.maxIterations; i++) {
                System.out.println(i);
                for (int index = 0; index < this.config.width * this.config.height; index++) {
                        int randomColumn = random.nextInt(this.config.width);
                        int randomRow = random.nextInt(this.config.height);
                        Direction choosenDirection = Direction.randomLetter();

                        this.action(randomColumn, randomRow, choosenDirection);
                }

                callback.apply(i);
            }
        }).start();
    }

    public void action(int x, int y, Direction direction) {
        var randomValue = this.randomSpeciesGenerator.nextInt(this.overallProbability);

        if (randomValue < config.probabilityOfReproduction) {
            this.reproduction(x, y, direction);
            return;
        }

        if (randomValue < config.probabilityOfSelection + config.probabilityOfReproduction) {
            this.selection(x, y, direction);
            return;
        }

        this.move(x, y, direction);
    }

    public void reproduction(int x, int y, Direction choosenDirection) {
        Species cellSpeciesOnField = this.getSpeciesAtCell(x, y);

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
        Species cellSpeciesOnField = this.getSpeciesAtCell(x, y);

        if (cellSpeciesOnField == null) {
            return;
        }

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            Species otherSpeciesOnField = this.getSpeciesAtCell(xOther, yOther);

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

            Species content = this.speziesBoard[xOther][yOther];
            this.speziesBoard[xOther][yOther] = this.speziesBoard[x][y];
            this.speziesBoard[x][y] = content;
            return Boolean.TRUE;
        });
    }

    public boolean executeActionAt(int x, int y, Direction direction, BiFunction<Integer, Integer, Boolean> callback) {
        switch (direction) {
            case RIGHT -> {
                return callback.apply((x + 1) % this.config.width, y);
            }
            case LEFT -> {
                return callback.apply((x - 1 + this.config.width) % this.config.width , y);
            }
            case TOP -> {
                return callback.apply(x, (y - 1 + this.config.height) % this.config.height);
            }
            case DOWN -> {
                return callback.apply(x, (y + 1 + this.config.height) % this.config.height);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public Species chooseRandomSpecies(Species[] species) {
        int rand = this.randomSpeciesGenerator.nextInt(species.length);
        return species[rand];
    }

    public Species getSpeciesAtCell(int xAches, int yAches) {
        return this.speziesBoard[xAches][yAches];
    }
}
