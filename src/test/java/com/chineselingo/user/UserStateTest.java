package com.chineselingo.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserStateTest {

    private UserState userState;

    @BeforeEach
    void setUp() {
        userState = new UserState();
    }

    @Test
    void testInitialStateHasNoKnownCharacters() {
        assertFalse(userState.isKnown(0));
        assertFalse(userState.isKnown(1));
        assertFalse(userState.isKnown(100));
    }

    @Test
    void testMarkKnown() {
        int charId = 42;
        
        assertFalse(userState.isKnown(charId), "Initially should not be known");
        
        userState.markKnown(charId);
        
        assertTrue(userState.isKnown(charId), "Should be known after marking");
    }

    @Test
    void testMarkMultipleCharactersKnown() {
        userState.markKnown(1);
        userState.markKnown(10);
        userState.markKnown(100);
        
        assertTrue(userState.isKnown(1));
        assertTrue(userState.isKnown(10));
        assertTrue(userState.isKnown(100));
        assertFalse(userState.isKnown(5));
        assertFalse(userState.isKnown(50));
    }

    @Test
    void testRecordReview() {
        int charId = 5;
        long timestamp = 1700000000L;
        
        assertNull(userState.getReviewHistory(charId), "Initially no history");
        
        userState.recordReview(charId, true, timestamp);
        
        UserState.ReviewHistory history = userState.getReviewHistory(charId);
        assertNotNull(history, "History should exist after review");
        assertEquals(1, history.getViews(), "Should have 1 view");
        assertEquals(1, history.getSuccesses(), "Should have 1 success");
        assertEquals(timestamp, history.getLastReviewedEpochSeconds());
    }

    @Test
    void testRecordMultipleReviews() {
        int charId = 5;
        
        userState.recordReview(charId, true, 1000L);
        userState.recordReview(charId, true, 2000L);
        userState.recordReview(charId, false, 3000L);
        userState.recordReview(charId, true, 4000L);
        
        UserState.ReviewHistory history = userState.getReviewHistory(charId);
        assertNotNull(history);
        assertEquals(4, history.getViews(), "Should have 4 views");
        assertEquals(3, history.getSuccesses(), "Should have 3 successes");
        assertEquals(4000L, history.getLastReviewedEpochSeconds(), "Should have latest timestamp");
    }

    @Test
    void testReviewHistoryForMultipleCharacters() {
        userState.recordReview(1, true, 1000L);
        userState.recordReview(2, false, 2000L);
        userState.recordReview(3, true, 3000L);
        
        UserState.ReviewHistory history1 = userState.getReviewHistory(1);
        UserState.ReviewHistory history2 = userState.getReviewHistory(2);
        UserState.ReviewHistory history3 = userState.getReviewHistory(3);
        
        assertNotNull(history1);
        assertNotNull(history2);
        assertNotNull(history3);
        
        assertEquals(1, history1.getSuccesses());
        assertEquals(0, history2.getSuccesses());
        assertEquals(1, history3.getSuccesses());
        
        assertEquals(1000L, history1.getLastReviewedEpochSeconds());
        assertEquals(2000L, history2.getLastReviewedEpochSeconds());
        assertEquals(3000L, history3.getLastReviewedEpochSeconds());
    }

    @Test
    void testGetReviewHistoryForNonReviewedCharacter() {
        assertNull(userState.getReviewHistory(999));
    }

    @Test
    void testGetKnownChars() {
        userState.markKnown(5);
        userState.markKnown(10);
        userState.markKnown(15);
        
        var knownChars = userState.getKnownChars();
        assertNotNull(knownChars);
        assertTrue(knownChars.get(5));
        assertTrue(knownChars.get(10));
        assertTrue(knownChars.get(15));
        assertFalse(knownChars.get(7));
    }

    @Test
    void testReviewHistoryInitialValues() {
        UserState.ReviewHistory history = new UserState.ReviewHistory();
        
        assertEquals(0, history.getViews());
        assertEquals(0, history.getSuccesses());
        assertEquals(0, history.getLastReviewedEpochSeconds());
    }
}
