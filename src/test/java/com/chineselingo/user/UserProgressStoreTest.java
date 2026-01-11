package com.chineselingo.user;

import com.chineselingo.data.DataManager;
import com.chineselingo.data.StaticData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class UserProgressStoreTest {

    @Test
    void saveAndLoad_roundTripKnownCharacters() throws IOException, URISyntaxException {
        Path fixturesDir = getFixturesDir();
        Path tempDir = Files.createTempDirectory("chineselingo-progress-");

        // Copy fixtures (dictionary/frequency/components) into a writable temp folder.
        copyFixture(tempDir, fixturesDir, "cedict_ts.u8");
        copyFixture(tempDir, fixturesDir, "subtlex.txt");
        copyFixture(tempDir, fixturesDir, "ids.txt");

        DataManager dataManager = new DataManager(tempDir);
        StaticData staticData = dataManager.loadData();
        var mapper = staticData.getCharIdMapper();

        UserState original = new UserState();
        original.markKnown(mapper.getId("木"));
        original.markKnown(mapper.getId("林"));

        Path progressFile = tempDir.resolve(UserProgressStore.DEFAULT_FILENAME);
        UserProgressStore.save(progressFile, original, mapper);

        UserState loaded = UserProgressStore.load(progressFile, mapper);
        assertTrue(loaded.isKnown(mapper.getId("木")));
        assertTrue(loaded.isKnown(mapper.getId("林")));
        assertFalse(loaded.isKnown(mapper.getId("森")));

        // Change progress and ensure it persists.
        loaded.markKnown(mapper.getId("森"));
        UserProgressStore.save(progressFile, loaded, mapper);

        UserState loadedAgain = UserProgressStore.load(progressFile, mapper);
        assertTrue(loadedAgain.isKnown(mapper.getId("木")));
        assertTrue(loadedAgain.isKnown(mapper.getId("林")));
        assertTrue(loadedAgain.isKnown(mapper.getId("森")));
    }

    @Test
    void load_missingFileThrows() throws IOException, URISyntaxException {
        Path fixturesDir = getFixturesDir();
        Path tempDir = Files.createTempDirectory("chineselingo-progress-missing-");

        // Need a mapper; easiest is load static data from fixtures.
        DataManager dataManager = new DataManager(fixturesDir);
        StaticData staticData = dataManager.loadData();

        Path missing = tempDir.resolve(UserProgressStore.DEFAULT_FILENAME);
        assertThrows(IOException.class, () -> UserProgressStore.load(missing, staticData.getCharIdMapper()));
    }

    private static void copyFixture(Path targetDir, Path fixturesDir, String filename) throws IOException {
        Files.copy(fixturesDir.resolve(filename), targetDir.resolve(filename));
    }

    private static Path getFixturesDir() throws URISyntaxException {
        var url = UserProgressStoreTest.class.getResource("/fixtures");
        assertNotNull(url, "fixtures resource folder not found");
        return Paths.get(url.toURI());
    }
}
