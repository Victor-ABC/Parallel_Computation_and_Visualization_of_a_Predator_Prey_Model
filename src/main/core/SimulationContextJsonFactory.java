package main.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import main.core.config.SimulationConfig;

public class SimulationContextJsonFactory {

    public SimulationContextJsonFactory() {

    }

    public SimulationConfig CreateSimulationContextFromJsonFile(String path) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File(path), SimulationConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Json file could not be parsed: " + e);
        }
    }
}
