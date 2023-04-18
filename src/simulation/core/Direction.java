package simulation.core;

import java.util.*;

public enum Direction {
    TOP,
    RIGHT,
    DOWN,
    LEFT;

    private static final List<Direction> values =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = values.size();
    private static final Random RANDOM = new Random();

    public static Direction randomLetter()  {
        return values.get(RANDOM.nextInt(SIZE));
    }

    public static List<Direction> getRandomDirections() {
        var list = new ArrayList<>(values);
        Collections.shuffle(list);
        return list;
    }
}
