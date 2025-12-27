package com.chineselingo.sentence;

import com.chineselingo.user.UserState;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SentenceFilterTest {

    private SentenceStore store;
    private InvertedIndex index;
    private UserState userState;

    @BeforeEach
    void setUp() {
        store = new SentenceStore();
        index = new InvertedIndex();
        userState = new UserState();
    }

    @Test
    void testFindIPlusOneSentencesWithPerfectMatch() {
        // Sentence: "ABC" with IDs [1, 2, 3]
        IntArrayList tokens = new IntArrayList(new int[]{1, 2, 3});
        int sentenceId = store.addSentence("ABC", tokens);
        
        index.addEntry(1, sentenceId);
        index.addEntry(2, sentenceId);
        index.addEntry(3, sentenceId);
        
        // User knows A(1) and B(2), learning C(3)
        userState.markKnown(1);
        userState.markKnown(2);
        
        SentenceFilter filter = new SentenceFilter(store, index, 0.90);
        List<Integer> results = filter.findIPlusOneSentences(3, userState);
        
        // Should find the sentence: 2 known + 1 target = 3/3 = 100% >= 90%
        assertEquals(1, results.size());
        assertEquals(sentenceId, results.get(0));
    }

    @Test
    void testThresholdFiltering() {
        // Sentence: "ABCD" with IDs [1, 2, 3, 4]
        IntArrayList tokens = new IntArrayList(new int[]{1, 2, 3, 4});
        int sentenceId = store.addSentence("ABCD", tokens);
        
        index.addEntry(1, sentenceId);
        index.addEntry(2, sentenceId);
        index.addEntry(3, sentenceId);
        index.addEntry(4, sentenceId);
        
        // User knows A(1), B(2), C(3), learning D(4)
        userState.markKnown(1);
        userState.markKnown(2);
        userState.markKnown(3);
        
        SentenceFilter filter = new SentenceFilter(store, index, 0.90);
        
        // 3 known + 1 target = 4/4 = 100% >= 90% - should pass
        List<Integer> results = filter.findIPlusOneSentences(4, userState);
        assertEquals(1, results.size());
    }

    @Test
    void testBelowThreshold() {
        // Sentence: "ABCD" with IDs [1, 2, 3, 4]
        IntArrayList tokens = new IntArrayList(new int[]{1, 2, 3, 4});
        int sentenceId = store.addSentence("ABCD", tokens);
        
        index.addEntry(1, sentenceId);
        index.addEntry(2, sentenceId);
        index.addEntry(3, sentenceId);
        index.addEntry(4, sentenceId);
        
        // User only knows A(1), learning D(4)
        userState.markKnown(1);
        
        SentenceFilter filter = new SentenceFilter(store, index, 0.90);
        
        // 1 known + 1 target = 2/4 = 50% < 90% - should fail
        List<Integer> results = filter.findIPlusOneSentences(4, userState);
        assertEquals(0, results.size());
    }

    @Test
    void testExactThreshold() {
        // Sentence: "ABCDEFGHIJ" (10 chars) with IDs [1..10]
        IntArrayList tokens = new IntArrayList(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        int sentenceId = store.addSentence("ABCDEFGHIJ", tokens);
        
        for (int i = 1; i <= 10; i++) {
            index.addEntry(i, sentenceId);
        }
        
        // User knows 8 chars, learning the 9th
        // 8 known + 1 target = 9/10 = 90% - should pass at 0.90
        for (int i = 1; i <= 8; i++) {
            userState.markKnown(i);
        }
        
        SentenceFilter filter = new SentenceFilter(store, index, 0.90);
        List<Integer> results = filter.findIPlusOneSentences(9, userState);
        
        assertEquals(1, results.size());
    }

    @Test
    void testTargetCharCountsAsKnown() {
        // Sentence: "ABC" with IDs [1, 2, 3]
        IntArrayList tokens = new IntArrayList(new int[]{1, 2, 3});
        int sentenceId = store.addSentence("ABC", tokens);
        
        index.addEntry(1, sentenceId);
        index.addEntry(2, sentenceId);
        index.addEntry(3, sentenceId);
        
        // User knows only A(1), learning C(3)
        userState.markKnown(1);
        
        SentenceFilter filter = new SentenceFilter(store, index, 0.75);
        
        // 1 known + 1 target = 2/3 = 66.7% < 75% - should fail
        List<Integer> results = filter.findIPlusOneSentences(3, userState, 0.75);
        assertEquals(0, results.size());
        
        // But at 0.66 threshold should pass
        results = filter.findIPlusOneSentences(3, userState, 0.66);
        assertEquals(1, results.size());
    }

    @Test
    void testSortsByShorterFirst() {
        // Add three sentences with target char
        IntArrayList tokens1 = new IntArrayList(new int[]{1, 2, 3, 4, 5}); // length 5
        IntArrayList tokens2 = new IntArrayList(new int[]{1, 2, 3}); // length 3
        IntArrayList tokens3 = new IntArrayList(new int[]{1, 2, 3, 4}); // length 4
        
        int id1 = store.addSentence("ABCDE", tokens1);
        int id2 = store.addSentence("ABC", tokens2);
        int id3 = store.addSentence("ABCD", tokens3);
        
        // All sentences contain char 1
        index.addEntry(1, id1);
        index.addEntry(1, id2);
        index.addEntry(1, id3);
        
        // Add other chars to index
        for (int i = 2; i <= 5; i++) {
            if (tokens1.size() >= i) index.addEntry(i, id1);
            if (tokens2.size() >= i) index.addEntry(i, id2);
            if (tokens3.size() >= i) index.addEntry(i, id3);
        }
        
        // User knows all chars except 1
        for (int i = 2; i <= 5; i++) {
            userState.markKnown(i);
        }
        
        SentenceFilter filter = new SentenceFilter(store, index, 0.50);
        List<Integer> results = filter.findIPlusOneSentences(1, userState);
        
        // Should return in order: id2 (len 3), id3 (len 4), id1 (len 5)
        assertEquals(3, results.size());
        assertEquals(id2, results.get(0));
        assertEquals(id3, results.get(1));
        assertEquals(id1, results.get(2));
    }

    @Test
    void testSortsBySentenceIdWhenSameLength() {
        // Add sentences with same length
        IntArrayList tokens1 = new IntArrayList(new int[]{1, 2, 3});
        IntArrayList tokens2 = new IntArrayList(new int[]{1, 4, 5});
        IntArrayList tokens3 = new IntArrayList(new int[]{1, 6, 7});
        
        int id1 = store.addSentence("ABC", tokens1);
        int id2 = store.addSentence("ADE", tokens2);
        int id3 = store.addSentence("AFG", tokens3);
        
        index.addEntry(1, id1);
        index.addEntry(1, id2);
        index.addEntry(1, id3);
        
        for (int i = 2; i <= 7; i++) {
            userState.markKnown(i);
        }
        
        SentenceFilter filter = new SentenceFilter(store, index, 0.50);
        List<Integer> results = filter.findIPlusOneSentences(1, userState);
        
        // Should return in order: id1, id2, id3 (all same length, ordered by ID)
        assertEquals(3, results.size());
        assertEquals(id1, results.get(0));
        assertEquals(id2, results.get(1));
        assertEquals(id3, results.get(2));
    }

    @Test
    void testIgnoresUnknownCharacters() {
        // Sentence with UNKNOWN_ID tokens
        IntArrayList tokens = new IntArrayList(new int[]{1, SentenceParser.UNKNOWN_ID, 2, SentenceParser.UNKNOWN_ID, 3});
        int sentenceId = store.addSentence("A.B.C", tokens);
        
        index.addEntry(1, sentenceId);
        index.addEntry(2, sentenceId);
        index.addEntry(3, sentenceId);
        
        // User knows 1 and 2, learning 3
        userState.markKnown(1);
        userState.markKnown(2);
        
        SentenceFilter filter = new SentenceFilter(store, index, 0.90);
        List<Integer> results = filter.findIPlusOneSentences(3, userState);
        
        // Should ignore UNKNOWN_ID: 2 known + 1 target = 3/3 = 100% >= 90%
        assertEquals(1, results.size());
    }

    @Test
    void testNoSentencesForUnknownChar() {
        IntArrayList tokens = new IntArrayList(new int[]{1, 2, 3});
        store.addSentence("ABC", tokens);
        
        index.addEntry(1, 0);
        index.addEntry(2, 0);
        index.addEntry(3, 0);
        
        userState.markKnown(1);
        userState.markKnown(2);
        
        SentenceFilter filter = new SentenceFilter(store, index);
        List<Integer> results = filter.findIPlusOneSentences(999, userState);
        
        // Char 999 not in any sentence
        assertEquals(0, results.size());
    }

    @Test
    void testMultipleSentencesMixedThreshold() {
        // Sentence 1: ABC (1,2,3) - 100% known
        // Sentence 2: ABCD (1,2,3,4) - 75% known
        // Sentence 3: ABCDE (1,2,3,4,5) - 60% known
        
        IntArrayList tokens1 = new IntArrayList(new int[]{1, 2, 3});
        IntArrayList tokens2 = new IntArrayList(new int[]{1, 2, 3, 4});
        IntArrayList tokens3 = new IntArrayList(new int[]{1, 2, 3, 4, 5});
        
        int id1 = store.addSentence("ABC", tokens1);
        int id2 = store.addSentence("ABCD", tokens2);
        int id3 = store.addSentence("ABCDE", tokens3);
        
        // All contain char 3
        index.addEntry(3, id1);
        index.addEntry(3, id2);
        index.addEntry(3, id3);
        
        // User knows 1 and 2, learning 3
        userState.markKnown(1);
        userState.markKnown(2);
        
        // At 0.90 threshold, only sentence 1 should pass (100%)
        SentenceFilter filter = new SentenceFilter(store, index, 0.90);
        List<Integer> results = filter.findIPlusOneSentences(3, userState);
        assertEquals(1, results.size());
        assertEquals(id1, results.get(0));
        
        // At 0.75 threshold, sentences 1 and 2 should pass
        results = filter.findIPlusOneSentences(3, userState, 0.75);
        assertEquals(2, results.size());
        assertEquals(id1, results.get(0)); // Shorter first
        assertEquals(id2, results.get(1));
        
        // At 0.60 threshold, all three should pass
        results = filter.findIPlusOneSentences(3, userState, 0.60);
        assertEquals(3, results.size());
        assertEquals(id1, results.get(0));
        assertEquals(id2, results.get(1));
        assertEquals(id3, results.get(2));
    }
}
