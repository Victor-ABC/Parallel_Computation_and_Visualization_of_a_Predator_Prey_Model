package simulation.core.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SpeciesContext {

    public String name;
    public String color;
    private ArrayList<String> kills;
    
    public boolean isEating(SpeciesContext speciesContext) {
        return this.kills.contains(speciesContext.name);
    }
}
