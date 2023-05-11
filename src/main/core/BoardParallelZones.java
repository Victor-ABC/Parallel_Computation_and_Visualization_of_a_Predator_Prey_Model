package main.core;

import main.core.config.SimulationConfig;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BoardParallelZones extends Board {

    public ReentrantLock[][] lockmap;

    int threadCount = 2;

    public BoardParallelZones(SimulationConfig simulationConfig) {
        super(simulationConfig);

        this.lockmap = new ReentrantLock[simulationConfig.width][simulationConfig.height];

        for (int row = 0; row < simulationConfig.width; row++) {
            for (int col = 0; col < simulationConfig.height; col++) {
                    this.lockmap[row][col] = new ReentrantLock();
            }
        }
    }

    @Override
    public void run(Function<Integer, Boolean> callback) {
        for (int threadIncrement = 1; threadIncrement <= this.threadCount; threadIncrement++) {
            new Thread(new Runnable() {
                private int number;

                public Runnable init(int number) {
                    this.number = number;
                    return this;
                }

                @Override
                public void run() {
                    execute(this.number, callback);
                }
            }.init(threadIncrement)).start();
        }
    }

    public void execute(int threadIncrement, Function<Integer, Boolean> callback) {;
        var threadHeight = this.simulationConfig.height / this.threadCount;

        for (int i = threadIncrement; i <= this.simulationConfig.maxIterations; i += threadIncrement) {
            for (int index = 0; index < this.simulationConfig.width * this.simulationConfig.height; index++) {
                int randomColumn = random.nextInt(this.simulationConfig.width);
                int randomRow = random.nextInt(threadHeight * (threadIncrement - 1), threadHeight * threadIncrement);
                Direction choosenDirection = Direction.randomLetter();

                if(isCombinationInCriticalZone(randomColumn, randomRow, choosenDirection)) {
                    this.getLock(randomColumn, randomRow, choosenDirection);
                    try {
                        this.action(randomColumn, randomRow, choosenDirection);
                    } finally {
                        this.unlock(randomColumn, randomRow, choosenDirection);
                    }
                } else {
                    this.action(randomColumn, randomRow, choosenDirection);
                }
            }
        }
    }

    public boolean isCombinationInCriticalZone(int x, int y, Direction choosenDirection) {
        if(isCriticalZone(x, y)) {
            return true;
        }

        return this.executeActionAt(x, y, choosenDirection, this::isCriticalZone);
    }

    private boolean isCriticalZone(int x, int y) {
        if(x == 0) {
            return true;
        }

        if(y == 0) {
            return true;
        }

        if(x == this.simulationConfig.width - 1) {
            return true;
        }

        if(y == this.simulationConfig.height - 1) {
            return true;
        }

        var threadHeight = this.simulationConfig.height / this.threadCount;

        if(y % threadHeight == 0) {
            return true;
        }

        if(y % threadHeight == this.simulationConfig.height - 1) {
            return true;
        }

        return false;
    }

    public synchronized void getLock(int x, int y, Direction choosenDirection) {
            this.lockmap[x][y].lock();

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
                this.lockmap[xOther][yOther].lock();

            return true;
        });
    }

    public void unlock(int x, int y, Direction choosenDirection) {
        this.lockmap[x][y].unlock();

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            this.lockmap[xOther][yOther].unlock();

            return true;
        });
    }
}
