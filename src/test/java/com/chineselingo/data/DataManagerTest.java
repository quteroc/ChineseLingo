package com.chineselingo.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerTest {

    @Test
    void testLoadDataFromFixtures() throws IOException, URISyntaxException {
        // Get the fixtures directory from test resources
        Path fixturesDir = Paths.get(getClass().getResource("/fixtures").toURI());
        
        DataManager dataManager = new DataManager(fixturesDir);
        StaticData data = dataManager.loadData();

        assertNotNull(data);
        assertNotNull(data.getCharIdMapper());
        
        // Verify character mapper has loaded characters
        assertTrue(data.getCharIdMapper().size() > 0, "CharIdMapper should contain characters");
        
        // Verify definitions were loaded
        assertTrue(data.getDefinitions().size() > 0, "Definitions should be loaded");
        
        // Verify frequencies were loaded
        assertTrue(data.getFrequencies().size() > 0, "Frequencies should be loaded");
        
        // Verify component relationships were loaded
        assertTrue(data.getComponentToCompounds().size() > 0, "Component relationships should be loaded");
    }

    @Test
    void testLoadDataWithSpecificCharacters() throws IOException, URISyntaxException {
        Path fixturesDir = Paths.get(getClass().getResource("/fixtures").toURI());
        
        DataManager dataManager = new DataManager(fixturesDir);
        StaticData data = dataManager.loadData();

        CharIdMapper mapper = data.getCharIdMapper();
        
        // Test that specific characters from fixtures are present
        int woodId = mapper.getId("木");
        int forestId = mapper.getId("林");
        
        // Verify definitions
        assertNotNull(data.getDefinition(woodId), "木 should have a definition");
        assertTrue(data.getDefinition(woodId).contains("tree") || 
                  data.getDefinition(woodId).contains("wood"), 
                  "木 definition should contain 'tree' or 'wood'");
        
        assertNotNull(data.getDefinition(forestId), "林 should have a definition");
        assertTrue(data.getDefinition(forestId).contains("forest"), 
                  "林 definition should contain 'forest'");
        
        // Verify frequencies
        assertTrue(data.getFrequency(woodId) > 0, "木 should have a frequency");
        assertTrue(data.getFrequency(forestId) > 0, "林 should have a frequency");
        
        // Verify component relationships
        // 林 should contain 木 as component
        var woodCompounds = data.getCompoundsContaining(woodId);
        assertNotNull(woodCompounds, "木 should be a component in other characters");
        assertTrue(woodCompounds.contains(forestId), "林 should contain 木 as a component");
    }

    @Test
    void testLoadDataFromNonExistentDirectory(@TempDir Path tempDir) {
        Path nonExistent = tempDir.resolve("nonexistent");
        DataManager dataManager = new DataManager(nonExistent);
        
        assertThrows(IOException.class, () -> dataManager.loadData(), 
                    "Should throw IOException for non-existent directory");
    }
}
