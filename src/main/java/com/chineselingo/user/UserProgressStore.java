package com.chineselingo.user;

import com.chineselingo.data.CharIdMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Loads and saves user progress (known characters) to a JSON file (postepy.json).
 */
public final class UserProgressStore {
    public static final String DEFAULT_FILENAME = "postepy.json";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private UserProgressStore() {
    }

    public static void save(Path file, UserState userState, CharIdMapper charIdMapper) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        if (userState == null) {
            throw new IllegalArgumentException("userState cannot be null");
        }
        if (charIdMapper == null) {
            throw new IllegalArgumentException("charIdMapper cannot be null");
        }

        List<String> knownChars = new ArrayList<>();
        BitSet known = userState.getKnownChars();
        for (int charId = known.nextSetBit(0); charId >= 0; charId = known.nextSetBit(charId + 1)) {
            String ch = charIdMapper.getChar(charId);
            if (ch != null) {
                knownChars.add(ch);
            }
        }

        UserProgressDto dto = new UserProgressDto(knownChars);

        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), dto);
    }

    public static UserState load(Path file, CharIdMapper charIdMapper) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        if (charIdMapper == null) {
            throw new IllegalArgumentException("charIdMapper cannot be null");
        }
        if (!Files.exists(file)) {
            throw new IOException("Progress file does not exist: " + file);
        }

        UserProgressDto dto = objectMapper.readValue(file.toFile(), UserProgressDto.class);

        UserState userState = new UserState();
        if (dto.knownChars != null) {
            for (String ch : dto.knownChars) {
                if (ch == null || ch.isBlank()) {
                    continue;
                }
                int id = charIdMapper.getId(ch);
                userState.markKnown(id);
            }
        }

        return userState;
    }

    public static final class UserProgressDto {
        public List<String> knownChars;

        public UserProgressDto() {
        }

        public UserProgressDto(List<String> knownChars) {
            this.knownChars = knownChars;
        }
    }
}
