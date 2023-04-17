package simulation.core;

public class Spezies {

    public SpeziesContext context;
    private int xAches;
    private int yAches;

    public Spezies(SpeziesContext context, int xAches, int yAches) {
        this.context = context;
        this.xAches = xAches;
        this.yAches = yAches;
    }

    public Spezies breedAtCell(int xAches, int yAches) {
        return new Spezies(this.context, xAches, yAches);
    }
}
