package simulation.core;

public class SimulationContext {

    public int maxIterations = 100;

    public String seed = "42";

    public int width = 100;
    public int height = 100;

    public int filledFields = 100;

    public SpeciesContext[] spezies = new SpeciesContext[] {
            new SpeciesContext( "rabbit", "#ff0000", 1, 1, 1, new String[]{"dog"}, new String[]{"fox"}),
            new SpeciesContext( "dog", "#ff0000", 1, 1, 1, new String[]{"fox"}, new String[]{"rabbit"}),
            new SpeciesContext( "fox", "#ff0000", 1, 1, 1, new String[]{"rabbit"}, new String[]{"fox"})
    };
}
