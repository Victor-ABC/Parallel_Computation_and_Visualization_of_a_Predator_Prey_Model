package main.core;

import main.core.config.SimulationConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BoardParallel extends Board {

    public ReentrantLock[][] lockmap;

    int threadCount = 4;

    ExecutorService pool;

    public BoardParallel(SimulationConfig simulationConfig) {
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
                    execute(callback);
                }
            }.init(threadIncrement));
        }
    }

    public void execute(Function<Integer, Boolean> callback) {
        var random = ThreadLocalRandom.current();

        for (int i = 0; i <= this.simulationConfig.maxIterations / this.threadCount; i++) {
            for (int index = 0; index < this.simulationConfig.width * this.simulationConfig.height; index++) {
                int randomColumn = random.nextInt(this.simulationConfig.width);
                int randomRow = random.nextInt(this.simulationConfig.height);
                Direction choosenDirection = Direction.randomLetter();
                this.getLock(randomColumn, randomRow, choosenDirection);
                try {
                    this.action(randomColumn, randomRow, choosenDirection);
                } finally {
                    this.unlock(randomColumn, randomRow, choosenDirection);
                }

            }

            callback.apply(i);
        }
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
