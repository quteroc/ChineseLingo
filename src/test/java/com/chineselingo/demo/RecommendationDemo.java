package com.chineselingo.demo;

import com.chineselingo.data.DataManager;
import com.chineselingo.data.StaticData;
import com.chineselingo.graph.GraphManager;
import com.chineselingo.recommendation.RecommendationEngine;
import com.chineselingo.recommendation.RecommendationEngine.RecommendationMode;
import com.chineselingo.user.UserState;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Demonstration of the WP-02 recommendation system.
 * Shows how the system recommends characters based on known components.
 */
public class RecommendationDemo {
    
    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("=== ChineseLingo WP-02 Recommendation Demo ===\n");
        
        // Load data from test fixtures
        var fixturesPath = RecommendationDemo.class.getResource("/fixtures");
        if (fixturesPath == null) {
            System.err.println("Fixtures not found. Run from test classpath.");
            return;
        }
        
        DataManager dataManager = new DataManager(Paths.get(fixturesPath.toURI()));
        StaticData staticData = dataManager.loadData();
        
        GraphManager graphManager = new GraphManager(staticData);
        var mapper = staticData.getCharIdMapper();
        
        System.out.println("Loaded data:");
        System.out.println("  - Characters: " + mapper.size());
        System.out.println("  - Definitions: " + staticData.getDefinitions().size());
        System.out.println("  - Frequencies: " + staticData.getFrequencies().size());
        System.out.println();
        
        // Create a new user who knows 木 (wood)
        UserState userState = new UserState();
        int woodId = mapper.getId("木");
        userState.markKnown(woodId);
        
        System.out.println("User knows: 木 (wood)");
        System.out.println("  Definition: " + staticData.getDefinition(woodId));
        System.out.println("  Frequency: " + staticData.getFrequency(woodId));
        System.out.println();
        
        // Create recommendation engines in both modes
        RecommendationEngine strictEngine = new RecommendationEngine(graphManager, RecommendationMode.STRICT);
        RecommendationEngine lenientEngine = new RecommendationEngine(graphManager, RecommendationMode.LENIENT);
        
        // Get recommendations in STRICT mode
        System.out.println("=== STRICT Mode Recommendations ===");
        IntArrayList strictRecs = strictEngine.recommendTopN(userState, 3);
        displayRecommendations(strictRecs, staticData, mapper);
        
        // Get recommendations in LENIENT mode
        System.out.println("\n=== LENIENT Mode Recommendations ===");
        IntArrayList lenientRecs = lenientEngine.recommendTopN(userState, 3);
        displayRecommendations(lenientRecs, staticData, mapper);
        
        // Simulate learning 林 and getting new recommendations
        int forestId = mapper.getId("林");
        userState.markKnown(forestId);
        userState.recordReview(forestId, true, System.currentTimeMillis() / 1000);
        
        System.out.println("\n=== After Learning 林 (forest) ===");
        System.out.println("User now knows: 木, 林");
        
        System.out.println("\nNew STRICT recommendations:");
        strictRecs = strictEngine.recommendTopN(userState, 3);
        displayRecommendations(strictRecs, staticData, mapper);
        
        System.out.println("\nNew LENIENT recommendations:");
        lenientRecs = lenientEngine.recommendTopN(userState, 3);
        displayRecommendations(lenientRecs, staticData, mapper);
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void displayRecommendations(IntArrayList recommendations, 
                                              StaticData staticData, 
                                              com.chineselingo.data.CharIdMapper mapper) {
        if (recommendations.isEmpty()) {
            System.out.println("  No recommendations available");
            return;
        }
        
        for (int i = 0; i < recommendations.size(); i++) {
            int charId = recommendations.getInt(i);
            String character = mapper.getChar(charId);
            String definition = staticData.getDefinition(charId);
            int frequency = staticData.getFrequency(charId);
            
            System.out.printf("  %d. %s (ID=%d, freq=%d)%n", 
                i + 1, character, charId, frequency);
            System.out.printf("     Definition: %s%n", definition);
            
            // Show components
            IntArrayList components = staticData.getComponents(charId);
            if (components != null && !components.isEmpty()) {
                System.out.print("     Components: ");
                for (int j = 0; j < components.size(); j++) {
                    System.out.print(mapper.getChar(components.getInt(j)));
                    if (j < components.size() - 1) System.out.print(", ");
                }
                System.out.println();
            }
        }
    }
}
