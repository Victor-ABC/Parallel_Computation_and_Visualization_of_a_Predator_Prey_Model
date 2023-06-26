package main.simulation.core;

import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;
import main.simulation.analysis.CsvAccess;
import main.simulation.config.Config;

/**
 * Die Klasse "BoardParallelZones" erweitert die Klasse "BoardParallel" und stellt ein Spielbrett für
 * parallele Berechnungen mit Zonen dar.
 * Idee: ein 20 (breite) X 20 (höhe) Spielfeld mit 4 Threads wird in
 * 4 Zonen a 20 (breite) X 5 (höhe) aufgeteilt.
 * Im Gegensatz zu BoardParallel müssen nur dann Felder gesperrt werden, wenn sich diese in Randbereichen
 * befinden. (Hoffnung: weniger Sync-Overhead ; Problem: Prüfung, ob in Kritischer Region dauert Zeit)
 *
 * Beispiel: 20X20 Spielfeld mit 4 Threads (A, B, C und D)
 *
 * 0 = nicht Kritisch
 * 1 = Kritisch
 *
 * A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1)
 * A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0)
 * A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0)
 * A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0) A(0)
 * A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1) A(1)
 * B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1)
 * B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0)
 * B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0)
 * B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0) B(0)
 * B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1) B(1)
 * C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1)
 * C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0)
 * C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0)
 * C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0) C(0)
 * C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1) C(1)
 * D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1)
 * D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0)
 * D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0)
 * D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0) D(0)
 * D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1) D(1)
 */
public class BoardParallelZones extends BoardParallel {


    SplittableRandom random = new SplittableRandom();

    public BoardParallelZones(Config config) {
        super(config);
    }

    @Override
    public void run(Function<Integer, Boolean> callback) {
        CsvAccess.createCSV(config.getMetrics().getPath(), config.getMetrics().getMetricsCsvFileName(),
                config.getMetrics().getUseFields());
        //Start Time
        long startTime = System.currentTimeMillis();
        Map<Integer, Future<Boolean>> futureMap = new HashMap<>();
        executeAllTasks(createTask(callback), futureMap);
        this.waitForFinishAndPersistMetrics(futureMap, startTime);
    }

    private Callable<Boolean> createTask(
            Function<Integer, Boolean> callback) {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() {
                execute();
                callback.apply(1);
                return true;
            }

            public Callable<Boolean> init() {
                return this;
            }

        }.init();
    }

    public void execute() {
        int threadId = (int) (Thread.currentThread().getId() % this.config.numberOfThreads) + 1;
        var threadHeight = this.config.height / this.config.numberOfThreads;
        for (int index = 0; index < this.config.width * this.config.height; index++) {
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
    }

    public boolean isCombinationInCriticalZone(int x, int y, Direction choosenDirection) {
        if(isCriticalZone(x, y)) {
            return Boolean.TRUE;
        }
        return this.executeActionAt(x, y, choosenDirection, this::isCriticalZone);
    }

    public boolean isCriticalZone(int x, int y) {
        var threadHeight = this.config.height / this.config.numberOfThreads;
        //Bsp. threadHeight = 5 (0 - 4)
        // x=0 ist oben in Java 2D array, der höhe 5  z.B. String[5][10]
        // x=4 ist unten in Java 2D array, der höhe 5  z.B. String[5][10]
        boolean isTop = x % threadHeight == 0; //0 % 5 == 0
        boolean isBottom = x % threadHeight == threadHeight - 1; //4 % 5 = 4 = 5 - 1
        return isTop || isBottom;
    }
}
