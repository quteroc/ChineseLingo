package com.chineselingo.sentence;

import com.chineselingo.data.CharIdMapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Parser for Tatoeba-style sentence files.
 * 
 * Expected format (TSV/CSV):
 * sentenceId<tab>lang<tab>text
 * 
 * Filters:
 * - Only Mandarin Chinese (lang = "cmn")
 * - Length between 2 and 25 characters (by codepoints)
 * 
 * Tokenization:
 * - Maps each character to its ID via CharIdMapper
 * - Unknown characters are mapped to a sentinel UNKNOWN_ID (-1)
 */
public class SentenceParser {
    private static final Logger logger = LoggerFactory.getLogger(SentenceParser.class);
    
    /**
     * Sentinel value for unknown/unmapped characters.
     * These are typically punctuation or characters not in the main corpus.
     */
    public static final int UNKNOWN_ID = -1;
    
    private static final String MANDARIN_LANG_CODE = "cmn";
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 25;

    /**
     * Parses a Tatoeba-style sentence file and populates store and index.
     * 
     * @param filePath path to the sentence file
     * @param charIdMapper mapper to convert characters to IDs
     * @param store sentence store to populate
     * @param index inverted index to populate
     * @throws IOException if file reading fails
     */
    public void parse(Path filePath, CharIdMapper charIdMapper, 
                     SentenceStore store, InvertedIndex index) throws IOException {
        logger.info("Parsing sentence file: {}. Start:{}", filePath, Instant.now().toString());
        int lineCount = 0;
        int acceptedCount = 0;
        int filteredByLang = 0;
        int filteredByLength = 0;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    ParseResult result = parseLine(line, charIdMapper);
                    
                    if (result == null) {
                        continue; // Invalid format
                    }
                    
                    if (!result.isMandarinChinese) {
                        filteredByLang++;
                        continue;
                    }
                    
                    if (!result.isValidLength) {
                        filteredByLength++;
                        continue;
                    }
                    
                    // Add to store and index
                    int sentenceId = store.addSentence(result.text, result.tokens);
                    
                    // Build inverted index
                    for (int i = 0; i < result.tokens.size(); i++) {
                        int charId = result.tokens.getInt(i);
                        if (charId != UNKNOWN_ID) {
                            index.addEntry(charId, sentenceId);
                        }
                    }
                    
                    acceptedCount++;
                } catch (Exception e) {
                    logger.warn("Failed to parse line {}: {}", lineCount, line, e);
                }
            }
        }

        logger.info("Parsed {} sentences from {} lines. End:{}", acceptedCount, lineCount, Instant.now().toString());
        logger.info("  Filtered by language: {}", filteredByLang);
        logger.info("  Filtered by length: {}", filteredByLength);
    }

    private ParseResult parseLine(String line, CharIdMapper charIdMapper) {
        // Try tab-separated first
        String[] parts = line.split("\t");
        
        // If not 3 parts, try comma-separated
        if (parts.length != 3) {
            parts = line.split(",", 3);
        }
        
        if (parts.length != 3) {
            return null; // Invalid format
        }
        
        String sentenceIdStr = parts[0].trim();
        String lang = parts[1].trim();
        String text = parts[2].trim();
        
        // Check language
        boolean isMandarinChinese = MANDARIN_LANG_CODE.equals(lang);
        
        // Count codepoints for length validation
        int codepointCount = text.codePointCount(0, text.length());
        boolean isValidLength = codepointCount >= MIN_LENGTH && codepointCount <= MAX_LENGTH;
        
        // Tokenize the text
        IntArrayList tokens = tokenizeText(text, charIdMapper);
        
        ParseResult result = new ParseResult();
        result.text = text;
        result.tokens = tokens;
        result.isMandarinChinese = isMandarinChinese;
        result.isValidLength = isValidLength;
        
        return result;
    }

    private IntArrayList tokenizeText(String text, CharIdMapper charIdMapper) {
        IntArrayList tokens = new IntArrayList();
        
        // Iterate over codepoints
        for (int i = 0; i < text.length(); ) {
            int codepoint = text.codePointAt(i);
            String charStr = new String(Character.toChars(codepoint));
            
            // Check if this character exists in the mapper
            // If not, it's likely punctuation or unknown, map to UNKNOWN_ID
            Integer existingId = charIdMapper.getReadOnlyCharToIdMap().get(charStr);
            
            if (existingId != null) {
                tokens.add(existingId);
            } else {
                // Map unknown characters to UNKNOWN_ID
                tokens.add(UNKNOWN_ID);
            }
            
            i += Character.charCount(codepoint);
        }
        
        return tokens;
    }

    private static class ParseResult {
        String text;
        IntArrayList tokens;
        boolean isMandarinChinese;
        boolean isValidLength;
    }
}
