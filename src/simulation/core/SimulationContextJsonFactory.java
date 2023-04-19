package simulation.core;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;

public class SimulationContextJsonFactory {

    public SimulationContextJsonFactory() {

    }

    public SimulationConfig CreateSimulationContextFromJsonFile(String path) {
        JSONParser parser = new JSONParser();

        try {
            JSONObject json = (JSONObject) parser.parse(new FileReader(path));

            SimulationConfig simulationConfig = new SimulationConfig();
            simulationConfig.width = ((Long) json.get("width")).intValue();
            simulationConfig.height = ((Long) json.get("height")).intValue();
            simulationConfig.maxIterations = ((Long)json.get("max_iterations")).intValue();
            simulationConfig.seed = (Long) json.get("seed");
            simulationConfig.probabilityOfReproduction = ((Long) json.get("probability_of_reproduction")).intValue();
            simulationConfig.probabilityOfSelection = ((Long) json.get("probability_of_selection")).intValue();
            simulationConfig.probabilityOfMovement = ((Long) json.get("probability_of_movement")).intValue();

            var species = (JSONArray) json.get("species");

            ArrayList<SpeciesContext> speciesContexts = new ArrayList<SpeciesContext>();

            for (Object specie: species) {
                if ( specie instanceof JSONObject ) {
                    speciesContexts.add(this.getSpeciesContextFromJsonObject((JSONObject) specie));
                }
            }

            simulationConfig.spezies = speciesContexts.toArray(simulationConfig.spezies);
            return simulationConfig;
        } catch (Exception e) {
            throw new RuntimeException("Json file could not be parsed: " + e.toString());
        }
    }

    private SpeciesContext getSpeciesContextFromJsonObject(JSONObject species) {
        return new SpeciesContext(
                species.get("name").toString(),
                species.get("color").toString(),
                (String[]) ((JSONArray) species.get("kills")).stream().toArray(String[]::new)
        );
    }
}
