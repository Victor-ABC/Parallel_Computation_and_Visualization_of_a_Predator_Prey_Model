package main.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import main.core.config.Config;

public abstract class BoardParallel extends Board {

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
