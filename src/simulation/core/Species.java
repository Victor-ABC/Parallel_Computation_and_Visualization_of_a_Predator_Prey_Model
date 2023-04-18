package simulation.core;

public class Species {

    public SpeciesContext context;

    public int power = 0;
    private int xAches;
    private int yAches;

    public Species(SpeciesContext context, int xAches, int yAches) {
        this.context = context;
        this.xAches = xAches;
        this.yAches = yAches;
    }

    public boolean shouldMove() {
        return true;
    }

    public boolean shouldBreed() {
        return true;
    }

    public boolean isLowerThanBreed() {
        return  true;
    }
}
