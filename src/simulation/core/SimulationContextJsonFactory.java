package simulation.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import simulation.core.config.SimulationConfig;
import simulation.core.config.SpeciesContext;

public class SimulationContextJsonFactory {

    public SimulationContextJsonFactory() {

    }

    public SimulationConfig CreateSimulationContextFromJsonFile(String path) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File f = new File(path);
            return mapper.readValue(f, SimulationConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Json file could not be parsed: " + e);
        }
    }
}
