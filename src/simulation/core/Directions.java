package simulation.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Directions {
    TOP,
    RIGHT,
    DOWN,
    LEFT;

    private static final List<Directions> values =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = values.size();
    private static final Random RANDOM = new Random();

    public static Directions randomLetter()  {
        return values.get(RANDOM.nextInt(SIZE));
    }
}
