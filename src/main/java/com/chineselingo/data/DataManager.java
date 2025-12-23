package com.chineselingo.data;

import com.chineselingo.parser.CEDICTParser;
import com.chineselingo.parser.IDSParser;
import com.chineselingo.parser.SUBTLEXParser;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Facade for data loading and parsing operations.
 * Orchestrates the parsing of CEDICT, SUBTLEX, and IDS files into memory-efficient structures.
 */
public class DataManager {
    private static final Logger logger = LoggerFactory.getLogger(DataManager.class);

    private final Path dataDirectory;
    private final CEDICTParser cedictParser;
    private final SUBTLEXParser subtlexParser;
    private final IDSParser idsParser;

    /**
     * Creates a DataManager with the specified data directory.
     * @param dataDirectory path to directory containing data files
     */
    public DataManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.cedictParser = new CEDICTParser();
        this.subtlexParser = new SUBTLEXParser();
        this.idsParser = new IDSParser();
    }

    /**
     * Creates a DataManager with default data directory "data/".
     */
    public DataManager() {
        this(Paths.get("data"));
    }

    /**
     * Loads all data files and returns an immutable StaticData object.
     * 
     * Expected files in data directory:
     * - cedict_ts.u8 (or cedict.txt)
     * - subtlex.txt (or frequency.txt)
     * - ids.txt
     * 
     * @return StaticData containing all parsed structures
     * @throws IOException if any file cannot be read or parsed
     */
    public StaticData loadData() throws IOException {
        logger.info("Loading data from directory: {}", dataDirectory);

        if (!Files.exists(dataDirectory) || !Files.isDirectory(dataDirectory)) {
            throw new IOException("Data directory does not exist: " + dataDirectory);
        }

        // Initialize data structures
        CharIdMapper charIdMapper = new CharIdMapper();
        Int2ObjectOpenHashMap<String> definitions = new Int2ObjectOpenHashMap<>();
        Int2IntOpenHashMap frequencies = new Int2IntOpenHashMap();
        Int2ObjectOpenHashMap<IntArrayList> componentToCompounds = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<IntArrayList> compoundToComponents = new Int2ObjectOpenHashMap<>();

        // Load CEDICT
        Path cedictPath = findFile(dataDirectory, "cedict_ts.u8", "cedict.txt");
        if (cedictPath != null) {
            cedictParser.parse(cedictPath, charIdMapper, definitions);
        } else {
            logger.warn("CEDICT file not found in {}", dataDirectory);
        }

        // Load SUBTLEX
        Path subtlexPath = findFile(dataDirectory, "subtlex.txt", "frequency.txt");
        if (subtlexPath != null) {
            subtlexParser.parse(subtlexPath, charIdMapper, frequencies);
        } else {
            logger.warn("SUBTLEX file not found in {}", dataDirectory);
        }

        // Load IDS
        Path idsPath = findFile(dataDirectory, "ids.txt", "ids-ucs.txt");
        if (idsPath != null) {
            idsParser.parse(idsPath, charIdMapper, componentToCompounds, compoundToComponents);
        } else {
            logger.warn("IDS file not found in {}", dataDirectory);
        }

        logger.info("Data loading complete. Total unique characters: {}", charIdMapper.size());
        logger.info("  Definitions: {}", definitions.size());
        logger.info("  Frequencies: {}", frequencies.size());
        logger.info("  Component relationships: {}", componentToCompounds.size());

        return new StaticData(charIdMapper, definitions, frequencies, 
                            componentToCompounds, compoundToComponents);
    }

    /**
     * Finds a file in the data directory, trying multiple possible names.
     * @param directory the directory to search
     * @param names possible file names to try
     * @return Path to the first file found, or null if none exist
     */
    private Path findFile(Path directory, String... names) {
        for (String name : names) {
            Path path = directory.resolve(name);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }
}
