package com.chineselingo.app;

import com.chineselingo.data.CharIdMapper;
import com.chineselingo.data.DataManager;
import com.chineselingo.data.StaticData;
import com.chineselingo.graph.GraphManager;
import com.chineselingo.recommendation.RecommendationEngine;
import com.chineselingo.user.UserManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AppContext {
    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);

    private final UserManager userManager;
    private final RecommendationEngine recommendationEngine;
    private final StaticData staticData;

    public AppContext() throws IOException {
        userManager = new UserManager();
        DataManager dataManager = new DataManager();
        staticData = dataManager.loadData();
        recommendationEngine = new RecommendationEngine(new GraphManager(staticData), RecommendationEngine.RecommendationMode.LENIENT);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RecommendationEngine getRecommendationEngine() {
        return recommendationEngine;
    }

    public CharIdMapper getCharIdMapper() {
        return staticData.getCharIdMapper();
    }

    public Int2ObjectOpenHashMap<String> getDefinitions() {
        return staticData.getDefinitions();
    }
}