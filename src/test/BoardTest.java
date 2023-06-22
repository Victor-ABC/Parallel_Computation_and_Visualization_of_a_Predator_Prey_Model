package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import main.core.Board;
import main.core.config.Config;
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
            config = mapper.readValue(jsonString, Config.class);
            board = new Board(config);
            assertNotNull(config);
            assertNotNull(board);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    void shouldWork_100PercentFullGameField() {
        for(int row = 0; row < config.width ; row++) {
            for(int col = 0 ; col < config.height; col++) {
                assertNotNull(board.getSpeciesAtCell(row, col));
            }
        }
    }

    @Test
    void a() {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("_dd_MM_yy");
        String formattedDateTime = dateFormat.format(new Date(currentTimeMillis));

        System.out.println("Formatted date: " + formattedDateTime);
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