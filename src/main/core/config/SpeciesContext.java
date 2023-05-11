package main.core.config;

import java.util.HashSet;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SpeciesContext {

    public String name;
    public String color;
    private HashSet<String> kills;

    public boolean isEating(SpeciesContext speciesContext) {
        return this.kills.contains(speciesContext.name);
    }
}
