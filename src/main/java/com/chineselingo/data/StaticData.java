package com.chineselingo.data;

import com.chineselingo.sentence.InvertedIndex;
import com.chineselingo.sentence.SentenceStore;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Immutable container for all parsed data structures.
 * Holds character definitions, frequencies, component relationships, and sentences.
 */
public class StaticData {
    private final CharIdMapper charIdMapper;
    private final Int2ObjectOpenHashMap<String> definitions;
    private final Int2IntOpenHashMap frequencies;
    private final Int2ObjectOpenHashMap<IntArrayList> componentToCompounds;
    private final Int2ObjectOpenHashMap<IntArrayList> compoundToComponents;
    private final SentenceStore sentenceStore;
    private final InvertedIndex sentenceIndex;

    public StaticData(
            CharIdMapper charIdMapper,
            Int2ObjectOpenHashMap<String> definitions,
            Int2IntOpenHashMap frequencies,
            Int2ObjectOpenHashMap<IntArrayList> componentToCompounds,
            Int2ObjectOpenHashMap<IntArrayList> compoundToComponents,
            SentenceStore sentenceStore,
            InvertedIndex sentenceIndex) {
        this.charIdMapper = charIdMapper;
        this.definitions = definitions;
        this.frequencies = frequencies;
        this.componentToCompounds = componentToCompounds;
        this.compoundToComponents = compoundToComponents;
        this.sentenceStore = sentenceStore;
        this.sentenceIndex = sentenceIndex;
    }

    public CharIdMapper getCharIdMapper() {
        return charIdMapper;
    }

    public Int2ObjectOpenHashMap<String> getDefinitions() {
        return definitions;
    }

    public Int2IntOpenHashMap getFrequencies() {
        return frequencies;
    }

    public Int2ObjectOpenHashMap<IntArrayList> getComponentToCompounds() {
        return componentToCompounds;
    }

    public Int2ObjectOpenHashMap<IntArrayList> getCompoundToComponents() {
        return compoundToComponents;
    }

    /**
     * Gets the definition for a character ID.
     * @param charId the character ID
     * @return definition string, or null if not found
     */
    public String getDefinition(int charId) {
        return definitions.get(charId);
    }

    /**
     * Gets the frequency for a character ID.
     * @param charId the character ID
     * @return frequency count, or 0 if not found
     */
    public int getFrequency(int charId) {
        return frequencies.get(charId);
    }

    public int getMostFrequent() {
        int maxKey = 0;
        int maxValue = Integer.MIN_VALUE;

        for (Int2IntMap.Entry entry : frequencies.int2IntEntrySet()) {
            if (entry.getIntValue() > maxValue) {
                maxValue = entry.getIntValue();
                maxKey = entry.getIntKey();
            }
        }
        return maxKey;
    }
    /**
     * Gets the list of compound character IDs that contain the given component.
     * @param componentId the component character ID
     * @return list of compound IDs, or null if not found
     */
    public IntArrayList getCompoundsContaining(int componentId) {
        return componentToCompounds.get(componentId);
    }

    /**
     * Gets the list of component character IDs for a given compound.
     * @param compoundId the compound character ID
     * @return list of component IDs, or null if not found
     */
    public IntArrayList getComponents(int compoundId) {
        return compoundToComponents.get(compoundId);
    }

    /**
     * Gets the sentence store containing all parsed sentences.
     * @return the sentence store, or null if not loaded
     */
    public SentenceStore getSentenceStore() {
        return sentenceStore;
    }

    /**
     * Gets the inverted index for sentence lookup by character.
     * @return the inverted index, or null if not loaded
     */
    public InvertedIndex getSentenceIndex() {
        return sentenceIndex;
    }
}
