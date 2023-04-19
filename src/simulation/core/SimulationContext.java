package simulation.core;

public class SimulationContext {

    public int maxIterations = 100;

    public long seed = 42;

    public int width = 100;
    public int height = 100;

    public int filledFields = 100;

    public int probabilityOfReproduction = 1;
    public int probabilityOfSelection = 1;
    public int probabilityOfMovement = 1;

    public SpeciesContext[] spezies = new SpeciesContext[] {
            new SpeciesContext( "rabbit", "#ff0000",  new String[]{"dog"}, new String[]{"fox"}),
            new SpeciesContext( "dog", "#ff0000",  new String[]{"fox"}, new String[]{"rabbit"}),
            new SpeciesContext( "fox", "#ff0000",  new String[]{"rabbit"}, new String[]{"fox"})
    };
}
