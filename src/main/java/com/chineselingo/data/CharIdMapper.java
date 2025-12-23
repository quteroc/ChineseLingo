package com.chineselingo.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe mapper that assigns stable integer IDs to Chinese characters.
 * Used during the data loading phase to convert characters to IDs for efficient storage.
 */
public class CharIdMapper {
    private final ConcurrentHashMap<String, Integer> charToId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> idToChar = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(0);

    /**
     * Gets or creates an ID for the given character.
     * @param character the character (can be a single char or a String)
     * @return the stable integer ID for this character
     */
    public int getId(String character) {
        return charToId.computeIfAbsent(character, c -> {
            int id = nextId.getAndIncrement();
            idToChar.put(id, c);
            return id;
        });
    }

    /**
     * Gets the character for a given ID.
     * @param id the character ID
     * @return the character string, or null if ID not found
     */
    public String getChar(int id) {
        return idToChar.get(id);
    }

    /**
     * Returns the total number of unique characters mapped.
     * @return count of unique characters
     */
    public int size() {
        return charToId.size();
    }

    /**
     * Returns a read-only view of the char-to-ID mapping.
     * @return unmodifiable map of characters to IDs
     */
    public Map<String, Integer> getReadOnlyCharToIdMap() {
        return Map.copyOf(charToId);
    }

    /**
     * Returns a read-only view of the ID-to-char mapping.
     * @return unmodifiable map of IDs to characters
     */
    public Map<Integer, String> getReadOnlyIdToCharMap() {
        return Map.copyOf(idToChar);
    }
}
