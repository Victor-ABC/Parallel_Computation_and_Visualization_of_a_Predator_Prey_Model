package simulation.core;

import java.util.ArrayList;
import java.util.List;

public class SpeciesContext {
    public final String color;
    private final ArrayList<String> kills;
    public final String name;

    public SpeciesContext(
            String name,
            String color,
            String[] kills
    ) {
        this.name = name;
        this.color = color;
        this.kills = new ArrayList<>(List.of(kills));
    }

    public boolean isEating(SpeciesContext speciesContext) {
        return this.kills.contains(speciesContext.name);
    }
}
