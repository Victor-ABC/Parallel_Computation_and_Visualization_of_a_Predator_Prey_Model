package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import main.core.Board;
import main.core.config.SimulationConfig;
import main.core.config.SpeciesContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class BoardTest {
    Board board;
    SimulationConfig simulationConfig;
    String jsonString = """
            {
              "width": 3,
              "height": 3,
              "maxIterations": 1,
              "seed": 50,
              "filledFields": 100,
              "probabilityOfReproduction": 1,
              "probabilityOfSelection": 1,
              "probabilityOfMovement": 1,
              "species": [
                {
                  "name": "Affen",
                  "color": "#FF0000",
                  "kills": [
                    "Hund"
                  ]
                }, 
                {
                  "name": "Katze",
                  "color": "#00FF00",
                  "kills" : [
                    "Affen"
                  ]
                },
                {
                  "name": "Hund",
                  "color" : "#0000FF",
                  "kills" : [
                    "Katze"
                  ]
                }
              ]
            }
            """;
    @BeforeAll
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            simulationConfig = mapper.readValue(jsonString, SimulationConfig.class);
            board = new Board(simulationConfig);
            assertNotNull(simulationConfig);
            assertNotNull(board);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    void shouldWork_100PercentFullGameField() {
        for(int row = 0; row < simulationConfig.width ; row++) {
            for(int col = 0 ; col < simulationConfig.height; col++) {
                assertNotNull(board.getSpeciesAtCell(row, col));
            }
        }
    }

    @Test
    void shouldWork_differentSpecies() {
        Set<String> distinctSpecies = new HashSet<>();
        for(int row = 0; row < simulationConfig.width ; row++) {
            for(int col = 0 ; col < simulationConfig.height; col++) {
                if (board.getSpeciesAtCell(row, col) != null) {
                    distinctSpecies.add(board.getSpeciesAtCell(row, col).name);
                }
            }
        }
        assertEquals(distinctSpecies.size(), simulationConfig.species.length);
    }

}