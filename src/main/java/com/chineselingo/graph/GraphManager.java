package com.chineselingo.graph;

import com.chineselingo.data.StaticData;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Manages the structural graph of Chinese character components.
 * Provides safe access to component-compound relationships from StaticData.
 */
public class GraphManager {
    private final StaticData staticData;

    /**
     * Creates a GraphManager wrapping the given StaticData.
     * @param staticData the immutable data container
     */
    public GraphManager(StaticData staticData) {
        if (staticData == null) {
            throw new IllegalArgumentException("StaticData cannot be null");
        }
        this.staticData = staticData;
    }

    /**
     * Gets the list of compound character IDs that contain the given component.
     * Returns a defensive copy to ensure immutability.
     * 
     * @param componentId the component character ID
     * @return defensive copy of compound IDs list, or null if component not found
     */
    public IntArrayList getCompoundsForComponent(int componentId) {
        IntArrayList compounds = staticData.getCompoundsContaining(componentId);
        if (compounds == null) {
            return null;
        }
        // Return defensive copy
        return compounds.clone();
    }

    /**
     * Gets the list of component character IDs for a given compound.
     * Returns a defensive copy to ensure immutability.
     * 
     * @param compoundId the compound character ID
     * @return defensive copy of component IDs list, or null if compound not found
     */
    public IntArrayList getComponentsForCompound(int compoundId) {
        IntArrayList components = staticData.getComponents(compoundId);
        if (components == null) {
            return null;
        }
        // Return defensive copy
        return components.clone();
    }

    /**
     * Gets the frequency of a character from the underlying data.
     * 
     * @param charId the character ID
     * @return frequency count, or 0 if not found
     */
    public int getFrequency(int charId) {
        return staticData.getFrequency(charId);
    }

    /**
     * Gets the underlying StaticData (for advanced use cases).
     * Note: Callers should treat the returned data as immutable.
     * 
     * @return the StaticData instance
     */
    public StaticData getStaticData() {
        return staticData;
    }
}
