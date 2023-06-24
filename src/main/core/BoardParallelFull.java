package main.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import main.core.config.Config;

/**
 * Die Klasse "BoardParallel" erweitert die Klasse "Board" und fügt zusätzliche Funktionalität hinzu,
 * um parallele Verarbeitung und Thread-Synchronisation zu ermöglichen.
 */
public class BoardParallelFull extends BoardParallel {

    /**
     * @param config Konfiguration
     */
    public BoardParallelFull(Config config) {
        super(config);
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
        executeAllTasks(createTask(callback), futureMap);
        waitForFinishAndPersistMetrics(futureMap, startTime);
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
}
