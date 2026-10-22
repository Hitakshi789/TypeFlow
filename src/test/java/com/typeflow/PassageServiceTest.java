package com.typeflow;

import com.typeflow.dto.PassageDto;
import com.typeflow.service.PassageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PassageServiceTest {

    private PassageService passageService;

    @BeforeEach
    void setUp() {
        passageService = new PassageService();
    }

    @Test
    @DisplayName("Verify that initial passage contains at least 350 words, ensuring words never run out in 30s, 60s, or 120s modes")
    void testPassageLengthSufficientForEnduranceTest() {
        PassageDto passage = passageService.getRandomPassage(null);
        assertNotNull(passage);
        assertNotNull(passage.getText());

        String[] words = passage.getText().trim().split("\\s+");
        assertTrue(words.length >= 350, "Initial passage must have at least 350 words (found " + words.length + ")");
    }

    @Test
    @DisplayName("Verify that generateWords produces correct count and prevents consecutive identical words")
    void testGenerateWordsNoConsecutiveDuplicates() {
        List<String> words = passageService.generateWords(200);
        assertEquals(200, words.size());

        for (int i = 0; i < words.size() - 1; i++) {
            assertNotEquals(words.get(i).toLowerCase(), words.get(i + 1).toLowerCase(),
                    "Consecutive words must not be identical at index " + i);
        }
    }

    @Test
    @DisplayName("Verify getMoreWords returns non-empty stream batch for prefetching")
    void testGetMoreWordsPrefetch() {
        List<String> batch = passageService.getMoreWords(100);
        assertEquals(100, batch.size());
        for (String w : batch) {
            assertNotNull(w);
            assertFalse(w.isBlank());
        }
    }
}
