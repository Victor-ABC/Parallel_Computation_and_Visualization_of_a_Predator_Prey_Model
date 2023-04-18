package simulation.core;

import java.util.Random;

public class RandomWrapper {
    public static Random random = null;

    public static void createRandom(long seed) {
        if(random instanceof Random) {
            throw new RuntimeException("Already inilized");
        }

        random = new Random(seed);
    }

    public static Random getRandom()  {
        if(!(random instanceof Random)) {
            throw new RuntimeException("Not inilized");
        }

        return random;
    }
}
