package com.chineselingo.parser;

import com.chineselingo.data.CharIdMapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Parser for CC-CEDICT format dictionary files.
 * 
 * Expected format:
 * 林 林 [lin2] /forest/grove/
 * 
 * Lines starting with # are comments and are skipped.
 * For WP-01, focuses on single-character entries.
 */
public class CEDICTParser {
    private static final Logger logger = LoggerFactory.getLogger(CEDICTParser.class);

    /**
     * Parses a CEDICT file and populates the definitions map.
     * 
     * @param filePath path to the CEDICT file
     * @param charIdMapper mapper to assign character IDs
     * @param definitions map to populate with charId -> definition
     * @throws IOException if file reading fails
     */
    public void parse(Path filePath, CharIdMapper charIdMapper, Int2ObjectOpenHashMap<String> definitions) 
            throws IOException {
        logger.info("Parsing CEDICT file: {}. Start:{}", filePath, Instant.now().toString());
        int lineCount = 0;
        int entryCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                
                // Skip comments and empty lines
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }

                try {
                    parseLine(line, charIdMapper, definitions);
                    entryCount++;
                } catch (Exception e) {
                    logger.warn("Failed to parse line {}: {}", lineCount, line, e);
                }
            }
        }

        logger.info("Parsed {} entries from {} lines. End: {}", entryCount, lineCount, Instant.now().toString());
    }

    private void parseLine(String line, CharIdMapper charIdMapper, Int2ObjectOpenHashMap<String> definitions) {
        // Format: 传统 简体 [pinyin] /def1/def2/
        // Find the opening bracket for pinyin
        int pinyinStart = line.indexOf('[');
        if (pinyinStart == -1) {
            return;
        }

        // Extract traditional and simplified (space-separated before pinyin)
        String beforePinyin = line.substring(0, pinyinStart).trim();
        int spaceIdx = beforePinyin.indexOf(' ');
        if (spaceIdx == -1) {
            return;
        }

        String traditional = beforePinyin.substring(0, spaceIdx).trim();
        String simplified = beforePinyin.substring(spaceIdx + 1).trim();

        // Find the closing bracket and definition start
        int pinyinEnd = line.indexOf(']', pinyinStart);
        if (pinyinEnd == -1 || pinyinEnd + 1 >= line.length()) {
            return;
        }

        // Extract definition (everything after the closing bracket)
        String definition = line.substring(pinyinEnd + 1).trim();

        // For WP-01, focus on single-character entries
        // Store both simplified and traditional if they're single characters
        if (simplified.length() == 1) {
            int charId = charIdMapper.getId(simplified);
            definitions.put(charId, definition);
        }

        if (traditional.length() == 1 && !traditional.equals(simplified)) {
            int charId = charIdMapper.getId(traditional);
            definitions.put(charId, definition);
        }
    }
}
