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

/**
 * Die Klasse "BoardParallel" erweitert die Klasse "Board" und fügt zusätzliche Funktionalität hinzu,
 * um parallele Verarbeitung und Thread-Synchronisation zu ermöglichen.
 */
public class BoardParallel extends Board {

    public ReentrantLock[][] lockmap;

    ExecutorService pool;

    /**
     * Die Klasse "BoardParallel" erweitert die Klasse "Board" und fügt zusätzliche Funktionalität hinzu,
     * um parallele Verarbeitung und Thread-Synchronisation zu ermöglichen.
     *
     * Im Konstruktor der Klasse "BoardParallel" wird zunächst der Konstruktor der Basisklasse "Board"
     * mit dem übergebenen Config-Objekt aufgerufen, um die grundlegende Initialisierung durchzuführen.
     *
     * Anschließend wird ein zweidimensionales Array von ReentrantLocks mit der Größe des Spielfelds
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
     * @param config Konfiguration
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
     * Die Methode "run" startet die parallele Ausführung der Simulation.
     * Zunächst werden CSV-Dateien für die Metriken erstellt und die Startzeit erfasst.
     *
     * Dann wird eine Map "futureMap" erstellt, um die zukünftigen Ergebnisse der Threads zu speichern.
     * In einer Schleife werden für jede Iteration der Simulation Callable-Objekte erzeugt und an den
     * ExecutorService übergeben.
     * Jedes Callable-Objekt führt die Methode "execute(callback)" aus und ruft anschließend die
     * callback-Funktion auf. Das Ergebnis des Callables wird in der futureMap gespeichert.
     *
     * Um die Threads zu synchronisieren, wird ein separater Thread erstellt, der blockierend auf die
     * Beendigung der anderen Threads wartet. Dies ist notwendig, um ein Blockieren des Hauptthreads zu
     * verhindern und die Benutzeroberfläche (UI) reaktiv zu halten. In diesem Thread werden die
     * Ergebnisse der Threads abgerufen und die Endzeit erfasst. Abschließend werden die Metriken
     * in die CSV-Datei geschrieben, die Dauer ausgegeben und das Programm beendet.
     *
     * Die Methode "run" koordiniert also die parallele Ausführung der Simulation und sorgt dafür,
     * dass alle Threads ihre Aufgaben abschließen, bevor das Programm endet.
     * @param callback misst, wie viele Iterationen bereits erfolgt sind.
     */
    @Override
    public void run(Function<Integer, Boolean> callback) {
        Util.createCSV(config.getMetrics().getPath(), config.getMetrics().getMetricsCsvFileName(),
                config.getMetrics().getUseFields());
        //Start Time
        long startTime = System.currentTimeMillis();
        Map<Integer, Future<Boolean>> futureMap = new HashMap<>();
        for (int i = 0; i < this.config.maxIterations; i++) {
            Future<Boolean> future = this.pool.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    execute();
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
                            //bis alle Tasks im Thread-Pool fertig sind.
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

    /**
     * In einer Schleife wird für jede Zelle des Spielfelds eine zufällige Spalte, eine zufällige Reihe
     * und eine zufällige Richtung ausgewählt.
     * Dabei wird zuerst das feld (x + y) und das nachbarfeld "choosenDirection" blockiert
     *
     * Dann wird die Aktion für die ausgewählte Zelle, Spalte, Reihe und Richtung ausgeführt.
     *
     * Unabhängig vom Ergebnis wird schließlich das Schloss für die Zellen wieder freigegeben.
     * getLock() -> ist Atomar/Thread-save, da sonst Deadlocks auftreten könnten
     * Beispiel:
     * Zeitpunkt 1: T1 lockt (x=1, y=1)
     * Zeitpunkt 2: T2 lockt (x=1, y=2)
     * Zeitpunkt 3: T1 will nachbar locken (x=1, y=2) (geht nicht)
     * Zeitpunkt 4: T2 will nachbar locken (x=1, y=1) (geht nicht)
     * Ergebnis: T1 und T2 blockieren jeweils das, was der andere Thread möchte!
     */
    public void execute() {
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
}
