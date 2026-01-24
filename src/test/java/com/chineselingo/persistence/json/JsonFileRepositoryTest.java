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

        original.recordReview(10, true, now);
        original.recordReview(10, false, now + 5);
        original.recordReview(42, true, now + 10);
        original.markKnown(10);
        original.markKnown(42);

        // when
        repository.save(UserStateMapper.toDto(original));

        UserStateDto loadedDto = repository.load();
        UserState restored = UserStateMapper.fromDto(loadedDto);

        // then
        assertNotNull(restored);

        // --- knownChars ---
        assertTrue(restored.getKnownChars().get(10));
        assertTrue(restored.getKnownChars().get(42));

        // --- reviewHistory: charId=10 ---
        UserState.ReviewHistory h10 =
                restored.getReviewHistoryMap().get(10);

        assertNotNull(h10);
        assertEquals(2, h10.getViews());
        assertEquals(1, h10.getSuccesses());

        // --- reviewHistory: charId=42 ---
        UserState.ReviewHistory h42 =
                restored.getReviewHistoryMap().get(42);

        assertNotNull(h42);
        assertEquals(1, h42.getViews());
        assertEquals(1, h42.getSuccesses());
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
