package com.chineselingo.recommendation;

import com.chineselingo.graph.GraphManager;
import com.chineselingo.user.UserState;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

/**
 * Recommendation engine for suggesting the next best character to learn.
 * Uses component relationships and character frequency to make intelligent suggestions.
 */
public class RecommendationEngine {
    private final GraphManager graphManager;
    private final RecommendationMode mode;

    /**
     * Recommendation mode determining how strict the component requirements are.
     */
    public enum RecommendationMode {
        /**
         * STRICT mode: candidate compound is learnable only if ALL components are known.
         */
        STRICT,
        
        /**
         * LENIENT mode: candidate compound is learnable if at least ONE component is known.
         */
        LENIENT
    }

    /**
     * Creates a RecommendationEngine with the specified mode.
     * @param graphManager the graph manager for component relationships
     * @param mode the recommendation mode (STRICT or LENIENT)
     */
    public RecommendationEngine(GraphManager graphManager, RecommendationMode mode) {
        if (graphManager == null) {
            throw new IllegalArgumentException("GraphManager cannot be null");
        }
        if (mode == null) {
            throw new IllegalArgumentException("RecommendationMode cannot be null");
        }
        this.graphManager = graphManager;
        this.mode = mode;
    }

    /**
     * Recommends the next best character to learn based on user's known characters.
     * 
     * @param userState the user's current learning state
     * @return the recommended character ID, or -1 if no suitable candidate found
     */
    public int recommendNext(UserState userState) {
        IntArrayList topN = recommendTopN(userState, 1);
        return topN.isEmpty() ? -1 : topN.getInt(0);
    }

    /**
     * Recommends the top N characters to learn based on user's known characters.
     * 
     * @param userState the user's current learning state
     * @param n the number of recommendations to return
     * @return list of recommended character IDs (may be less than n if fewer candidates available)
     */
    public IntArrayList recommendTopN(UserState userState, int n) {
        if (userState == null) {
            throw new IllegalArgumentException("UserState cannot be null");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }

        // Collect all learnable candidates
        IntOpenHashSet candidates = new IntOpenHashSet();
        
        // Get known characters once to avoid multiple clones
        BitSet knownChars = userState.getKnownChars();
        
        // Iterate through known characters and find compounds that contain them
        for (int knownCharId = knownChars.nextSetBit(0); 
             knownCharId >= 0; 
             knownCharId = knownChars.nextSetBit(knownCharId + 1)) {
            
            IntArrayList compounds = graphManager.getCompoundsForComponent(knownCharId);
            if (compounds != null) {
                for (int compound : compounds) {
                    // Skip if already known
                    if (userState.isKnown(compound)) {
                        continue;
                    }
                    
                    // Check if compound meets mode requirements
                    if (isLearnable(compound, userState)) {
                        candidates.add(compound);
                    }
                }
            }
        }

        // Convert to list and sort by frequency (descending) then by charId (ascending)
        List<CharFrequency> candidateList = new ArrayList<>();
        for (int candidateId : candidates) {
            int frequency = graphManager.getFrequency(candidateId);
            candidateList.add(new CharFrequency(candidateId, frequency));
        }

        candidateList.sort(Comparator
            .<CharFrequency>comparingInt(cf -> cf.frequency)
            .reversed()
            .thenComparingInt(cf -> cf.charId));

        // Return top N
        IntArrayList result = new IntArrayList();
        int limit = Math.min(n, candidateList.size());
        for (int i = 0; i < limit; i++) {
            result.add(candidateList.get(i).charId);
        }
        
        return result;
    }

    /**
     * Checks if a compound character is learnable based on the current mode.
     * 
     * @param compoundId the compound character ID
     * @param userState the user's current learning state
     * @return true if the compound is learnable
     */
    private boolean isLearnable(int compoundId, UserState userState) {
        IntArrayList components = graphManager.getComponentsForCompound(compoundId);
        
        // If no components defined, not learnable through this mechanism
        if (components == null || components.isEmpty()) {
            return false;
        }

        if (mode == RecommendationMode.STRICT) {
            // All components must be known
            for (int componentId : components) {
                if (!userState.isKnown(componentId)) {
                    return false;
                }
            }
            return true;
        } else { // LENIENT
            // At least one component must be known
            for (int componentId : components) {
                if (userState.isKnown(componentId)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Helper class for sorting characters by frequency and ID.
     */
    private static class CharFrequency {
        final int charId;
        final int frequency;

        CharFrequency(int charId, int frequency) {
            this.charId = charId;
            this.frequency = frequency;
        }
    }
}
