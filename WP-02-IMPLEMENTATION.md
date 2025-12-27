# ChineseLingo - Stage 2 (WP-02) Implementation

## Overview

This document describes the Stage 2 (WP-02: Graph Core and Recommendation Logic) implementation for ChineseLingo, building upon the WP-01 foundation.

## Components Implemented

### 1. GraphManager (`com.chineselingo.graph.GraphManager`)

**Purpose:** Encapsulates structural graph data derived from WP-01 parsing and provides safe access to component-compound relationships.

**Key Features:**
- Wraps `StaticData` for clean separation of concerns
- Returns defensive copies to ensure immutability
- Provides bidirectional graph queries:
  - `getCompoundsForComponent(int)` - Find all compounds containing a component
  - `getComponentsForCompound(int)` - Find all components of a compound
- Efficient frequency lookups via `getFrequency(int)`

**Memory Safety:**
- All returned `IntArrayList` instances are clones, preventing external modification
- Null handling for non-existent characters

### 2. UserState (`com.chineselingo.user.UserState`)

**Purpose:** Represents user learning state and progress with memory-efficient structures.

**Data Structures:**
- `BitSet knownChars` - Compact representation of known character IDs
- `Int2ObjectOpenHashMap<ReviewHistory>` - Per-character review statistics using fastutil

**Key Methods:**
- `isKnown(int charId)` - Check if character is known
- `markKnown(int charId)` - Mark character as known
- `recordReview(int charId, boolean success, long nowEpochSeconds)` - Track review events

**ReviewHistory Inner Class:**
- `views` (int) - Total number of reviews
- `successes` (int) - Number of successful reviews
- `lastReviewedEpochSeconds` (long) - Timestamp of last review

**Memory Efficiency:**
- `BitSet` uses ~1 bit per character for known/unknown state
- Fastutil primitive maps avoid boxing overhead
- Review history only created for reviewed characters

### 3. RecommendationEngine (`com.chineselingo.recommendation.RecommendationEngine`)

**Purpose:** Suggests optimal next characters to learn based on known components and frequency.

**Recommendation Modes:**
- **STRICT**: Compound learnable only if ALL components are known
  - Example: 森 (forest) requires both 木 and 林
- **LENIENT**: Compound learnable if at least ONE component is known
  - Example: 森 learnable if user knows 木 OR 林

**Algorithm:**
1. Collect all candidates by finding compounds of known components
2. Filter out already-known characters
3. Apply mode-specific learnability rules
4. Sort by:
   - Primary: Frequency (descending) - most common first
   - Tiebreaker: Character ID (ascending) - deterministic ordering

**Key Methods:**
- `recommendNext(UserState)` - Returns single best recommendation or -1
- `recommendTopN(UserState, int n)` - Returns top N recommendations

**Correctness Properties:**
- Deterministic: Same input always produces same output
- Complete: Never recommends already-known characters
- Efficient: Uses hashset for O(1) candidate checking

## Test Coverage

### GraphManagerTest (10 tests)
- Constructor validation
- Component-to-compounds mapping
- Compound-to-components mapping
- Defensive copy verification
- Null handling for non-existent characters
- Frequency lookups

### UserStateTest (9 tests)
- Known character tracking
- Review history recording
- Multiple characters and reviews
- Initial state validation
- ReviewHistory data structure

### RecommendationEngineTest (15 tests)
- Constructor validation
- STRICT vs LENIENT mode differences
- Frequency-based sorting
- CharId tiebreaker
- Known character filtering
- Edge cases (no known chars, all chars known)
- Deterministic behavior validation
- Top-N recommendations

**Total: 45 tests** (34 new tests for WP-02 + 11 from WP-01)

## Demonstration

A demonstration program (`RecommendationDemo`) shows the system in action:

```
User knows: 木 (wood)

STRICT Mode Recommendations:
  1. 林 (forest) - freq=30000
     Components: 木

LENIENT Mode Recommendations:
  1. 林 (forest) - freq=30000
  2. 森 (deep forest) - freq=10000
     Components: 木, 林

After learning 林:
Both modes recommend 森 (now all components known)
```

