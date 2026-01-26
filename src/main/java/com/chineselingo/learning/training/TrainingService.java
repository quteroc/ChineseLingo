package com.chineselingo.learning.training;

import com.chineselingo.data.CharIdMapper;
import com.chineselingo.learning.common.dto.CharacterCandidate;
import com.chineselingo.recommendation.RecommendationEngine;
import com.chineselingo.user.UserState;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class TrainingService {

    private final UserState userState;
    private final RecommendationEngine recommendationEngine;
    private final CharIdMapper charIdMapper;
    final Int2ObjectOpenHashMap<String> definitions;

    private CharacterCandidate current;

    public TrainingService(final UserState userState, final RecommendationEngine recommendationEngine,
                           final CharIdMapper charIdMapper, final Int2ObjectOpenHashMap<String> definitions) {
        this.userState = userState;
        this.recommendationEngine = recommendationEngine;
        this.charIdMapper = charIdMapper;
        this.definitions = definitions;
    }

    /** Pobiera pierwszy / kolejny znak do nauki */
    public CharacterCandidate nextSign() {
        int charId = recommendationEngine.recommendNext(userState);
        String character = charIdMapper.getChar(charId);
        String meaning = definitions.get(charId);
        current = new CharacterCandidate(charId, character, meaning);
        return current;
    }

    /** Użytkownik kliknął "Got it" */
    public void markCurrentAsKnown() {
        if (current == null) {
            throw new IllegalStateException("No current character");
        }
        userState.markKnown(current.getCharId());
    }

    public CharacterCandidate getCurrent() {
        return current;
    }
}