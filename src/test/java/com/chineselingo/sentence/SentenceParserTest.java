package com.chineselingo.sentence;

import com.chineselingo.data.CharIdMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class SentenceParserTest {

    @Test
    void testParseFixture() throws IOException, URISyntaxException {
        Path fixturesDir = Paths.get(getClass().getResource("/fixtures").toURI());
        Path sentencePath = fixturesDir.resolve("sentences.tsv");
        
        // Pre-populate CharIdMapper with some characters
        CharIdMapper mapper = new CharIdMapper();
        mapper.getId("我");
        mapper.getId("是");
        mapper.getId("中");
        mapper.getId("国");
        mapper.getId("人");
        mapper.getId("你");
        mapper.getId("好");
        mapper.getId("爱");
        mapper.getId("木");
        mapper.getId("林");
        mapper.getId("森");
        mapper.getId("的");
        mapper.getId("一");
        mapper.getId("他");
        mapper.getId("喜");
        mapper.getId("欢");
        mapper.getId("学");
        mapper.getId("习");
        mapper.getId("文");
        
        SentenceStore store = new SentenceStore();
        InvertedIndex index = new InvertedIndex();
        
        SentenceParser parser = new SentenceParser();
        parser.parse(sentencePath, mapper, store, index);
        
        // Should parse only Mandarin sentences (cmn)
        // Valid: 1, 2, 3, 5, 7, 8, 9, 11 (8 sentences with language cmn)
        // Filtered by length: sentence 6 (10 chars) and 11 (9 chars) are within limits
        // So we should have multiple sentences
        assertTrue(store.size() > 0, "Should parse at least some sentences");
        
        // Check that index was built
        assertTrue(index.size() > 0, "Index should contain some characters");
    }

    @Test
    void testFiltersByLanguage(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.tsv");
        Files.writeString(testFile, 
            "1\tcmn\t你好\n" +
            "2\teng\tHello\n" +
            "3\tjpn\tこんにちは\n" +
            "4\tcmn\t再见\n"
        );
        
        CharIdMapper mapper = new CharIdMapper();
        mapper.getId("你");
        mapper.getId("好");
        mapper.getId("再");
        mapper.getId("见");
        
        SentenceStore store = new SentenceStore();
        InvertedIndex index = new InvertedIndex();
        
        SentenceParser parser = new SentenceParser();
        parser.parse(testFile, mapper, store, index);
        
        // Should only parse sentences 1 and 4 (cmn language)
        assertEquals(2, store.size());
    }

    @Test
    void testFiltersByLength(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.tsv");
        Files.writeString(testFile, 
            "1\tcmn\t你\n" +  // Too short (1 char)
            "2\tcmn\t你好\n" +  // Valid (2 chars)
            "3\tcmn\t一二三四五六七八九十一二三四五六七八九十一二三四五六\n" +  // Too long (>25 chars)
            "4\tcmn\t我爱中文\n"  // Valid (4 chars)
        );
        
        CharIdMapper mapper = new CharIdMapper();
        mapper.getId("你");
        mapper.getId("好");
        mapper.getId("我");
        mapper.getId("爱");
        mapper.getId("中");
        mapper.getId("文");
        
        SentenceStore store = new SentenceStore();
        InvertedIndex index = new InvertedIndex();
        
        SentenceParser parser = new SentenceParser();
        parser.parse(testFile, mapper, store, index);
        
        // Should only parse sentences 2 and 4 (length 2-25)
        assertEquals(2, store.size());
    }

    @Test
    void testTokenization(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.tsv");
        Files.writeString(testFile, "1\tcmn\t你好\n");
        
        CharIdMapper mapper = new CharIdMapper();
        int id1 = mapper.getId("你");
        int id2 = mapper.getId("好");
        
        SentenceStore store = new SentenceStore();
        InvertedIndex index = new InvertedIndex();
        
        SentenceParser parser = new SentenceParser();
        parser.parse(testFile, mapper, store, index);
        
        assertEquals(1, store.size());
        int[] tokens = store.tokens(0);
        assertNotNull(tokens);
        assertEquals(2, tokens.length);
        assertEquals(id1, tokens[0]);
        assertEquals(id2, tokens[1]);
    }

    @Test
    void testUnknownCharactersHandled(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.tsv");
        Files.writeString(testFile, "1\tcmn\t你好，世界！\n");
        
        CharIdMapper mapper = new CharIdMapper();
        int id1 = mapper.getId("你");
        int id2 = mapper.getId("好");
        // Don't map 世界 or punctuation
        
        SentenceStore store = new SentenceStore();
        InvertedIndex index = new InvertedIndex();
        
        SentenceParser parser = new SentenceParser();
        parser.parse(testFile, mapper, store, index);
        
        assertEquals(1, store.size());
        int[] tokens = store.tokens(0);
        assertNotNull(tokens);
        // Should have tokens for all characters (known and unknown)
        assertEquals(6, tokens.length);
        assertEquals(id1, tokens[0]);
        assertEquals(id2, tokens[1]);
        assertEquals(SentenceParser.UNKNOWN_ID, tokens[2]); // ，
        assertEquals(SentenceParser.UNKNOWN_ID, tokens[3]); // 世
        assertEquals(SentenceParser.UNKNOWN_ID, tokens[4]); // 界
        assertEquals(SentenceParser.UNKNOWN_ID, tokens[5]); // ！
    }

    @Test
    void testBuildsInvertedIndex(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.tsv");
        Files.writeString(testFile, 
            "1\tcmn\t你好\n" +
            "2\tcmn\t你是谁\n" +
            "3\tcmn\t好人\n"
        );
        
        CharIdMapper mapper = new CharIdMapper();
        int idYou = mapper.getId("你");
        int idGood = mapper.getId("好");
        int idIs = mapper.getId("是");
        int idWho = mapper.getId("谁");
        int idPerson = mapper.getId("人");
        
        SentenceStore store = new SentenceStore();
        InvertedIndex index = new InvertedIndex();
        
        SentenceParser parser = new SentenceParser();
        parser.parse(testFile, mapper, store, index);
        
        // Check inverted index
        var youSentences = index.getSentencesForChar(idYou);
        assertEquals(2, youSentences.getCardinality()); // Sentences 0 and 1
        assertTrue(youSentences.contains(0));
        assertTrue(youSentences.contains(1));
        
        var goodSentences = index.getSentencesForChar(idGood);
        assertEquals(2, goodSentences.getCardinality()); // Sentences 0 and 2
        assertTrue(goodSentences.contains(0));
        assertTrue(goodSentences.contains(2));
        
        var personSentences = index.getSentencesForChar(idPerson);
        assertEquals(1, personSentences.getCardinality()); // Only sentence 2
        assertTrue(personSentences.contains(2));
    }
}
