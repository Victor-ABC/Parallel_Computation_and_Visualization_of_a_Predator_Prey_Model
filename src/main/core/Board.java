package main.core;

import static main.core.Util.getData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import main.core.config.Config;
import main.core.config.Species;

public class Board {

    public Species[][] speziesBoard;

    public Config config;

    public final SplittableRandom randomSpeciesGenerator = new SplittableRandom();
    private final int overallProbability;

    /**
     * Die Konstruktor-Methode "Board" wird mit einer Konfiguration (config) aufgerufen,
     * um ein Spielbrett zu erstellen.
     * Zu Beginn werden die übergebene Konfiguration (config) und die Gesamt-Wahrscheinlichkeit berechnet,
     * indem die Wahrscheinlichkeiten für Reproduktion, Auswahl und Bewegung addiert werden.
     * Die Anzahl der Felder wird basierend auf der Breite und Höhe in der Konfiguration berechnet.
     * Wenn die Konfiguration eine zufällige anfängliche Reihenfolge verlangt (isInitialOrderRandom()),
     * wird die Methode "fillFieldWithRandomOrder" aufgerufen, um das Spielbrett mit einer
     * zufälligen Reihenfolge der Felder zu füllen.
     * Wenn die Konfiguration eine feste Reihenfolge verlangt, wird die Methode "fillFieldWithFixedOrder"
     * aufgerufen, um das Spielbrett mit einer festen Reihenfolge der Felder zu füllen.
     * @param config die Config (vorher: config.json)
     */
    public Board(Config config) {
        this.config = config;
        this.overallProbability = config.probabilityOfReproduction
                + config.probabilityOfSelection
                + config.probabilityOfMovement;
        int amountOfFields = config.width * config.height;
        if (config.isInitialOrderRandom()) {
            fillFieldWithRandomOrder(amountOfFields);
        } else {
            fillFieldWithFixedOrder(amountOfFields);
        }
    }

    /**
     * Die Methode "fillFieldWithFixedOrder" füllt ein Feld in einer festgelegten Reihenfolge mit
     * einer angegebenen Anzahl von Feldern. Sie verwendet einen Pseudo-Zufallszahlengenerator,
     * um eindeutige Zufallszahlen zu generieren. Die Methode nimmt den Parameter "amountOfFields"
     * entgegen, um die Anzahl der zu füllenden Felder zu bestimmen.
     * Die Methode beginnt damit, ein boolesches Array namens "generated" zu erstellen,
     * um die generierten Zahlen zu verfolgen, und eine ArrayList namens "filledFields",
     * um die generierten Zahlen in der Reihenfolge, in der sie generiert werden, zu speichern.
     *
     * Es wird eine neue Instanz von "Random" mit einem angegebenen Seed-Wert (12345) erstellt,
     * um konsistente Ergebnisse zu gewährleisten.
     *
     * Anschließend betritt die Methode eine Schleife, die eindeutige Zufallszahlen generiert.
     * Sie erzeugt eine Zufallszahl mit "random.nextInt(amountOfFields)" solange, bis sie eine Zahl findet,
     * die zuvor noch nicht generiert wurde. Die Schleife läuft so lange, bis "amountOfFields"
     * eindeutige Zahlen generiert wurden.
     *
     * Jede eindeutige Zufallszahl wird im "generated" Array als generiert markiert und zur "filledFields"
     * Liste hinzugefügt. Schließlich wird die Methode "fillField" mit der "filledFields" Liste aufgerufen,
     * um weitere Verarbeitungsschritte durchzuführen.
     * @param amountOfFields z.B. 100 bei einem 10x10 Spielfeld
     */
    private void fillFieldWithFixedOrder(int amountOfFields) {
        // Create an array to keep track of generated numbers
        boolean[] generated = new boolean[amountOfFields];
        List<Integer> filledFields = new ArrayList<>();
        // Create a new instance of Random with the specified seed
        Random random = new Random(12345);

        // Generate unique random numbers
        for (int i = 0; i < amountOfFields; i++) {
            int randomNumber;

            // Generate a random number until it is unique
            do {
                randomNumber = random.nextInt(amountOfFields);
            } while (generated[randomNumber]);
            // Mark the number as generated
            generated[randomNumber] = true;
            // Add random number to fill-array
            filledFields.add(randomNumber);
        }
        fillField(filledFields);
    }

