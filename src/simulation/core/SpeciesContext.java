package simulation.core;

import java.util.ArrayList;
import java.util.HashMap;

public class SpeciesContext {

    private static HashMap<String, SpeciesContext> SpeciesContextInstances = new HashMap<String, SpeciesContext>();
    public final String color;
    private String[] killedBy;
    private String[] kills;
    public final String name;
    public final int probabilityOfReproduction;
    public final int probabilityOfSelection;
    public final int probabilityOfMovement;

    public SpeciesContext(
            String name,
            String color,
            int probabilityOfReproduction,
            int probabilityOfSelection,
            int probabilityOfMovement,
            String[] killedBy,
            String[] kills
    ) {
        this.name = name;
        this.probabilityOfReproduction = probabilityOfReproduction;
        this.probabilityOfSelection = probabilityOfSelection;
        this.probabilityOfMovement = probabilityOfMovement;
        this.color = color;
        this.killedBy = killedBy;
        this.kills = kills;

        addToSingleton(name, this);
    }

    public static void addToSingleton(String name, SpeciesContext speciesContext) {
        SpeciesContextInstances.put(name, speciesContext);
    }

    public static SpeciesContext getSpeciesContext(String name) {
        return SpeciesContextInstances.get(name);
    }


    public boolean isEating(SpeciesContext speciesContext) {
        return this.getKills().contains(speciesContext);
    }

    public ArrayList<SpeciesContext> getKills() {
        ArrayList<SpeciesContext> killedContexts = new ArrayList<SpeciesContext>();

        for (String kill : kills) {
            killedContexts.add(getSpeciesContext(kill));
        }

        return killedContexts;
    }

    public ArrayList<SpeciesContext> getKilledBy() {
        ArrayList<SpeciesContext> killedByContexts = new ArrayList<SpeciesContext>();

        for (String killBy : killedBy) {
            killedByContexts.add(getSpeciesContext(killBy));
        }

        return killedByContexts;
    }
}