## Integration with WP-01

WP-02 cleanly builds on WP-01 infrastructure:
- Uses `StaticData` as immutable data source
- Leverages `CharIdMapper` for character-to-ID translation
- Reuses component graphs built by `IDSParser`
- Accesses frequency data from `SUBTLEXParser`

No changes to WP-01 code were necessary.

## Design Decisions

### 1. Defensive Copies in GraphManager
**Rationale:** Ensures immutability guarantees even if caller modifies returned lists. Small performance cost acceptable for safety.

### 2. BitSet for Known Characters
**Rationale:** Memory-efficient (~1 bit per char vs. 16+ bytes per Integer in HashSet). Fast set/get operations.

### 3. Separate ReviewHistory Class
**Rationale:** Encapsulates review statistics, allows future extensions (e.g., SRS intervals), type-safe access.

### 4. Two Recommendation Modes
**Rationale:** STRICT ensures solid foundation before learning compounds. LENIENT allows faster progression. User/application can choose strategy.

### 5. Frequency-First Sorting
**Rationale:** Learning high-frequency characters first maximizes practical utility. CharId tiebreaker ensures determinism.

## Memory Characteristics

**Estimated Overhead per User:**
- `BitSet knownChars`: ~1 KB for 8000 characters
- Review history: ~24 bytes per reviewed character
- Total for active learner (~500 reviewed chars): ~13 KB

**Scalability:** Can support thousands of concurrent users with minimal memory impact.

## Performance Characteristics

**Recommendation Generation:**
- Time Complexity: O(K × C + C log C) where K = known chars, C = candidates
- Typical: K < 1000, C < 100, very fast (< 1ms)
- Bottleneck: Sorting candidates (negligible for realistic sizes)

**Graph Queries:**
- `getCompoundsForComponent`: O(1) lookup + O(n) clone (n = result size)
- `getComponentsForCompound`: O(1) lookup + O(m) clone (m = result size)
- Both very fast for typical cases (n, m < 10)

## Future Extensions (WP-03+)

This implementation is structured to support:
- **WP-03**: Sentence mining - can track characters in context
- **WP-04**: Spaced repetition - ReviewHistory ready for SRS intervals
- **WP-05**: Profile persistence - UserState serializable to JSON
- **WP-06**: UI integration - Clean separation of concerns

## Acceptance Criteria Status

✅ **Build passes** - Maven build successful
✅ **All tests pass** - 45/45 tests passing
✅ **Recommendation works on fixtures** - Demo validates correct behavior
✅ **Deterministic recommendations** - Tests verify consistency
✅ **Memory-efficient structures** - fastutil + BitSet used throughout
✅ **Clean integration with WP-01** - No changes to existing code
✅ **Structured for WP-03** - Modular design supports future work

## Files Added

**Source Files:**
- `src/main/java/com/chineselingo/graph/GraphManager.java`
- `src/main/java/com/chineselingo/user/UserState.java`
- `src/main/java/com/chineselingo/recommendation/RecommendationEngine.java`

**Test Files:**
- `src/test/java/com/chineselingo/graph/GraphManagerTest.java`
- `src/test/java/com/chineselingo/user/UserStateTest.java`
- `src/test/java/com/chineselingo/recommendation/RecommendationEngineTest.java`
- `src/test/java/com/chineselingo/demo/RecommendationDemo.java` (demo only)

**Total:** 6 production files + 1 demo = 7 new files

## Build and Test

```bash
# Clean build
mvn clean compile

# Run all tests
mvn test

# Run full verification
mvn clean verify

# Run demo
mvn exec:java -Dexec.mainClass="com.chineselingo.demo.RecommendationDemo" \
              -Dexec.classpathScope=test
```

## Conclusion

WP-02 successfully implements the pedagogical "brain" for ChineseLingo:
- Graph-based character relationships from IDS data
- Memory-efficient user state tracking
- Intelligent recommendation engine with configurable strategies

The implementation is minimal, focused, and ready for the next stage of development.
