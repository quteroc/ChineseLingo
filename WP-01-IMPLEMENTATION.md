# ChineseLingo - Stage 1 (WP-01) Implementation

## Overview

This document describes the Stage 1 (WP-01: Foundation and Data Ingestion) implementation for ChineseLingo, a Java application for learning Chinese characters intelligently.

## Architecture

### Project Structure

```
ChineseLingo/
├── pom.xml                          # Maven build configuration
├── src/
│   ├── main/java/com/chineselingo/
│   │   ├── data/
│   │   │   ├── CharIdMapper.java    # Character-to-ID mapping
│   │   │   ├── DataManager.java     # Data loading facade
│   │   │   └── StaticData.java      # Immutable parsed data container
│   │   └── parser/
│   │       ├── CEDICTParser.java    # CC-CEDICT dictionary parser
│   │       ├── IDSParser.java       # IDS-UCS decomposition parser
│   │       └── SUBTLEXParser.java   # Frequency data parser
│   └── test/
│       ├── java/com/chineselingo/
│       │   ├── data/
│       │   │   ├── DataManagerTest.java
│       │   │   └── MemoryBudgetTest.java
│       │   └── parser/
│       │       ├── CEDICTParserTest.java
│       │       ├── IDSParserTest.java
│       │       └── SUBTLEXParserTest.java
│       └── resources/fixtures/
│           ├── cedict_ts.u8         # Test CEDICT data
│           ├── ids.txt              # Test IDS data
│           └── subtlex.txt          # Test frequency data
```

### Core Components

#### 1. CharIdMapper
**Purpose:** Thread-safe mapper that assigns stable integer IDs to Chinese characters.

**Key Features:**
- Concurrent ID assignment using `ConcurrentHashMap`
- Bidirectional mapping (char ↔ ID)
- Read-only views for immutability after loading

**Usage:**
```java
CharIdMapper mapper = new CharIdMapper();
int id = mapper.getId("木");  // Get or create ID
String char = mapper.getChar(id);  // Reverse lookup
```

#### 2. StaticData
**Purpose:** Immutable container for all parsed data structures.

**Data Structures:**
- `Int2ObjectOpenHashMap<String> definitions` - Character definitions (ID → definition)
- `Int2IntOpenHashMap frequencies` - Character frequencies (ID → frequency count)
- `Int2ObjectOpenHashMap<IntArrayList> componentToCompounds` - Component relationships
- `Int2ObjectOpenHashMap<IntArrayList> compoundToComponents` - Compound decompositions

**Memory Efficiency:**
- Uses fastutil primitive collections to minimize object overhead
- String definitions stored as single compact strings
- Integer IDs instead of String keys

#### 3. DataManager
**Purpose:** Facade for orchestrating data loading and parsing.

**Responsibilities:**
- Locates data files in configurable directory
- Coordinates parsing across all parsers
- Returns immutable `StaticData` object

**Usage:**
```java
DataManager manager = new DataManager(Paths.get("data"));
StaticData data = manager.loadData();
```

**File Discovery:**
- Tries multiple common filenames (e.g., `cedict_ts.u8`, `cedict.txt`)
- Logs warnings for missing files
- Continues loading available data

#### 4. CEDICTParser
**Purpose:** Parses CC-CEDICT format dictionary files.

**Format:**
```
林 林 [lin2] /forest/grove/
```

**Behavior:**
- Skips comment lines (starting with `#`)
- Extracts traditional, simplified, pinyin, and definitions
- **WP-01 Focus:** Single-character entries only
- Stores definitions as compact slash-delimited strings
- Uses `indexOf`/`substring` instead of regex for performance

#### 5. SUBTLEXParser
**Purpose:** Parses character frequency files.

**Supported Formats:**
- Tab-separated: `character\tfrequency`
- Comma-separated: `character,frequency`
- Auto-detects and skips header rows

**Behavior:**
- Single-character entries only
- Stores frequency as integer count

#### 6. IDSParser
**Purpose:** Parses IDS-UCS (Ideographic Description Sequence) files.

**Format:**
```
U+6797	林	⿰木木
```

**Behavior:**
- Extracts character decompositions
- **Treats as bag-of-components** - ignores IDC operators (⿰, ⿱, etc.)
- Builds bidirectional relationships:
  - Component → Compounds (e.g., 木 → [林, 森])
  - Compound → Components (e.g., 林 → [木, 木])
- Handles Unicode properly (multi-byte characters, IDC operators in U+2FF0-U+2FFF)

## Dependencies

