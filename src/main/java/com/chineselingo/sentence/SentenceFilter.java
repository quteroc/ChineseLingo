package com.chineselingo.sentence;

import com.chineselingo.user.UserState;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.roaringbitmap.RoaringBitmap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Filters and ranks sentences for "i+1" learning.
 * 
 * An i+1 sentence contains exactly one unknown target character,
 * with all other characters being known to the user.
 */
public class SentenceFilter {
    private final SentenceStore store;
    private final InvertedIndex index;
    private final double defaultThreshold;

    /**
     * Creates a SentenceFilter with default threshold of 0.90.
     * 
     * @param store the sentence store
     * @param index the inverted index
     */
    public SentenceFilter(SentenceStore store, InvertedIndex index) {
        this(store, index, 0.90);
    }

    /**
     * Creates a SentenceFilter with custom threshold.
     * 
     * @param store the sentence store
     * @param index the inverted index
     * @param defaultThreshold the default known character threshold (0.0 to 1.0)
     */
    public SentenceFilter(SentenceStore store, InvertedIndex index, double defaultThreshold) {
        this.store = store;
        this.index = index;
        this.defaultThreshold = defaultThreshold;
    }

    /**
     * Finds i+1 sentences for learning a target character.
     * 
     * An i+1 sentence:
     * - Contains the target character
     * - Has >= threshold proportion of known characters (including the target)
     * 
     * Results are sorted by:
     * 1. Sentence length (shorter first - simpler examples)
     * 2. Sentence ID (ascending - deterministic ordering)
     * 
     * @param targetCharId the character to learn
     * @param state the user's learning state
     * @return list of sentence IDs sorted by ranking
     */
    public List<Integer> findIPlusOneSentences(int targetCharId, UserState state) {
        return findIPlusOneSentences(targetCharId, state, defaultThreshold);
    }

    /**
     * Finds i+1 sentences with custom threshold.
     * 
     * @param targetCharId the character to learn
     * @param state the user's learning state
     * @param threshold the minimum proportion of known characters (0.0 to 1.0)
     * @return list of sentence IDs sorted by ranking
     */
    public List<Integer> findIPlusOneSentences(int targetCharId, UserState state, double threshold) {
        // Get all sentences containing the target character
        RoaringBitmap candidateSentences = index.getSentencesForChar(targetCharId);
        
        List<SentenceCandidate> candidates = new ArrayList<>();
        
        // Evaluate each candidate sentence
        for (int sentenceId : candidateSentences) {
            int[] tokens = store.tokens(sentenceId);
            if (tokens == null) {
                continue;
            }
            
            int totalChars = 0;
            int knownChars = 0;
            
            for (int charId : tokens) {
                // Skip UNKNOWN_ID (punctuation, etc.)
                if (charId == SentenceParser.UNKNOWN_ID) {
                    continue;
                }
                
                totalChars++;
                
                // Count as known if in user's known set OR is the target character
                if (state.isKnown(charId) || charId == targetCharId) {
                    knownChars++;
                }
            }
            
            // Calculate known ratio
            if (totalChars == 0) {
                continue; // Skip empty sentences
            }
            
            double knownRatio = (double) knownChars / totalChars;
            
            // Accept if ratio meets threshold
            if (knownRatio >= threshold) {
                candidates.add(new SentenceCandidate(sentenceId, tokens.length));
            }
        }
        
        // Sort by length (shorter first), then by sentenceId (ascending)
        candidates.sort(Comparator
            .comparingInt((SentenceCandidate c) -> c.length)
            .thenComparingInt(c -> c.sentenceId));
        
        // Extract sentence IDs
        List<Integer> result = new ArrayList<>(candidates.size());
        for (SentenceCandidate candidate : candidates) {
            result.add(candidate.sentenceId);
        }
        
        return result;
    }

    private static class SentenceCandidate {
        final int sentenceId;
        final int length;

        SentenceCandidate(int sentenceId, int length) {
            this.sentenceId = sentenceId;
            this.length = length;
        }
    }
}
