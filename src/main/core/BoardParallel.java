package main.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import main.core.config.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BoardParallel extends Board {

    public ReentrantLock[][] lockmap;

    int threadCount = 4;

    ExecutorService pool;

    public BoardParallel(Config config) {
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
    /*
    Mithilfe der map bekommen wir eine Referenz auf das Future des Executors/Tasks.
    So kann man messen, wie lange die gesamte run-Methode dauert.
    1. Start Time
    2. run() -> aufgabe ausführen + main-thread wartet blockierend, bis sub-threads fertig sind
    3. End Time -> Differenz
     */
    public void run(Function<Integer, Boolean> callback) {
        //Start Time
        long startTime = System.currentTimeMillis();
        Map<Integer, Future<Boolean>> futureMap = new HashMap<>();
        for (int threadIncrement = 1; threadIncrement <= this.threadCount; threadIncrement++) {
            Future<Boolean> future = this.pool.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    execute(callback);
                    return true;
                }

                public Callable<Boolean> init() {
                    return this;
                }

            }.init());
            futureMap.put(threadIncrement, future);
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
                    System.out.println("Duration: " + estimatedTime + " Milliseconds");
                }
        );
        t.start();
    }

    public void execute(Function<Integer, Boolean> callback) {
        var random = ThreadLocalRandom.current();

        for (int i = 0; i <= this.config.maxIterations / this.threadCount; i++) {
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
