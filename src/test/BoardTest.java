package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import main.simulation.core.Board;
import main.simulation.config.Config;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class BoardTest {
    Board board;
    Config config;
    String jsonString = """
            {
               "metrics": {
                 "metricsCsvFileName": "first_try",
                 "path" : "./metrics/",
                 "useFields": [
                   "mode",
                   "numberOfThreads",
                   "width",
                   "height",
                   "maxIterations",
                   "filledFieldsInPercent"
                 ]
               },
               "mode": "parallel_with_zones",
               "numberOfThreads": 2,
               "width": 1000,
               "height": 1000,
               "maxIterations": 100,
               "filledFieldsInPercent": 80,
               "isInitialOrderRandom" : false,
               "probabilityOfReproduction": 1,
               "probabilityOfSelection": 1,
               "probabilityOfMovement": 1,
               "species": [
                 {
                   "name": "Rot",
                   "color": "#FF0000",
                   "kills": [
                     "Gelb",
                     "Blau"
                   ]
                 },
                 {
                   "name": "Gelb",
                   "color": "#FFFF00",
                   "kills" : [
                     "Blau",
                     "Orange"
                   ]
                 },
                 {
                   "name": "Blau",
                   "color" : "#0000FF",
                   "kills" : [
                     "Orange",
                     "Gruen"
                   ]
                 },
                 {
                   "name": "Orange",
                   "color": "#FF8000",
                   "kills": [
                     "Gruen",
                     "Rot"
                   ]
                 },
                 {
                   "name": "Gruen",
                   "color": "#00FF00",
                   "kills": [
                     "Rot",
                     "Gelb"
                   ]
                 }
               ]
             }
            """;
    @BeforeAll
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            config = mapper.readValue(jsonString, Config.class);
            board = new Board(config);
            assertNotNull(config);
            assertNotNull(board);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void shouldWork_differentSpecies() {
        Set<String> distinctSpecies = new HashSet<>();
        for(int row = 0; row < config.width ; row++) {
            for(int col = 0 ; col < config.height; col++) {
                if (board.getSpeciesAtCell(row, col) != null) {
                    distinctSpecies.add(board.getSpeciesAtCell(row, col).name);
                }
            }
        }
        assertEquals(distinctSpecies.size(), config.species.length);
    }

}