    /**
     * Die Methode "fillFieldWithRandomOrder" füllt ein Feld mit einer bestimmten Anzahl von Feldern
     * in zufälliger Reihenfolge. Dazu werden zunächst alle Zahlen von 0 bis zur angegebenen Anzahl
     * von Feldern in einer Liste gespeichert. Anschließend wird die Reihenfolge der Zahlen zufällig gemischt.
     * Eine Teilmenge dieser zufällig angeordneten Zahlen wird ausgewählt, basierend auf einer prozentualen
     * Konfiguration. Schließlich wird diese Teilmenge verwendet, um das Feld zu füllen. Dadurch werden die
     * Felder in einer zufälligen Anordnung belegt.
     * @param amountOfFields z.B. 100 bei einem 10x10 Spielfeld
     */
    private void fillFieldWithRandomOrder(int amountOfFields) {
        ArrayList<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, amountOfFields).boxed().toList());
        Collections.shuffle(range);
        List<Integer> filledFields = range.subList(0, (amountOfFields / 100) * config.filledFieldsInPercent);
        fillField(filledFields);
    }

    private void fillField(List<Integer> filledFields) {
        this.speziesBoard = new Species[config.width][config.height];
        filledFields.forEach(integer -> {
            var fieldWith = (integer - 1) / config.height;
            var fieldHeight = (integer - fieldWith * config.width) % config.height;
            this.speziesBoard[fieldWith][fieldHeight] = this.chooseRandomSpecies(config.species);
        });
    }

    /**
     * Die Methode "run" führt eine übergebene Funktion aus, die durch den Parameter "callback" definiert wird.
     * Dabei handelt es sich um eine Funktion, die eine Ganzzahl als Eingabe nimmt. Hier wird
     * festgestellt, wie viele Iterationen bereits erfolgt sind.
     * Anschließend wird ein neuer Thread gestartet, in dem folgender Code ausgeführt wird:
     * Zuerst wird eine CSV-Datei erstellt, indem die Methode "createCSV" aufgerufen wird.
     * Dabei werden der Pfad, der Dateiname und die zu verwendenden Felder aus der Konfiguration abgerufen.
     * In dieser CSV-Datei werden anschließend die Metriken des Laufes gespeichert für
     * die anschließende Analyse.
     * Danach wird die Startzeit erfasst, indem der aktuelle Zeitstempel mit "System.currentTimeMillis()"
     * abgerufen wird.
     * Die Methode "execute" wird aufgerufen, wobei der übergebene "callback" und die Instanz von "Random"
     * verwendet werden, um die eigentliche logik auszuführen.
     * Nachdem die Aufgabe abgeschlossen ist, wird die vergangene Zeit seit dem Start berechnet,
     * indem der aktuelle Zeitstempel von der Startzeit abgezogen wird.
     * Die Ergebnisse der Ausführung werden in die CSV-Datei hinzugefügt, indem die Methode "appendRowInCSV"
     * aufgerufen wird. Dabei werden der Pfad, der Dateiname und die zu speichernden Daten aus der
     * Konfiguration abgerufen.
     * (Die Gesamtdauer der Ausführung wird auf der Konsole ausgegeben.)
     * Durch die Ausführung der Methode "run" in einem separaten Thread wird sichergestellt,
     * @param callback misst, wie viele Iterationen bereits erfolgt sind.
     */
    public void run(Function<Integer, Boolean> callback) {
        new Thread(() -> {
            Util.createCSV(config.getMetrics().getPath(), config.getMetrics().getMetricsCsvFileName(),
                    config.getMetrics().getUseFields());
            //Start Time
            long startTime = System.currentTimeMillis();
            execute(callback);
            //End Time
            long estimatedTime = System.currentTimeMillis() - startTime;
            Util.appendRowInCSV(config.getMetrics().getPath(), config.getMetrics().getMetricsCsvFileName(),
                    getData(config, estimatedTime));
            System.out.println("Duration: " + estimatedTime + " Milliseconds");
            System.exit(0);
        }).start();
    }

    /**
     * Eigentliche Programm-Logik für Sequentielle ausführung.
     * Zu Beginn wird eine neue Instanz der Klasse "Random" erstellt, um zufällige Werte zu generieren.
     * Anschließend wird eine äußere Schleife ausgeführt, die durch die maximale Anzahl von Iterationen
     * in der Konfiguration begrenzt ist.
     * In jedem Iterationsschritt wird eine innere Schleife durchlaufen,
     * die über alle Felder eines Gitters iteriert (basierend auf der Breite und Höhe in der Konfiguration).
     * Für jedes Feld wird eine zufällige Spalte und Zeile ausgewählt, indem die "nextInt" Methode von
     * "Random" mit den entsprechenden Grenzen aufgerufen wird.
     * Zusätzlich wird eine zufällige Richtung ("Direction") erzeugt.Die Methode "action" wird aufgerufen,
     * um eine Aktion auf dem ausgewählten Feld mit der zufälligen Richtung auszuführen.
     * Nachdem alle Felder bearbeitet wurden, wird die übergebene Funktion "callback"
     * mit dem aktuellen Iterationsschritt als Argument aufgerufen. (zählt Tasks=maxIterations)
     * @param callback misst, wie viele Iterationen bereits erfolgt sind.
     */
    private void execute(Function<Integer, Boolean> callback) {
        Random random = new Random();
        for (int i = 0; i < this.config.maxIterations; i++) {
            for (int index = 0; index < this.config.width * this.config.height; index++) {
                int randomColumn = random.nextInt(this.config.width);
                int randomRow = random.nextInt(this.config.height);
                Direction choosenDirection = Direction.randomLetter();
                this.action(randomColumn, randomRow, choosenDirection);
            }
            callback.apply(i);
        }
    }

    /**
     * Zu Beginn wird ein zufälliger Wert generiert, indem die Methode "nextInt" des
     * "randomSpeciesGenerator" aufgerufen wird. Dabei wird der Wert auf den Gesamt-Wahrscheinlichkeitsbereich begrenzt.
     * Wenn der generierte zufällige Wert kleiner ist als die Wahrscheinlichkeit
     * der Reproduktion in der Konfiguration, wird die Methode "reproduction" aufgerufen,
     * um eine Reproduktionsaktion auf dem Feld auszuführen. Anschließend wird die Methode verlassen.
     *
     * Wenn der generierte zufällige Wert kleiner ist als die Summe der Wahrscheinlichkeit
     * der Auswahl und der Wahrscheinlichkeit der Reproduktion in der Konfiguration, wird die Methode
     * "selection" aufgerufen, um eine Auswahlaktion auf dem Feld durchzuführen. Danach wird die Methode verlassen.
     *
     * Wenn keine der vorherigen Bedingungen erfüllt ist, wird die Methode "move" aufgerufen,
     * um eine Bewegungsaktion auf dem Feld mit der angegebenen Richtung auszuführen.
     *
     * Die Methode "action" führt also eine bestimmte Aktion auf dem Feld aus,
     * basierend auf zufällig generierten Werten und den Wahrscheinlichkeiten aus der Konfiguration.
     * Je nach zufälligem Wert wird entweder eine Reproduktions-, Auswahl- oder Bewegungsaktion ausgeführt.
     * @param x x-wert
     * @param y y-wert
     * @param direction Nachbar der Von-Neumann-Nachbarschaft
     */
    public void action(int x, int y, Direction direction) {
        var randomValue = this.randomSpeciesGenerator.nextInt(this.overallProbability);
        if (randomValue < config.probabilityOfReproduction) {
            this.reproduction(x, y, direction);
            return;
        }
        if (randomValue < config.probabilityOfSelection + config.probabilityOfReproduction) {
            this.selection(x, y, direction);
            return;
        }
        this.move(x, y, direction);
    }

    /**
     * Die Methode "reproduction" führt eine Reproduktionsaktion auf einem bestimmten Feld mit
     * den übergebenen Koordinaten (x, y) und der angegebenen Richtung (choosenDirection) aus.
     *
     * Zuerst wird die Spezies auf dem Feld abgerufen, indem die Methode "getSpeciesAtCell"
     * aufgerufen wird. Wenn eine Spezies auf dem Feld vorhanden ist, wird die Aktion "executeActionAt"
     * mit den übergebenen Parametern (x, y, choosenDirection) aufgerufen.
     *
     * Innerhalb der Aktion wird eine Funktion übergeben, die überprüft, ob das Ziel-Feld (xOther, yOther)
     * leer ist.
     * Wenn dies der Fall ist (d.h. die SpeziesBoard[xOther][yOther] ist null),
     * wird die Spezies der Zelle (cellSpeciesOnField) in das Ziel-Feld kopiert,
     * indem sie in das entsprechende Feld des SpeziesBoard[xOther][yOther] eingefügt wird.
     *
     * Wenn keine Spezies auf dem Feld vorhanden ist (cellSpeciesOnField == null),
     * wird die Aktion "executeActionAt" erneut aufgerufen. Diesmal wird jedoch die Spezies des Ziel-Felds
     * (getSpeciesAtCell(xOther, yOther)) auf das aktuelle Feld (speziesBoard[x][y]) kopiert.
     * @param x x-value
     * @param y y-value
     * @param choosenDirection Nachbar der Von-Neumann-Nachbarschaft
     */
    public void reproduction(int x, int y, Direction choosenDirection) {
        Species cellSpeciesOnField = this.getSpeciesAtCell(x, y);

        if (cellSpeciesOnField != null) {
            this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
                if (this.speziesBoard[xOther][yOther] == null) {
                    this.speziesBoard[xOther][yOther] = cellSpeciesOnField;
                }

                return true;
            });
        } else  {
            this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
                this.speziesBoard[x][y] = this.getSpeciesAtCell(xOther, yOther);

                return true;
            });
        }
    }

    /**
     * Die Methode "selection" führt eine Auswahlaktion auf einem bestimmten Feld mit den
     * übergebenen Koordinaten (x, y) und der angegebenen Richtung (choosenDirection) aus.
     *
     * Zu Beginn wird die Spezies auf dem Feld abgerufen, indem die Methode "getSpeciesAtCell"
     * aufgerufen wird. Wenn keine Spezies auf dem Feld vorhanden ist (cellSpeciesOnField == null),
     * wird die Methode beendet.
     *
     * Ansonsten wird die Aktion "executeActionAt" mit den übergebenen Parametern
     * (x, y, choosenDirection) aufgerufen.
     * Innerhalb dieser Aktion wird eine Funktion übergeben, die das Verhalten der Auswahlaktion definiert.
     *
     * Zuerst wird die Spezies auf dem Ziel-Feld (xOther, yOther) abgerufen,
     * indem die Methode "getSpeciesAtCell" aufgerufen wird.
     *
     * Wenn keine Spezies auf dem Ziel-Feld vorhanden ist (otherSpeciesOnField == null)
     * oder die Spezies auf dem Ziel-Feld identisch mit der Spezies auf dem aktuellen Feld ist
     * (otherSpeciesOnField == cellSpeciesOnField), wird die Funktion beendet und "Boolean.TRUE" zurückgegeben.
     * Fall: A frisst B
     * Wenn die Spezies auf dem Ziel-Feld die Spezies auf dem aktuellen Feld frisst
     * (otherSpeciesOnField.isEating(cellSpeciesOnField)), wird die Spezies auf dem
     * aktuellen Feld (speziesBoard[x][y]) entfernt (auf null gesetzt).
     * Fall: A wird von B gefressen
     * Wenn die Spezies auf dem aktuellen Feld die Spezies auf dem Ziel-Feld frisst
     * (cellSpeciesOnField.isEating(otherSpeciesOnField)), wird die Spezies auf dem Ziel-Feld
     * (speziesBoard[xOther][yOther]) entfernt (auf null gesetzt).
     * @param x x-value
     * @param y y-value
     * @param choosenDirection Nachbar der Von-Neumann-Nachbarschaft
     */
    public void selection(int x, int y, Direction choosenDirection) {
        Species cellSpeciesOnField = this.getSpeciesAtCell(x, y);

        if (cellSpeciesOnField == null) {
            return;
        }

        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            Species otherSpeciesOnField = this.getSpeciesAtCell(xOther, yOther);

            if (otherSpeciesOnField == null | otherSpeciesOnField == cellSpeciesOnField) {
                return Boolean.TRUE;
            }

            if (otherSpeciesOnField.isEating(cellSpeciesOnField)) {
                this.speziesBoard[x][y] = null;
            }

            if (cellSpeciesOnField.isEating(otherSpeciesOnField)) {
                this.speziesBoard[xOther][yOther] = null;
            }

            return Boolean.TRUE;
        });
    }

    /**
     * Die Methode "move" führt eine Bewegungsaktion auf einem bestimmten Feld mit den übergebenen Koordinaten (x, y) und der angegebenen Richtung (choosenDirection) aus.
     * @param x x-value
     * @param y y-value
     * @param choosenDirection Nachbar der Von-Neumann-Nachbarschaft
     */
    public void move(int x, int y, Direction choosenDirection) {
        this.executeActionAt(x, y, choosenDirection, (xOther, yOther) -> {
            if(this.speziesBoard[xOther][yOther] == this.speziesBoard[x][y]) {
                return Boolean.TRUE;
            }

            Species content = this.speziesBoard[xOther][yOther];
            this.speziesBoard[xOther][yOther] = this.speziesBoard[x][y];
            this.speziesBoard[x][y] = content;
            return Boolean.TRUE;
        });
    }

    /**
     *
     * Die Methode "executeActionAt" führt eine Aktion auf einem bestimmten Feld mit den übergebenen
     * Koordinaten (x, y) und der angegebenen Richtung (direction) aus.
     * Die Aktion wird durch eine Callback-Funktion definier. So muss nur an einer stelle im Code die
     * Entscheidung der Richtung definiert werden!
     *
     * In der Methode wird ein Switch-Statement verwendet, um die Richtung zu überprüfen.
     * Je nach Richtung wird die entsprechende Berechnung für die Zielkoordinaten durchgeführt
     * und die Callback-Funktion mit den berechneten Koordinaten aufgerufen.
     *
     * Wenn die Richtung "RIGHT" ist, werden die x-Koordinaten um eins erhöht und auf die
     * Breite des Spielfelds (config.width) modulo gerechnet.
     * Das Ergebnis wird zusammen mit der unveränderten y-Koordinate an die Callback-Funktion übergeben.
     *
     * Wenn die Richtung "LEFT" ist, werden die x-Koordinaten um eins verringert und auf die Breite
     * des Spielfelds (config.width) modulo gerechnet.
     * Das Ergebnis wird zusammen mit der unveränderten y-Koordinate an die Callback-Funktion übergeben.
     *
     * Wenn die Richtung "TOP" ist, bleibt die x-Koordinate unverändert,
     * während die y-Koordinaten um eins verringert und auf die Höhe des Spielfelds (config.height)
     * modulo gerechnet werden.
     * Das Ergebnis wird zusammen mit der unveränderten x-Koordinate an die Callback-Funktion übergeben.
     *
     * Wenn die Richtung "DOWN" ist, bleibt die x-Koordinate unverändert, während die y-Koordinaten
     * um eins erhöht und auf die Höhe des Spielfelds (config.height) modulo gerechnet werden.
     * Das Ergebnis wird zusammen mit der unveränderten x-Koordinate an die Callback-Funktion übergeben.
     * @param x des Ursprünglichen Felds
     * @param y des Ursprünglichen Felds
     * @param direction richtung
     * @param callback aktion
     * @return _
     */
    public boolean executeActionAt(int x, int y, Direction direction, BiFunction<Integer, Integer, Boolean> callback) {
        switch (direction) {
            case RIGHT -> {
                return callback.apply((x + 1) % this.config.width, y);
            }
            case LEFT -> {
                return callback.apply((x - 1 + this.config.width) % this.config.width , y);
            }
            case TOP -> {
                return callback.apply(x, (y - 1 + this.config.height) % this.config.height);
            }
            case DOWN -> {
                return callback.apply(x, (y + 1 + this.config.height) % this.config.height);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    /**
     * Die Methode "chooseRandomSpecies" wählt eine zufällige Spezies aus einem Array von Spezies (species) aus.
     * @param species alle Spezies
     * @return Spezies, die gewählt wurde
     */
    public Species chooseRandomSpecies(Species[] species) {
        int rand = this.randomSpeciesGenerator.nextInt(species.length);
        return species[rand];
    }

    /**
     * hohlt die Spezies(x = x und y = y) aus dem Bord
     * @param x _
     * @param y _
     * @return spezies
     */
    public Species getSpeciesAtCell(int x, int y) {
        return this.speziesBoard[x][y];
    }
}
