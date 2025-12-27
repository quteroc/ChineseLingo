package com.chineselingo.sentence;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.roaringbitmap.RoaringBitmap;

/**
 * Inverted index mapping character IDs to sentence IDs using RoaringBitmap.
 * Enables efficient lookup of all sentences containing a specific character.
 */
public class InvertedIndex {
    private final Int2ObjectOpenHashMap<RoaringBitmap> charToSentences;

    /**
     * Creates an empty InvertedIndex.
     */
    public InvertedIndex() {
        this.charToSentences = new Int2ObjectOpenHashMap<>();
    }

    /**
     * Adds a (charId, sentenceId) pair to the index.
     * 
     * @param charId the character ID
     * @param sentenceId the sentence ID containing this character
     */
    public void addEntry(int charId, int sentenceId) {
        RoaringBitmap bitmap = charToSentences.get(charId);
        if (bitmap == null) {
            bitmap = new RoaringBitmap();
            charToSentences.put(charId, bitmap);
        }
        bitmap.add(sentenceId);
    }

    /**
     * Gets all sentence IDs containing a specific character.
     * 
     * @param charId the character ID to look up
     * @return RoaringBitmap of sentence IDs, or empty bitmap if character not found
     */
    public RoaringBitmap getSentencesForChar(int charId) {
        RoaringBitmap bitmap = charToSentences.get(charId);
        return bitmap != null ? bitmap : new RoaringBitmap();
    }

    /**
     * Returns the number of unique characters indexed.
     * 
     * @return count of indexed characters
     */
    public int size() {
        return charToSentences.size();
    }
}
