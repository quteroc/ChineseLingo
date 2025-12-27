package com.chineselingo.parser;

import com.chineselingo.data.CharIdMapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class IDSParserTest {

    @Test
    void testParseIDS() throws IOException, URISyntaxException {
        Path idsPath = Paths.get(getClass().getResource("/fixtures/ids.txt").toURI());
        
        CharIdMapper mapper = new CharIdMapper();
        Int2ObjectOpenHashMap<IntArrayList> componentToCompounds = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<IntArrayList> compoundToComponents = new Int2ObjectOpenHashMap<>();
        
        IDSParser parser = new IDSParser();
        parser.parse(idsPath, mapper, componentToCompounds, compoundToComponents);
        
        // Verify characters were mapped
        assertTrue(mapper.size() > 0, "Should have mapped characters");
        
        // Verify relationships were extracted
        assertTrue(componentToCompounds.size() > 0, "Should have component-to-compound mappings");
        assertTrue(compoundToComponents.size() > 0, "Should have compound-to-component mappings");
        
        // Test specific relationships
        int woodId = mapper.getId("木");
        int forestId = mapper.getId("林");
        int denseForestId = mapper.getId("森");
        
        // 林 should contain 木 as component
        IntArrayList forestComponents = compoundToComponents.get(forestId);
        assertNotNull(forestComponents, "林 should have components");
        assertTrue(forestComponents.contains(woodId), "林 should contain 木 as component");
        
        // 木 should be listed as component in 林
        IntArrayList woodCompounds = componentToCompounds.get(woodId);
        assertNotNull(woodCompounds, "木 should be a component in other characters");
        assertTrue(woodCompounds.contains(forestId), "林 should be listed under 木 compounds");
        
        // 森 should contain both 木 and 林 as components
        IntArrayList denseForestComponents = compoundToComponents.get(denseForestId);
        assertNotNull(denseForestComponents, "森 should have components");
        assertTrue(denseForestComponents.contains(woodId), "森 should contain 木");
        assertTrue(denseForestComponents.contains(forestId), "森 should contain 林");
    }

    @Test
    void testSkipComments() throws IOException, URISyntaxException {
        Path idsPath = Paths.get(getClass().getResource("/fixtures/ids.txt").toURI());
        
        CharIdMapper mapper = new CharIdMapper();
        Int2ObjectOpenHashMap<IntArrayList> componentToCompounds = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<IntArrayList> compoundToComponents = new Int2ObjectOpenHashMap<>();
        
        IDSParser parser = new IDSParser();
        parser.parse(idsPath, mapper, componentToCompounds, compoundToComponents);
        
        // The fixture has 4 entries and 1 comment line
        // All 4 entries produce compound-to-component mappings (including 一 -> 一)
        assertEquals(4, compoundToComponents.size(), "Should have parsed 4 compound decompositions");
    }
}
