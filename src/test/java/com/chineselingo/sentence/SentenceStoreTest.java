package com.chineselingo.sentence;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SentenceStoreTest {

    @Test
    void testEmptyStore() {
        SentenceStore store = new SentenceStore();
        assertEquals(0, store.size());
    }

    @Test
    void testAddAndRetrieveSentence() {
        SentenceStore store = new SentenceStore();
        
        IntArrayList tokens = new IntArrayList();
        tokens.add(1);
        tokens.add(2);
        tokens.add(3);
        
        int sentenceId = store.addSentence("ABC", tokens);
        
        assertEquals(0, sentenceId, "First sentence should have ID 0");
        assertEquals(1, store.size());
        assertEquals("ABC", store.text(sentenceId));
        assertArrayEquals(new int[]{1, 2, 3}, store.tokens(sentenceId));
    }

    @Test
    void testMultipleSentences() {
        SentenceStore store = new SentenceStore();
        
        IntArrayList tokens1 = new IntArrayList(new int[]{1, 2});
        IntArrayList tokens2 = new IntArrayList(new int[]{3, 4, 5});
        
        int id1 = store.addSentence("AB", tokens1);
        int id2 = store.addSentence("CDE", tokens2);
        
        assertEquals(0, id1);
        assertEquals(1, id2);
        assertEquals(2, store.size());
        
        assertEquals("AB", store.text(id1));
        assertEquals("CDE", store.text(id2));
        assertArrayEquals(new int[]{1, 2}, store.tokens(id1));
        assertArrayEquals(new int[]{3, 4, 5}, store.tokens(id2));
    }

    @Test
    void testNonExistentSentence() {
        SentenceStore store = new SentenceStore();
        
        assertNull(store.text(999));
        assertNull(store.tokens(999));
    }
}
