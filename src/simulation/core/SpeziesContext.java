package simulation.core;

public class SpeziesContext {

    public final int id;
    public final String name;
    public final double growthRate;
    public final double decayRate;
    public final double moveRate;

    public SpeziesContext(int id, String name, double growthRate, double decayRate, double moveRate) {
        this.id = id;
        this.name = name;
        this.growthRate = growthRate;
        this.decayRate = decayRate;
        this.moveRate = moveRate;
    }
}
