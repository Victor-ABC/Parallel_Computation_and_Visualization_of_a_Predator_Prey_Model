package main.core;

import main.core.config.SimulationConfig;

import java.util.SplittableRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BoardParallelZones extends Board {

    public ReentrantLock[][] lockmap;

    int threadCount = 4;

    ExecutorService pool;

    public BoardParallelZones(SimulationConfig simulationConfig) {
        super(simulationConfig);

        this.lockmap = new ReentrantLock[simulationConfig.width][simulationConfig.height];

        for (int row = 0; row < simulationConfig.width; row++) {
            for (int col = 0; col < simulationConfig.height; col++) {
                this.lockmap[row][col] = new ReentrantLock();
            }
        }

        pool = Executors.newFixedThreadPool(this.threadCount);
    }

    @Override
    public void run(Function<Integer, Boolean> callback) {
        for (int threadIncrement = 1; threadIncrement <= this.threadCount; threadIncrement++) {
            this.pool.execute(new Runnable() {
                private int number;

                public Runnable init(int number) {
                    this.number = number;
                    return this;
                }

                @Override
                public void run() {
                    execute(this.number, callback);
                }
            }.init(threadIncrement));
        }
    }

    public void execute(int threadIncrement, Function<Integer, Boolean> callback) {;
        var random = new SplittableRandom();

        var threadHeight = this.simulationConfig.height / this.threadCount;

        for (int i = 0; i <= this.simulationConfig.maxIterations / this.threadCount; i++) {
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

            callback.apply(i);
        }
    }

    public boolean isCombinationInCriticalZone(int x, int y, Direction choosenDirection) {
        if(isCriticalZone(x, y)) {
            return Boolean.TRUE;
        }

        return this.executeActionAt(x, y, choosenDirection, this::isCriticalZone);
    }

    private boolean isCriticalZone(int x, int y) {
        if(x == 0 || y == 0) {
            return Boolean.TRUE;
        }

        if(x == this.simulationConfig.width - 1 || y == this.simulationConfig.height - 1) {
            return Boolean.TRUE;
        }

        var threadHeight = this.simulationConfig.height / this.threadCount;

        return y % threadHeight == 0 || y % threadHeight == this.simulationConfig.height - 1;
    }

    public synchronized void getLock(int x, int y, Direction choosenDirection) {
         this.lockmap[x][y].lock();

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            this.lockmap[xOther][yOther].lock();

            return Boolean.TRUE;
        });
    }

    public void unlock(int x, int y, Direction choosenDirection) {
        this.lockmap[x][y].unlock();

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            this.lockmap[xOther][yOther].unlock();

            return Boolean.TRUE;
        });
    }
}
