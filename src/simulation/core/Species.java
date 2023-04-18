package simulation.core;

public class Species {

    public SpeciesContext context;
    private int xAches;
    private int yAches;

    public Species(SpeciesContext context, int xAches, int yAches) {
        this.context = context;
        this.xAches = xAches;
        this.yAches = yAches;
    }

    public Species breedAtCell(int xAches, int yAches) {
        return new Species(this.context, xAches, yAches);
    }
}
