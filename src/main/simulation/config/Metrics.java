package main.simulation.config;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Metrics {
    public String metricsCsvFileName;
    public String path;
    public List<String> useFields;
}
