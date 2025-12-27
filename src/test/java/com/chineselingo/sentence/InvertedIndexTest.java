package com.chineselingo.sentence;

import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {

    @Test
    void testEmptyIndex() {
        InvertedIndex index = new InvertedIndex();
        assertEquals(0, index.size());
        
        RoaringBitmap result = index.getSentencesForChar(999);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddSingleEntry() {
        InvertedIndex index = new InvertedIndex();
        
        index.addEntry(1, 10);
        
        assertEquals(1, index.size());
        
        RoaringBitmap result = index.getSentencesForChar(1);
        assertEquals(1, result.getCardinality());
        assertTrue(result.contains(10));
    }

    @Test
    void testAddMultipleEntriesForSameChar() {
        InvertedIndex index = new InvertedIndex();
        
        index.addEntry(1, 10);
        index.addEntry(1, 20);
        index.addEntry(1, 30);
        
        assertEquals(1, index.size(), "Should still be 1 unique character");
        
        RoaringBitmap result = index.getSentencesForChar(1);
        assertEquals(3, result.getCardinality());
        assertTrue(result.contains(10));
        assertTrue(result.contains(20));
        assertTrue(result.contains(30));
    }

    @Test
    void testAddEntriesForMultipleChars() {
        InvertedIndex index = new InvertedIndex();
        
        index.addEntry(1, 10);
        index.addEntry(1, 20);
        index.addEntry(2, 10);
        index.addEntry(2, 30);
        index.addEntry(3, 40);
        
        assertEquals(3, index.size());
        
        RoaringBitmap result1 = index.getSentencesForChar(1);
        assertEquals(2, result1.getCardinality());
        assertTrue(result1.contains(10));
        assertTrue(result1.contains(20));
        
        RoaringBitmap result2 = index.getSentencesForChar(2);
        assertEquals(2, result2.getCardinality());
        assertTrue(result2.contains(10));
        assertTrue(result2.contains(30));
        
        RoaringBitmap result3 = index.getSentencesForChar(3);
        assertEquals(1, result3.getCardinality());
        assertTrue(result3.contains(40));
    }

    @Test
    void testNonExistentChar() {
        InvertedIndex index = new InvertedIndex();
        index.addEntry(1, 10);
        
        RoaringBitmap result = index.getSentencesForChar(999);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
