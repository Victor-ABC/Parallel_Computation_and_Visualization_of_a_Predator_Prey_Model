package main.core.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SimulationConfig {

    public int width;
    public int height;
    public int maxIterations;
    public long seed;
    public int filledFields;

    public int probabilityOfReproduction;
    public int probabilityOfSelection;
    public int probabilityOfMovement;

    public SpeciesContext[] species;
}
