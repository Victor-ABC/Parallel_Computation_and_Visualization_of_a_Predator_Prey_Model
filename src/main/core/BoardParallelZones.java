package main.core;

import static main.core.Util.getData;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import main.core.config.Config;

/**
 * Die Klasse "BoardParallelZones" erweitert die Klasse "Board" und stellt ein Spielbrett für
 * parallele Berechnungen mit Zonen dar.
 * Idee: ein 20X20 Spielfeld mit 4 Threads wird in
 * 4 Zonen a 20X5 aufgeteilt.
 * Im Gegensatz zu BoardParallel müssen nur dann Felder gesperrt werden, wenn sich diese in Randbereichen
 * befinden. (Hoffnung: weniger Sync-Overhead ; Problem: Prüfung, ob in Kritischer Region dauert Zeit)
 *
 * Beispiel: 20X20 Spielfeld mit 4 Threads (A, B, C und D)
 *
 * 0 = nicht Kritisch
 * 1 = Kritisch
 *
 * A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1)
 * A(1) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(1)
 * A(1) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(1)
 * A(1) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(1)
 * A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1)
 * B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1)
 * B(1) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(1)
 * B(1) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(1)
 * B(1) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(1)
 * B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1)
 * C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1)
 * C(1) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(1)
 * C(1) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(1)
 * C(1) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(1)
 * C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1)
 * D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1)
 * D(1) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(1)
 * D(1) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(1)
 * D(1) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(1)
 * D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1)
 */
public class BoardParallelZones extends Board {

    public ReentrantLock[][] lockmap;
    SplittableRandom random = new SplittableRandom();
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
        for (int i = 0; i < this.config.maxIterations; i++) {
            for (int index = 0; index < this.config.width * this.config.height; index++) {
                Future<Boolean> future = this.pool.submit(new Callable<Boolean>() {
                    private int iteration;

                    @Override
                    public Boolean call() {
                        execute();
                        if (this.iteration == 0) { //for counting iterations
                            callback.apply(1);
                        }
                        return true;
                    }

                    public Callable<Boolean> init(int iteration) {
                        this.iteration = iteration;
                        return this;
                    }

                }.init(i));
                futureMap.put(i * index, future);
            }
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

    public void execute() {
        int threadId = (int) (Thread.currentThread().getId() % this.config.numberOfThreads) + 1;
        var threadHeight = this.config.height / this.config.numberOfThreads;
        int randomColumn = random.nextInt(this.config.width);
        int randomRow = random.nextInt(threadHeight * (threadId - 1), threadHeight * threadId);
        Direction choosenDirection = Direction.randomLetter();
        if (isCombinationInCriticalZone(randomColumn, randomRow, choosenDirection)) {
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

    public boolean isCombinationInCriticalZone(int x, int y, Direction choosenDirection) {
        if(isCriticalZone(x, y)) {
            return Boolean.TRUE;
        }

        return this.executeActionAt(x, y, choosenDirection, this::isCriticalZone);
    }

    private boolean isCriticalZone(int x, int y) {
        if (x == 0 || y == 0) {
            return Boolean.TRUE;
        }
        if (x == this.config.width - 1 || y == this.config.height - 1) {
            return Boolean.TRUE;
        }
        var threadHeight = this.config.height / this.config.numberOfThreads;
        //Bsp. threadHeight = 5 (0 - 4)
        // x=0 ist oben in Java 2D array, der höhe 5  z.B. String[5][10]
        // x=4 ist unten in Java 2D array, der höhe 5  z.B. String[5][10]
        boolean isTop = x % threadHeight == 0; //0 % 5 == 0
        boolean isBottom = x % threadHeight == threadHeight - 1; //4 % 5 = 4 = 5 - 1
        return isTop || isBottom;
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
