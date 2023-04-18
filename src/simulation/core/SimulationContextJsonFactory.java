package simulation.core;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;

public class SimulationContextJsonFactory {

    public SimulationContextJsonFactory() {

    }

    public SimulationContext CreateSimulationContextFromJsonFile(String path) {
        JSONParser parser = new JSONParser();

        try {
            JSONObject json = (JSONObject) parser.parse(new FileReader(path));

            SimulationContext simulationContext = new SimulationContext();
            simulationContext.width = ((Long) json.get("width")).intValue();
            simulationContext.height = ((Long) json.get("height")).intValue();
            simulationContext.maxIterations = ((Long)json.get("max_iterations")).intValue();
            simulationContext.seed = json.get("seed").toString();

            var species = (JSONArray) json.get("species");

            ArrayList<SpeciesContext> speciesContexts = new ArrayList<SpeciesContext>();

            for (Object specie: species) {
                if ( specie instanceof JSONObject ) {
                    speciesContexts.add(this.getSpeciesContextFromJsonObject((JSONObject) specie));
                }
            }

            simulationContext.spezies = speciesContexts.toArray(simulationContext.spezies);
            return simulationContext;
        } catch (Exception e) {
            throw new RuntimeException("Json file could not be parsed: " + e.toString());
        }
    }

    private SpeciesContext getSpeciesContextFromJsonObject(JSONObject species) {
        return new SpeciesContext(
                species.get("name").toString(),
                species.get("color").toString(),
                ((Long) species.get("probability_of_reproduction")).intValue(),
                ((Long) species.get("probability_of_selection")).intValue(),
                ((Long) species.get("probability_of_movement")).intValue(),
                (String[]) ((JSONArray) species.get("kills")).stream().toArray(String[]::new),
                (String[]) ((JSONArray) species.get("is_killed_by")).stream().toArray(String[]::new)
        );
    }
}
