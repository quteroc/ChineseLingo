package com.chineselingo.learning.verifying;

import com.chineselingo.data.CharIdMapper;
import com.chineselingo.learning.common.dto.CharacterCandidate;
import com.chineselingo.user.UserState;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyingService {
    private static final int MAX_TEST_SIZE = 8;
    private static final String DELIMS = "[/\\.\\;\\(\\) ]";
    private final UserState userState;
    final CharIdMapper charIdMapper;
    final Int2ObjectOpenHashMap<String> definitions;

    private List<Integer> testSet = new ArrayList<>();
    private int index = 0;
    private CharacterCandidate current;

    public VerifyingService(final UserState userState, final CharIdMapper charIdMapper, final Int2ObjectOpenHashMap<String> definitions) {
        this.userState = userState;
        this.charIdMapper = charIdMapper;
        this.definitions = definitions;
    }

    public CharacterCandidate getCurrent() {
        if (testSet.isEmpty()) {
            return null;
        }
        int charId = testSet.get(index);
        if (current != null && charId == current.getCharId()) {return current;}
        String character = charIdMapper.getChar(charId);
        String meaning = definitions.get(charId);
        current = new CharacterCandidate(charId, character, meaning);
        return current;
    }

    public void startNewTest() {
        List<Integer> known = new ArrayList<>();

        BitSet bs = userState.getKnownChars();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            known.add(i);
        }
        Collections.shuffle(known);
        testSet = known.size() > MAX_TEST_SIZE
                ? known.subList(0, MAX_TEST_SIZE)
                : known;
        index = 0;
    }

    public void restartTest() {
        index = 0;
    }

    public boolean verify(String answer) {
        boolean success = true;
        if (testSet.isEmpty()) {
            success = false;
        }
        answer = answer.trim();
        if (answer.length() < 3) {
            success = false;
        }
        if (success) {
            String correctMeaning = current.getMeaning();
            String regex = DELIMS + Pattern.quote(answer) + DELIMS;
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(correctMeaning);
            success = matcher.find();
        }
        userState.recordReview(current.getCharId(), success, Instant.now().getEpochSecond());
        return success;
    }

    public boolean hasNext() {
        return index < testSet.size() - 1;
    }

    public void next() {
        if (hasNext()) {
            index++;
        }
    }

    public boolean isLast() {
        return index == testSet.size() - 1;
    }

    public String getCorrectAnswer () {
        return testSet.isEmpty() ? "" : definitions.get(testSet.get(index));
    }
}