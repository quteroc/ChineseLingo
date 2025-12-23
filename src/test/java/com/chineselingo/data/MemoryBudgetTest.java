package com.chineselingo.data;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class MemoryBudgetTest {

    private static final long MAX_MEMORY_MB = 300;

    @Test
    void testMemoryUsageWithinBudget() throws IOException, URISyntaxException {
        // Get memory before loading
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // Suggest garbage collection before measurement
        Thread.yield();
        
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Load data from fixtures
        Path fixturesDir = Paths.get(getClass().getResource("/fixtures").toURI());
        DataManager dataManager = new DataManager(fixturesDir);
        StaticData data = dataManager.loadData();
        
        // Get memory after loading
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        long memoryUsedMB = memoryUsed / (1024 * 1024);
        
        System.out.println("Memory usage statistics:");
        System.out.println("  Before: " + (memoryBefore / 1024 / 1024) + " MB");
        System.out.println("  After: " + (memoryAfter / 1024 / 1024) + " MB");
        System.out.println("  Used: " + memoryUsedMB + " MB");
        System.out.println("  Characters mapped: " + data.getCharIdMapper().size());
        System.out.println("  Definitions: " + data.getDefinitions().size());
        System.out.println("  Frequencies: " + data.getFrequencies().size());
        System.out.println("  Component relationships: " + data.getComponentToCompounds().size());
        
        // With small fixture files, memory usage should be very low
        // Set a generous threshold to avoid flakiness
        assertTrue(memoryUsedMB < MAX_MEMORY_MB, 
                  String.format("Memory usage (%d MB) should be less than %d MB", 
                               memoryUsedMB, MAX_MEMORY_MB));
        
        // Verify data was actually loaded
        assertNotNull(data);
        assertTrue(data.getCharIdMapper().size() > 0, "Should have loaded characters");
    }

    @Test
    void testDataStructuresAreEfficient() throws IOException, URISyntaxException {
        Path fixturesDir = Paths.get(getClass().getResource("/fixtures").toURI());
        DataManager dataManager = new DataManager(fixturesDir);
        StaticData data = dataManager.loadData();
        
        // Verify we're using efficient primitive collections
        assertNotNull(data.getDefinitions(), "Definitions map should exist");
        assertNotNull(data.getFrequencies(), "Frequencies map should exist");
        assertNotNull(data.getComponentToCompounds(), "Component-to-compounds map should exist");
        assertNotNull(data.getCompoundToComponents(), "Compound-to-components map should exist");
        
        // Verify data structures are using fastutil types (they are)
        assertEquals("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap", 
                    data.getDefinitions().getClass().getName(),
                    "Should use fastutil Int2ObjectOpenHashMap for definitions");
        
        assertEquals("it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap", 
                    data.getFrequencies().getClass().getName(),
                    "Should use fastutil Int2IntOpenHashMap for frequencies");
    }
}
