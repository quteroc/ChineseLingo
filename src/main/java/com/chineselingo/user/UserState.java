package com.chineselingo.user;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.BitSet;

/**
 * Represents a user's learning state and progress.
 * Tracks known characters and review history in a memory-efficient manner.
 */
public class UserState {
    private final BitSet knownChars;
    private final Int2ObjectOpenHashMap<ReviewHistory> reviewHistory;

    /**
     * Creates a new empty UserState.
     */
    public UserState() {
        this.knownChars = new BitSet();
        this.reviewHistory = new Int2ObjectOpenHashMap<>();
    }

    /**
     * Checks if a character is known by the user.
     * @param charId the character ID
     * @return true if the character is known
     */
    public boolean isKnown(int charId) {
        return knownChars.get(charId);
    }

    /**
     * Marks a character as known by the user.
     * @param charId the character ID to mark as known
     */
    public void markKnown(int charId) {
        knownChars.set(charId);
    }

    /**
     * Records a review event for a character.
     * Creates a new ReviewHistory entry if one doesn't exist.
     * 
     * @param charId the character ID
     * @param success whether the review was successful
     * @param nowEpochSeconds current time in epoch seconds
     */
    public void recordReview(int charId, boolean success, long nowEpochSeconds) {
        ReviewHistory history = reviewHistory.get(charId);
        if (history == null) {
            history = new ReviewHistory();
            reviewHistory.put(charId, history);
        }
        history.views++;
        if (success) {
            history.successes++;
        }
        history.lastReviewedEpochSeconds = nowEpochSeconds;
    }

    /**
     * Gets the review history for a character.
     * @param charId the character ID
     * @return ReviewHistory object, or null if no history exists
     */
    public ReviewHistory getReviewHistory(int charId) {
        return reviewHistory.get(charId);
    }

    /**
     * Gets a copy of the BitSet of known characters.
     * Returns a defensive copy to prevent external modification.
     * @return a clone of the BitSet of known character IDs
     */
    public BitSet getKnownChars() {
        return (BitSet) knownChars.clone();
    }

    /**
     * Container for per-character review statistics.
     */
    public static class ReviewHistory {
        private int views;
        private int successes;
        private long lastReviewedEpochSeconds;

        /**
         * Creates a new ReviewHistory with zero stats.
         */
        public ReviewHistory() {
            this.views = 0;
            this.successes = 0;
            this.lastReviewedEpochSeconds = 0;
        }

        /**
         * Gets the number of times this character has been viewed.
         * @return view count
         */
        public int getViews() {
            return views;
        }

        /**
         * Gets the number of successful reviews.
         * @return success count
         */
        public int getSuccesses() {
            return successes;
        }

        /**
         * Gets the timestamp of the last review in epoch seconds.
         * @return last review timestamp, or 0 if never reviewed
         */
        public long getLastReviewedEpochSeconds() {
            return lastReviewedEpochSeconds;
        }
    }
}
