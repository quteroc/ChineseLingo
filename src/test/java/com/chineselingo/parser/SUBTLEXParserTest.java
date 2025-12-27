package com.chineselingo.parser;

import com.chineselingo.data.CharIdMapper;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class SUBTLEXParserTest {

    @Test
    void testParseSUBTLEX() throws IOException, URISyntaxException {
        Path subtlexPath = Paths.get(getClass().getResource("/fixtures/subtlex.txt").toURI());
        
        CharIdMapper mapper = new CharIdMapper();
        Int2IntOpenHashMap frequencies = new Int2IntOpenHashMap();
        
        SUBTLEXParser parser = new SUBTLEXParser();
        parser.parse(subtlexPath, mapper, frequencies);
        
        // Verify characters were mapped
        assertTrue(mapper.size() > 0, "Should have mapped characters");
        
        // Verify frequencies were extracted
        assertTrue(frequencies.size() > 0, "Should have frequencies");
        
        // Test specific characters
        int woodId = mapper.getId("木");
        int forestId = mapper.getId("林");
        
        assertTrue(frequencies.containsKey(woodId), "木 should have a frequency");
        assertTrue(frequencies.containsKey(forestId), "林 should have a frequency");
        
        assertEquals(50000, frequencies.get(woodId), "木 frequency should match fixture");
        assertEquals(30000, frequencies.get(forestId), "林 frequency should match fixture");
    }

    @Test
    void testSkipHeader() throws IOException, URISyntaxException {
        Path subtlexPath = Paths.get(getClass().getResource("/fixtures/subtlex.txt").toURI());
        
        CharIdMapper mapper = new CharIdMapper();
        Int2IntOpenHashMap frequencies = new Int2IntOpenHashMap();
        
        SUBTLEXParser parser = new SUBTLEXParser();
        parser.parse(subtlexPath, mapper, frequencies);
        
        // The fixture has 6 data rows + 1 header
        // Should have parsed 6 characters (header skipped)
        assertEquals(6, frequencies.size(), "Should have parsed 6 character frequencies");
        
        // Verify header text wasn't treated as a character
        assertFalse(mapper.getReadOnlyCharToIdMap().containsKey("Character"), 
                   "Should not have parsed header as a character");
    }
}
