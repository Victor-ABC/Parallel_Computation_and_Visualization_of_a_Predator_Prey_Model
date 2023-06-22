package main.core.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Config {

    public String mode;
    public int numberOfThreads;
    public int width;
    public int height;
    public int maxIterations;
    public long seed;
    public int filledFieldsInPercent;

    public int probabilityOfReproduction;
    public int probabilityOfSelection;
    public int probabilityOfMovement;

    public Species[] species;
}