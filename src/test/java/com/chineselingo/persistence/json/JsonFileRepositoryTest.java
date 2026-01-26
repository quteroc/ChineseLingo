package com.chineselingo.persistence.json;

import static org.junit.jupiter.api.Assertions.*;

import com.chineselingo.config.PathsConfig;
import com.chineselingo.user.UserState;
import com.chineselingo.user.dto.UserStateDto;
import com.chineselingo.user.mapper.UserStateMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndLoadUserState() throws Exception {
        // given
        ObjectMapper objectMapper = JsonMapperFactory.get(); // factory z mixinami
        Path file = PathsConfig.userStateJson();
        JsonFileRepository<UserStateDto> repository =
                new JsonFileRepository<>(
                        objectMapper,
                        file,
                        UserStateDto.class
                );

        UserState original = new UserState();
        long now = Instant.now().getEpochSecond();

        original.recordReview(26408, true, now);
        original.recordReview(26408, false, now + 5);
        original.recordReview(26408, true, now + 10);
        original.markKnown(26408);

        // when
        repository.save(UserStateMapper.toDto(original));

        UserStateDto loadedDto = repository.load();
        UserState restored = UserStateMapper.fromDto(loadedDto);

        // then
        assertNotNull(restored);

        // --- knownChars ---
        assertTrue(restored.getKnownChars().get(26408));
        assertFalse(restored.getKnownChars().get(42));

        // --- reviewHistory: charId=26408 ---
        UserState.ReviewHistory h26408 = restored.getReviewHistoryMap().get(26408);
        assertNotNull(h26408);
        assertEquals(3, h26408.getViews());
        assertEquals(2, h26408.getSuccesses());

        // --- reviewHistory: charId=42 ---
        UserState.ReviewHistory h42 = restored.getReviewHistoryMap().get(42);
        assertNull(h42);
    }

    @Test
    void shouldReturnNullWhenFileDoesNotExist() throws Exception {
        // given
        JsonFileRepository<UserStateDto> repository =
                new JsonFileRepository<>(
                        JsonMapperFactory.get(),
                        tempDir.resolve("missing.json"),
                        UserStateDto.class
                );

        // when
        UserStateDto result = repository.load();

        // then
        assertNull(result);
    }
}
