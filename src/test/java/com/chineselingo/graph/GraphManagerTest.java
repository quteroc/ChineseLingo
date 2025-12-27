package com.chineselingo.graph;

import com.chineselingo.data.CharIdMapper;
import com.chineselingo.data.DataManager;
import com.chineselingo.data.StaticData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class GraphManagerTest {

    private GraphManager graphManager;
    private CharIdMapper charIdMapper;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        // Load test fixtures
        Path fixturesDir = Paths.get(getClass().getResource("/fixtures").toURI());
        DataManager dataManager = new DataManager(fixturesDir);
        StaticData staticData = dataManager.loadData();
        
        graphManager = new GraphManager(staticData);
        charIdMapper = staticData.getCharIdMapper();
    }

    @Test
    void testConstructorThrowsOnNullStaticData() {
        assertThrows(IllegalArgumentException.class, () -> new GraphManager(null));
    }

    @Test
    void testGetCompoundsForComponent() {
        // Test that 木 (wood) is a component of 林 (forest) and 森 (deep forest)
        int woodId = charIdMapper.getId("木");
        int forestId = charIdMapper.getId("林");
        int deepForestId = charIdMapper.getId("森");

        IntArrayList compounds = graphManager.getCompoundsForComponent(woodId);
        assertNotNull(compounds, "木 should have compounds");
        assertTrue(compounds.contains(forestId), "林 should contain 木");
        assertTrue(compounds.contains(deepForestId), "森 should contain 木");
    }

    @Test
    void testGetCompoundsForComponentReturnsDefensiveCopy() {
        int woodId = charIdMapper.getId("木");
        
        IntArrayList compounds1 = graphManager.getCompoundsForComponent(woodId);
        IntArrayList compounds2 = graphManager.getCompoundsForComponent(woodId);
        
        assertNotNull(compounds1);
        assertNotNull(compounds2);
        assertNotSame(compounds1, compounds2, "Should return defensive copies");
        assertEquals(compounds1, compounds2, "Copies should have same content");
    }

    @Test
    void testGetComponentsForCompound() {
        // Test that 林 (forest) is made of 木木 (two woods)
        int woodId = charIdMapper.getId("木");
        int forestId = charIdMapper.getId("林");

        IntArrayList components = graphManager.getComponentsForCompound(forestId);
        assertNotNull(components, "林 should have components");
        assertTrue(components.contains(woodId), "林 should be composed of 木");
    }

    @Test
    void testGetComponentsForCompoundReturnsDefensiveCopy() {
        int forestId = charIdMapper.getId("林");
        
        IntArrayList components1 = graphManager.getComponentsForCompound(forestId);
        IntArrayList components2 = graphManager.getComponentsForCompound(forestId);
        
        assertNotNull(components1);
        assertNotNull(components2);
        assertNotSame(components1, components2, "Should return defensive copies");
        assertEquals(components1, components2, "Copies should have same content");
    }

    @Test
    void testGetCompoundsForNonExistentComponent() {
        // Use a very high ID that doesn't exist
        IntArrayList compounds = graphManager.getCompoundsForComponent(999999);
        assertNull(compounds, "Non-existent component should return null");
    }

    @Test
    void testGetComponentsForNonExistentCompound() {
        // Use a very high ID that doesn't exist
        IntArrayList components = graphManager.getComponentsForCompound(999999);
        assertNull(components, "Non-existent compound should return null");
    }

    @Test
    void testGetFrequency() {
        // Test that we can retrieve frequencies
        int woodId = charIdMapper.getId("木");
        int forestId = charIdMapper.getId("林");
        
        int woodFreq = graphManager.getFrequency(woodId);
        int forestFreq = graphManager.getFrequency(forestId);
        
        assertTrue(woodFreq > 0, "木 should have a frequency");
        assertTrue(forestFreq > 0, "林 should have a frequency");
        assertTrue(woodFreq > forestFreq, "木 should be more frequent than 林 (based on fixtures)");
    }

    @Test
    void testGetFrequencyForNonExistent() {
        // Non-existent character should return 0
        int freq = graphManager.getFrequency(999999);
        assertEquals(0, freq, "Non-existent character should have frequency 0");
    }

    @Test
    void testGetStaticData() {
        StaticData staticData = graphManager.getStaticData();
        assertNotNull(staticData, "Should return StaticData");
    }
}
