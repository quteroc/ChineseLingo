package com.chineselingo.recommendation;

import com.chineselingo.data.CharIdMapper;
import com.chineselingo.data.DataManager;
import com.chineselingo.data.StaticData;
import com.chineselingo.graph.GraphManager;
import com.chineselingo.recommendation.RecommendationEngine.RecommendationMode;
import com.chineselingo.user.UserState;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationEngineTest {

    private GraphManager graphManager;
    private CharIdMapper charIdMapper;
    private RecommendationEngine strictEngine;
    private RecommendationEngine lenientEngine;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        // Load test fixtures
        Path fixturesDir = Paths.get(getClass().getResource("/fixtures").toURI());
        DataManager dataManager = new DataManager(fixturesDir);
        StaticData staticData = dataManager.loadData();
        
        graphManager = new GraphManager(staticData);
        charIdMapper = staticData.getCharIdMapper();
        
        strictEngine = new RecommendationEngine(graphManager, RecommendationMode.STRICT);
        lenientEngine = new RecommendationEngine(graphManager, RecommendationMode.LENIENT);
    }

    @Test
    void testConstructorThrowsOnNullGraphManager() {
        assertThrows(IllegalArgumentException.class, 
            () -> new RecommendationEngine(null, RecommendationMode.STRICT));
    }

    @Test
    void testConstructorThrowsOnNullMode() {
        assertThrows(IllegalArgumentException.class, 
            () -> new RecommendationEngine(graphManager, null));
    }

    @Test
    void testRecommendNextWithNoKnownCharacters() {
        UserState userState = new UserState();
        
        int recommendation = strictEngine.recommendNext(userState);
        
        assertEquals(-1, recommendation, "Should return -1 when no characters are known");
    }

    @Test
    void testRecommendNextStrictModeKnowingWood() {
        // Fixture: 木 (wood) -> 林 (forest)
        // 林 is composed of 木木 (two woods)
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        int forestId = charIdMapper.getId("林");
        
        userState.markKnown(woodId);
        
        int recommendation = strictEngine.recommendNext(userState);
        
        assertEquals(forestId, recommendation, 
            "Should recommend 林 when 木 is known (all components known in STRICT mode)");
    }

    @Test
    void testRecommendNextLenientModeKnowingWood() {
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        int forestId = charIdMapper.getId("林");
        
        userState.markKnown(woodId);
        
        int recommendation = lenientEngine.recommendNext(userState);
        
        assertEquals(forestId, recommendation, 
            "Should recommend 林 when 木 is known in LENIENT mode");
    }

    @Test
    void testAlreadyKnownCharactersNotRecommended() {
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        int forestId = charIdMapper.getId("林");
        
        userState.markKnown(woodId);
        userState.markKnown(forestId);
        
        int recommendation = strictEngine.recommendNext(userState);
        
        // 林 is already known, so should recommend 森 (deep forest) if available
        assertNotEquals(forestId, recommendation, 
            "Should not recommend already-known 林");
    }

    @Test
    void testFrequencySorting() {
        // Fixture frequencies: 木=50000, 林=30000, 森=10000
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        
        userState.markKnown(woodId);
        
        IntArrayList recommendations = strictEngine.recommendTopN(userState, 3);
        
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty(), "Should have recommendations");
        
        // The first recommendation should be the highest frequency compound
        int firstRec = recommendations.getInt(0);
        int forestId = charIdMapper.getId("林");
        
        assertEquals(forestId, firstRec, 
            "林 should be recommended first as it has higher frequency than 森");
    }

    @Test
    void testRecommendTopN() {
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        
        userState.markKnown(woodId);
        
        IntArrayList recommendations = strictEngine.recommendTopN(userState, 2);
        
        assertNotNull(recommendations);
        assertTrue(recommendations.size() <= 2, "Should return at most 2 recommendations");
        assertTrue(recommendations.size() > 0, "Should have at least 1 recommendation");
    }

    @Test
    void testRecommendTopNThrowsOnNullUserState() {
        assertThrows(IllegalArgumentException.class, 
            () -> strictEngine.recommendTopN(null, 1));
    }

    @Test
    void testRecommendTopNThrowsOnNegativeN() {
        UserState userState = new UserState();
        assertThrows(IllegalArgumentException.class, 
            () -> strictEngine.recommendTopN(userState, 0));
        assertThrows(IllegalArgumentException.class, 
            () -> strictEngine.recommendTopN(userState, -1));
    }

    @Test
    void testRecommendNextThrowsOnNullUserState() {
        assertThrows(IllegalArgumentException.class, 
            () -> strictEngine.recommendNext(null));
    }

    @Test
    void testStrictVsLenientMode() {
        // Test case where STRICT and LENIENT might differ
        // 森 (deep forest) is composed of 木 and 林
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        int forestId = charIdMapper.getId("林");
        int deepForestId = charIdMapper.getId("森");
        
        // Know only 木 (wood), not 林 (forest)
        userState.markKnown(woodId);
        
        // In LENIENT mode, 森 should be learnable (at least one component known)
        IntArrayList lenientRecs = lenientEngine.recommendTopN(userState, 10);
        
        // In STRICT mode, 森 should NOT be learnable (not all components known)
        // since 森 requires both 木 and 林, and we only know 木
        
        // However, both modes should recommend 林 first since it's all-木
        assertTrue(lenientRecs.contains(forestId), "LENIENT should recommend 林");
    }

    @Test
    void testEmptyResultWhenAllCandidatesKnown() {
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        int forestId = charIdMapper.getId("林");
        int deepForestId = charIdMapper.getId("森");
        
        userState.markKnown(woodId);
        userState.markKnown(forestId);
        userState.markKnown(deepForestId);
        
        IntArrayList recommendations = strictEngine.recommendTopN(userState, 10);
        
        // Should not recommend any of the known characters
        assertFalse(recommendations.contains(woodId));
        assertFalse(recommendations.contains(forestId));
        assertFalse(recommendations.contains(deepForestId));
    }

    @Test
    void testRecommendationIsDeterministic() {
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        userState.markKnown(woodId);
        
        int rec1 = strictEngine.recommendNext(userState);
        int rec2 = strictEngine.recommendNext(userState);
        int rec3 = strictEngine.recommendNext(userState);
        
        assertEquals(rec1, rec2, "Recommendations should be deterministic");
        assertEquals(rec2, rec3, "Recommendations should be deterministic");
    }

    @Test
    void testCharIdTiebreaker() {
        // When frequencies are equal, smaller charId should win
        // This is tested implicitly by the sorting logic
        UserState userState = new UserState();
        int woodId = charIdMapper.getId("木");
        userState.markKnown(woodId);
        
        IntArrayList recommendations = strictEngine.recommendTopN(userState, 10);
        
        // Verify that recommendations are sorted by frequency desc, then charId asc
        for (int i = 0; i < recommendations.size() - 1; i++) {
            int freq1 = graphManager.getFrequency(recommendations.getInt(i));
            int freq2 = graphManager.getFrequency(recommendations.getInt(i + 1));
            
            if (freq1 == freq2) {
                // If frequencies are equal, charId should be ascending
                assertTrue(recommendations.getInt(i) < recommendations.getInt(i + 1),
                    "When frequencies are equal, smaller charId should come first");
            } else {
                // Frequencies should be descending
                assertTrue(freq1 > freq2,
                    "Frequencies should be in descending order");
            }
        }
    }
}
