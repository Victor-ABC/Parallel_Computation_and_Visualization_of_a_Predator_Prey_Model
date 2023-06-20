package main.core;

import main.core.config.Config;

import java.util.SplittableRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BoardParallelZones extends Board {

    public ReentrantLock[][] lockmap;

    int threadCount = 4;

    ExecutorService pool;

    public BoardParallelZones(Config config) {
        super(config);

        this.lockmap = new ReentrantLock[config.width][config.height];

        for (int row = 0; row < config.width; row++) {
            for (int col = 0; col < config.height; col++) {
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

        var threadHeight = this.config.height / this.threadCount;

        for (int i = 0; i <= this.config.maxIterations / this.threadCount; i++) {
            for (int index = 0; index < this.config.width * this.config.height; index++) {
                int randomColumn = random.nextInt(this.config.width);
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

        if(x == this.config.width - 1 || y == this.config.height - 1) {
            return Boolean.TRUE;
        }

        var threadHeight = this.config.height / this.threadCount;

        return y % threadHeight == 0 || y % threadHeight == this.config.height - 1;
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
