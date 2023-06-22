package main.core.config;

import java.util.HashSet;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Species {

    public String name;
    public String color;
    private HashSet<String> kills;

    public boolean isEating(Species species) {
        return this.kills.contains(species.name);
    }
}
