package main.core;

import static main.core.Util.getData;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import main.core.config.Config;

public class BoardParallel extends Board {

    public ReentrantLock[][] lockmap;

    ExecutorService pool;

    public BoardParallel(Config config) {
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
    /*
    Mithilfe der map bekommen wir eine Referenz auf das Future des Executors/Tasks.
    So kann man messen, wie lange die gesamte run-Methode dauert.
    1. Start Time
    2. run() -> aufgabe ausführen + main-thread wartet blockierend, bis sub-threads fertig sind
    3. End Time -> Differenz
     */
    public void run(Function<Integer, Boolean> callback) {
        Util.createCSV(config.getMetrics().getPath(), config.getMetrics().getMetricsCsvFileName(),
                config.getMetrics().getUseFields());
        //Start Time
        long startTime = System.currentTimeMillis();
        Map<Integer, Future<Boolean>> futureMap = new HashMap<>();
        for (int i = 0; i < this.config.maxIterations; i++) {
            Future<Boolean> future = this.pool.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    execute(callback);
                    callback.apply(1);
                    return true;
                }

                public Callable<Boolean> init() {
                    return this;
                }

            }.init());
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

    public void execute(Function<Integer, Boolean> callback) {
        var random = ThreadLocalRandom.current();
        for (int index = 0; index < this.config.width * this.config.height; index++) {
            int randomColumn = random.nextInt(this.config.width);
            int randomRow = random.nextInt(this.config.height);
            Direction choosenDirection = Direction.randomLetter();
            this.getLock(randomColumn, randomRow, choosenDirection);
            try {
                this.action(randomColumn, randomRow, choosenDirection);
            } finally {
                this.unlock(randomColumn, randomRow, choosenDirection);
            }
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
