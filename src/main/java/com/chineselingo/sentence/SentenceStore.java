package com.chineselingo.sentence;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Memory-efficient storage for sentences with tokenized character arrays.
 * Each sentence is stored with its original text and tokenized character IDs.
 */
public class SentenceStore {
    private final Int2ObjectOpenHashMap<String> texts;
    private final Int2ObjectOpenHashMap<int[]> tokens;
    private int nextId;

    /**
     * Creates an empty SentenceStore.
     */
    public SentenceStore() {
        this.texts = new Int2ObjectOpenHashMap<>();
        this.tokens = new Int2ObjectOpenHashMap<>();
        this.nextId = 0;
    }

    /**
     * Adds a sentence to the store.
     * 
     * @param text the original sentence text
     * @param tokenList the tokenized character IDs as IntArrayList
     * @return the assigned sentence ID
     */
    public int addSentence(String text, IntArrayList tokenList) {
        int sentenceId = nextId++;
        texts.put(sentenceId, text);
        tokens.put(sentenceId, tokenList.toIntArray());
        return sentenceId;
    }

    /**
     * Gets the original text for a sentence.
     * 
     * @param sentenceId the sentence ID
     * @return the original text, or null if not found
     */
    public String text(int sentenceId) {
        return texts.get(sentenceId);
    }

    /**
     * Gets the tokenized character IDs for a sentence.
     * 
     * @param sentenceId the sentence ID
     * @return array of character IDs, or null if not found
     */
    public int[] tokens(int sentenceId) {
        return tokens.get(sentenceId);
    }

    /**
     * Returns the total number of sentences stored.
     * 
     * @return count of sentences
     */
    public int size() {
        return texts.size();
    }
}
