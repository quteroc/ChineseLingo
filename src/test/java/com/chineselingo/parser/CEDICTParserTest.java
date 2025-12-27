package com.chineselingo.parser;

import com.chineselingo.data.CharIdMapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class CEDICTParserTest {

    @Test
    void testParseCEDICT() throws IOException, URISyntaxException {
        Path cedictPath = Paths.get(getClass().getResource("/fixtures/cedict_ts.u8").toURI());
        
        CharIdMapper mapper = new CharIdMapper();
        Int2ObjectOpenHashMap<String> definitions = new Int2ObjectOpenHashMap<>();
        
        CEDICTParser parser = new CEDICTParser();
        parser.parse(cedictPath, mapper, definitions);
        
        // Verify characters were mapped
        assertTrue(mapper.size() > 0, "Should have mapped characters");
        
        // Verify definitions were extracted
        assertTrue(definitions.size() > 0, "Should have definitions");
        
        // Test specific characters
        int woodId = mapper.getId("木");
        int forestId = mapper.getId("林");
        
        assertNotNull(definitions.get(woodId), "木 should have a definition");
        assertNotNull(definitions.get(forestId), "林 should have a definition");
        
        String woodDef = definitions.get(woodId);
        assertTrue(woodDef.contains("tree") || woodDef.contains("wood"), 
                  "木 definition should contain 'tree' or 'wood': " + woodDef);
        
        String forestDef = definitions.get(forestId);
        assertTrue(forestDef.contains("forest"), 
                  "林 definition should contain 'forest': " + forestDef);
    }

    @Test
    void testSkipComments() throws IOException, URISyntaxException {
        Path cedictPath = Paths.get(getClass().getResource("/fixtures/cedict_ts.u8").toURI());
        
        CharIdMapper mapper = new CharIdMapper();
        Int2ObjectOpenHashMap<String> definitions = new Int2ObjectOpenHashMap<>();
        
        CEDICTParser parser = new CEDICTParser();
        parser.parse(cedictPath, mapper, definitions);
        
        // Verify that we got actual entries, not comment lines
        // The fixture has 5 entries and 2 comment lines
        assertEquals(5, definitions.size(), "Should have parsed 5 character definitions");
    }
}
