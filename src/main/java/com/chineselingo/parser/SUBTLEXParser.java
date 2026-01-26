package com.chineselingo.parser;

import com.chineselingo.data.CharIdMapper;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Parser for SUBTLEX frequency files.
 * 
 * Expected format (tab or comma separated):
 * character<tab>frequency
 * 的<tab>1234567
 * 
 * Or with header:
 * Character,Frequency
 * 的,1234567
 */
public class SUBTLEXParser {
    private static final Logger logger = LoggerFactory.getLogger(SUBTLEXParser.class);

    /**
     * Parses a SUBTLEX frequency file and populates the frequencies map.
     * 
     * @param filePath path to the frequency file
     * @param charIdMapper mapper to assign character IDs
     * @param frequencies map to populate with charId -> frequency
     * @throws IOException if file reading fails
     */
    public void parse(Path filePath, CharIdMapper charIdMapper, Int2IntOpenHashMap frequencies) 
            throws IOException {
        logger.info("Parsing SUBTLEX file: {}. Start:{}", filePath, Instant.now().toString());
        int lineCount = 0;
        int entryCount = 0;
        boolean firstLine = true;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                line = line.trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                // Skip header if present (first line with non-numeric second column)
                if (firstLine) {
                    firstLine = false;
                    if (looksLikeHeader(line)) {
                        continue;
                    }
                }

                try {
                    parseLine(line, charIdMapper, frequencies);
                    entryCount++;
                } catch (Exception e) {
                    logger.warn("Failed to parse line {}: {}", lineCount, line, e);
                }
            }
        }

        logger.info("Parsed {} frequency entries from {} lines. End:{}", entryCount, lineCount, Instant.now().toString());
    }

    private boolean looksLikeHeader(String line) {
        // Try to detect if this is a header line
        String[] parts = splitLine(line);
        if (parts.length >= 2) {
            try {
                Integer.parseInt(parts[1]);
                return false; // Second column is numeric, not a header
            } catch (NumberFormatException e) {
                return true; // Second column is not numeric, likely a header
            }
        }
        return false;
    }

    private void parseLine(String line, CharIdMapper charIdMapper, Int2IntOpenHashMap frequencies) {
        String[] parts = splitLine(line);
        
        if (parts.length < 2) {
            return;
        }

        String character = parts[0].trim();
        String freqStr = parts[1].trim();

        // Only process single characters
        if (character.length() != 1) {
            return;
        }

        try {
            int frequency = Integer.parseInt(freqStr);
            int charId = charIdMapper.getId(character);
            frequencies.put(charId, frequency);
        } catch (NumberFormatException e) {
            // Skip invalid frequency values
        }
    }

    private String[] splitLine(String line) {
        // Try tab first, then comma
        if (line.contains("\t")) {
            return line.split("\t");
        } else if (line.contains(",")) {
            return line.split(",");
        } else {
            // Fallback to whitespace
            return line.split("\\s+");
        }
    }
}
