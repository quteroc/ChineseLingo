package com.chineselingo.parser;

import com.chineselingo.data.CharIdMapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Parser for IDS-UCS (Ideographic Description Sequence) files.
 * 
 * Expected format (tab or space separated):
 * U+6797<tab>林<tab>⿰木木
 * 
 * The IDS describes how a character is composed. For WP-01, we treat it as a 
 * bag-of-components, ignoring IDC operators (⿰, ⿱, etc.).
 */
public class IDSParser {
    private static final Logger logger = LoggerFactory.getLogger(IDSParser.class);

    /**
     * Parses an IDS file and builds component-compound relationships.
     * 
     * @param filePath path to the IDS file
     * @param charIdMapper mapper to assign character IDs
     * @param componentToCompounds map from component ID to list of compound IDs
     * @param compoundToComponents map from compound ID to list of component IDs
     * @throws IOException if file reading fails
     */
    public void parse(Path filePath, CharIdMapper charIdMapper,
                     Int2ObjectOpenHashMap<IntArrayList> componentToCompounds,
                     Int2ObjectOpenHashMap<IntArrayList> compoundToComponents) 
            throws IOException {
        logger.info("Parsing IDS file: {}. Start:{}", filePath, Instant.now().toString());
        int lineCount = 0;
        int entryCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                try {
                    parseLine(line, charIdMapper, componentToCompounds, compoundToComponents);
                    entryCount++;
                } catch (Exception e) {
                    logger.warn("Failed to parse line {}: {}", lineCount, line, e);
                }
            }
        }

        logger.info("Parsed {} IDS entries from {} lines. End:{}", entryCount, lineCount, Instant.now().toString());
    }

    private void parseLine(String line, CharIdMapper charIdMapper,
                          Int2ObjectOpenHashMap<IntArrayList> componentToCompounds,
                          Int2ObjectOpenHashMap<IntArrayList> compoundToComponents) {
        // Format: U+6797 林 ⿰木木
        String[] parts = line.split("\\s+");
        
        if (parts.length < 3) {
            return;
        }

        // parts[0] is unicode (U+xxxx)
        // parts[1] is the character
        // parts[2] is the IDS decomposition
        String character = parts[1].trim();
        String ids = parts[2].trim();

        if (character.length() != 1) {
            return;
        }

        int compoundId = charIdMapper.getId(character);

        // Extract components from IDS
        IntArrayList componentIds = extractComponents(ids, charIdMapper);
        
        if (componentIds.isEmpty()) {
            return;
        }

        // Store compound -> components mapping
        compoundToComponents.put(compoundId, componentIds);

        // Store component -> compounds mapping (reverse index)
        for (int componentId : componentIds) {
            componentToCompounds
                .computeIfAbsent(componentId, k -> new IntArrayList())
                .add(compoundId);
        }
    }

    private IntArrayList extractComponents(String ids, CharIdMapper charIdMapper) {
        IntArrayList components = new IntArrayList();
        
        // Extract all characters from IDS, ignoring IDC operators
        // IDC operators are in the Unicode block U+2FF0–U+2FFF
        for (int i = 0; i < ids.length(); ) {
            int codePoint = ids.codePointAt(i);
            
            // Skip IDC operators (U+2FF0 to U+2FFF)
            if (codePoint >= 0x2FF0 && codePoint <= 0x2FFF) {
                i += Character.charCount(codePoint);
                continue;
            }

            // Extract the character
            String component = ids.substring(i, i + Character.charCount(codePoint));
            int componentId = charIdMapper.getId(component);
            
            // Add to components list if not already present (treat as set)
            if (!components.contains(componentId)) {
                components.add(componentId);
            }
            
            i += Character.charCount(codePoint);
        }
        
        return components;
    }
}
