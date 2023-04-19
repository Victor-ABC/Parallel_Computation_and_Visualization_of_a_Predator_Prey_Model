package simulation.core;

public class SpeciesOnField {

    public SpeciesContext context;

    public int power = 0;
    private int xAches;
    private int yAches;

    public SpeciesOnField(SpeciesContext context, int xAches, int yAches) {
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
