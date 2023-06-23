package main.core;

import static main.core.Util.getData;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import main.core.config.Config;

public class BoardParallelZones extends Board {

    public ReentrantLock[][] lockmap;

    ExecutorService pool;

    public BoardParallelZones(Config config) {
        super(config);

        this.lockmap = new ReentrantLock[config.width][config.height];

        for (int row = 0; row < config.width; row++) {
            for (int col = 0; col < config.height; col++) {
                this.lockmap[row][col] = new ReentrantLock();
            }
        }

        pool = Executors.newFixedThreadPool(this.config.numberOfThreads);
    }

    @Override
    public void run(Function<Integer, Boolean> callback) {
        Util.createCSV(config.getMetrics().getPath(), config.getMetrics().getMetricsCsvFileName(),
                config.getMetrics().getUseFields());
        //Start Time
        long startTime = System.currentTimeMillis();
        Map<Integer, Future<Boolean>> futureMap = new HashMap<>();
        for (int i = 0; i <= this.config.maxIterations; i++) {
            Future<Boolean> future = this.pool.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    execute(this.number, callback);
                    callback.apply(1);
                    return true;
                }

                private int number;

                public Callable<Boolean> init(int number) {
                    this.number = number;
                    return this;
                }

            }.init((i % this.config.numberOfThreads) + 1));
            futureMap.put(i, future);
        }
        //Neuer Thread, der blockierend wartet, bis die anderen Threads alle die Tasks beendet haben.
        //Grund: nimmt man den main-thread, wartet dieser blockierend und die UI wird nicht rerendert/die
        //       Anwendung "freezed" komplett
        Thread t = new Thread(
                () -> {
                    for (Entry<Integer, Future<Boolean>> entry : futureMap.entrySet()) {
                        try {
                            entry.getValue().get(); //Wichtig! Hier wird blockierend gewartet,
                            //bis der Thread fertig ist
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //End Time
                    long estimatedTime = System.currentTimeMillis() - startTime;
                    Util.appendRowInCSV(config.getMetrics().getPath(),
                            config.getMetrics().getMetricsCsvFileName(), getData(config, estimatedTime));
                    System.out.println("Duration: " + estimatedTime + " Milliseconds");
                    System.exit(0);
                }
        );
        t.start();
    }

    public void execute(int threadIncrement, Function<Integer, Boolean> callback) {;
        var random = new SplittableRandom();

        var threadHeight = this.config.height / this.config.numberOfThreads;

        for (int index = 0; index < this.config.width * this.config.height; index++) {
            int randomColumn = random.nextInt(this.config.width);
            int randomRow = random.nextInt(threadHeight * (threadIncrement - 1), threadHeight * threadIncrement);
            Direction choosenDirection = Direction.randomLetter();

            if(isCombinationInCriticalZone(randomColumn, randomRow, choosenDirection)) {
                this.lockmap[randomColumn][randomRow].lock();

                this.executeActionAt(randomColumn, randomRow, choosenDirection, (xOther, yOther) -> {
                    this.lockmap[xOther][yOther].lock();

                    return Boolean.TRUE;
                });

                try {
                    this.action(randomColumn, randomRow, choosenDirection);
                } finally {


                    this.lockmap[randomColumn][randomRow].unlock();

                    this.executeActionAt(randomColumn, randomRow, choosenDirection, (xOther, yOther) -> {
                        this.lockmap[xOther][yOther].unlock();

                        return Boolean.TRUE;
                    });

                }
            } else {
                this.action(randomColumn, randomRow, choosenDirection);
            }
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

        var threadHeight = this.config.height / this.config.numberOfThreads;

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
