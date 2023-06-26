package main.simulation.core;

import static main.simulation.analysis.Util.getValuesOfConfigProperties;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import main.simulation.analysis.CsvAccess;
import main.simulation.config.Config;

public abstract class BoardParallel extends Board {

    public ReentrantLock[][] lockmap;
    ExecutorService pool;

    /**
     * Es wird ein zweidimensionales Array von ReentrantLocks mit der Größe des Spielfelds
     * (config.width * config.height) erstellt. Dabei wird für jedes Feld im Array ein ReentrantLock-Objekt
     * erzeugt und in das entsprechende Feld eingefügt.
     * Dieses Array dient zur Synchronisation der Threads während der parallelen Verarbeitung.
     *
     * Des Weiteren wird ein ExecutorService "pool" erstellt, der für die Verwaltung der Threads verwendet wird.
     * Der ExecutorService wird mit einer festen Anzahl von Threads initialisiert,
     * die in der Konfiguration (config.numberOfThreads) angegeben ist.
     *
     * Der Konstruktor der Klasse "BoardParallel" bereitet also die erforderlichen Datenstrukturen für
     * die parallele Verarbeitung vor, indem er das Lock-Array erstellt und den ExecutorService initialisiert.
     *
     * Durch die Verwendung von ReentrantLocks und dem ExecutorService ermöglicht die Klasse "BoardParallel"
     * die gleichzeitige Ausführung von Aktionen auf dem Spielfeld durch mehrere Threads und gewährleistet
     * die erforderliche Synchronisation für den Zugriff auf die Spielfeldressourcen.
     * @param config config
     */
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

    /**
     * Diese Methode blockiert in einer Atomaren (synchronized) Anweisung ein Feld + Nachbarfeld
     * @param x x
     * @param y y
     * @param choosenDirection Nachbar
     */
    public synchronized void getLock(int x, int y, Direction choosenDirection) {
        this.lockmap[x][y].lock();

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            this.lockmap[xOther][yOther].lock();

            return true;
        });
    }

    /**
     * gibt Feld + Nachbarfeld wieder frei
     * @param x x
     * @param y y
     * @param choosenDirection Nachbar
     */
    public void unlock(int x, int y, Direction choosenDirection) {
        this.lockmap[x][y].unlock();

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            this.lockmap[xOther][yOther].unlock();

            return true;
        });
    }

    void executeAllTasks(Callable<Boolean> task, Map<Integer, Future<Boolean>> futureMap) {
        for (int i = 0; i < this.config.maxIterations; i++) {
            Future<Boolean> future = this.pool.submit(task);
            futureMap.put(i, future);
        }
    }

    /**
     * Die Methode waitForFinishAndPersistMetrics wartet auf den Abschluss von mehreren Threads
     * und speichert anschließend Metriken
     * Die Methode verwendet einen separaten Thread (t), um den Hauptthread nicht zu blockieren.
     * Der neue Thread durchläuft alle Einträge im futureMap, die den Status der einzelnen Aufgaben darstellen.
     * Dabei wird der get()-Aufruf aufgerufen, um blockierend zu warten,
     * bis alle Aufgaben im Thread-Pool abgeschlossen sind.
     *
     * Nachdem alle Aufgaben abgeschlossen sind, wird die Gesamtzeit berechnet, indem die aktuelle Systemzeit
     * (System.currentTimeMillis()) von der Startzeit (startTime) abgezogen wird.
     * Anschließend werden die Metriken mithilfe der Util-Klasse in einer CSV-Datei gespeichert.
     * @param futureMap map mit den Futures der Tasks
     * @param startTime _
     */
    public void waitForFinishAndPersistMetrics(Map<Integer, Future<Boolean>> futureMap, long startTime) {
        //Neuer Thread, der blockierend wartet, bis die anderen Threads alle die Tasks beendet haben.
        //Grund: nimmt man den main-thread, wartet dieser blockierend und die UI wird nicht rerendert/die
        //       Anwendung "freezed" komplett
        Thread t = new Thread(
                () -> {
                    for (Entry<Integer, Future<Boolean>> entry : futureMap.entrySet()) {
                        try {
                            entry.getValue().get(); //Wichtig! Hier wird blockierend gewartet,
                            //bis alle Tasks im Thread-Pool fertig sind.
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //End Time
                    long estimatedTime = System.currentTimeMillis() - startTime;
                    CsvAccess.appendRowInCSV(config.getMetrics().getPath(),
                            config.getMetrics().getMetricsCsvFileName(), getValuesOfConfigProperties(config, estimatedTime));
                    System.out.println("Duration: " + estimatedTime + " Milliseconds");
                    System.exit(0);
                }
        );
        t.start();
    }

}