### Production Dependencies
- **fastutil 8.5.12** - Memory-efficient primitive collections
- **Jackson 2.16.0** - JSON parsing (for future profile serialization)
- **RoaringBitmap 1.0.1** - Efficient bitmap operations (reserved for WP-03)
- **SLF4J 2.0.9** - Logging facade
- **slf4j-simple** - Simple logging backend

### Test Dependencies
- **JUnit 5.10.1** - Testing framework

### Java Version
- **Java 17** (configured in pom.xml)

## Testing

### Test Coverage

**Unit Tests (11 total):**
- `DataManagerTest` - 3 tests for data loading
- `MemoryBudgetTest` - 2 tests for memory validation
- `CEDICTParserTest` - 2 tests for dictionary parsing
- `SUBTLEXParserTest` - 2 tests for frequency parsing
- `IDSParserTest` - 2 tests for IDS parsing

**Test Fixtures:**
All tests use small fixture files in `src/test/resources/fixtures/`:
- 5 dictionary entries (cedict_ts.u8)
- 6 frequency entries (subtlex.txt)
- 4 IDS decompositions (ids.txt)

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=MemoryBudgetTest

# Full clean build and verify
mvn clean verify
```

### Memory Budget

**Test Results:**
- Memory usage with fixtures: ~2 MB
- Budget threshold: 300 MB (very conservative)
- Test validates efficient primitive collection usage

## Building

```bash
# Clean build
mvn clean compile

# Build with tests
mvn clean verify

# Package JAR
mvn package
```

## Performance Considerations

### Parsing Optimizations
1. **Avoid regex in hot paths** - Uses `indexOf`/`substring` instead
2. **Minimal object allocation** - Reuses strings, avoids intermediate objects
3. **Buffered reading** - Uses `BufferedReader` for file I/O
4. **Primitive collections** - fastutil reduces memory overhead

### Memory Efficiency
- Integer IDs (4 bytes) instead of String keys (40+ bytes)
- Single compact definition strings (no String arrays)
- `IntArrayList` for component lists (4 bytes per entry vs. 16+ for `ArrayList<Integer>`)
- Expected RAM usage: ~100-200 MB for full datasets (within 500 MB requirement)

## Data Files (Production)

Expected files in `data/` directory:

1. **cedict_ts.u8** (or cedict.txt)
   - CC-CEDICT dictionary
   - Format: `traditional simplified [pinyin] /definition1/definition2/`

2. **subtlex.txt** (or frequency.txt)
   - Character frequency data
   - Format: `character<tab>frequency` or CSV with header

3. **ids.txt** (or ids-ucs.txt)
   - IDS decomposition data
   - Format: `U+XXXX<tab>character<tab>decomposition`

## Implementation Notes

### WP-01 Scope Decisions

**Single-character focus:**
- All parsers currently process single-character entries only
- Multi-character words/compounds ignored for WP-01
- This simplifies the component relationship graph
- Future: WP-02+ can extend to handle multi-character entries

**Definition storage:**
- Stored as raw slash-delimited strings (e.g., `/forest/grove/`)
- Minimal processing during parsing
- Future: Can split and process during display if needed

**Component relationships:**
- IDS treated as bag-of-components
- IDC operators ignored (⿰, ⿱, ⿲, ⿳, etc.)
- Simplified graph structure for WP-01
- Future: Can preserve operator information for advanced queries

### Thread Safety

**During Loading:**
- `CharIdMapper` uses `ConcurrentHashMap` for thread-safe ID assignment
- Parsers can potentially run in parallel (not implemented yet)

**After Loading:**
- `StaticData` and all contained structures are effectively immutable
- Safe for concurrent read access from multiple threads

### Error Handling

**Parsing errors:**
- Individual line parse failures logged as warnings
- Parsing continues for remaining lines
- Ensures partial data loads succeed

**Missing files:**
- Logged as warnings, not errors
- Application continues with available data
- Useful for development and testing

## Future Extensions (WP-02 to WP-06)

This foundation supports:

- **WP-02:** Profile system (save/load learned characters)
- **WP-03:** Recommendation engine using component relationships
- **WP-04:** Multi-character word support
- **WP-05:** Sentence mining and context extraction
- **WP-06:** UI layer (JavaFX/Swing or web-based)

## Acceptance Criteria Status

✅ **Project builds successfully**
- Maven build completes without errors
- All dependencies resolved

✅ **Unit tests pass**
- 11/11 tests passing
- Coverage includes parsers, data manager, and memory validation

✅ **DataManager can load fixture datasets**
- Loads all three data sources
- Returns populated `StaticData` with correct mappings

✅ **Parsers documented with expected formats**
- Each parser has JavaDoc explaining input format
- Test fixtures demonstrate correct format
- Inline comments explain parsing logic

## License

See LICENSE file in repository root.
