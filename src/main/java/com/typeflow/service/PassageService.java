package com.typeflow.service;

import com.typeflow.dto.PassageDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PassageService {

    // Curated high-frequency English typing word bank (10FastFingers style)
    private static final String[] COMMON_WORDS = {
        "the", "be", "of", "and", "a", "to", "in", "he", "have", "it",
        "that", "for", "they", "I", "with", "as", "not", "on", "she", "at",
        "by", "this", "we", "you", "do", "but", "his", "from", "they", "say",
        "her", "she", "or", "an", "will", "my", "one", "all", "would", "there",
        "their", "what", "so", "up", "out", "if", "about", "who", "get", "which",
        "go", "me", "when", "make", "can", "like", "time", "no", "just", "him",
        "know", "take", "people", "into", "year", "your", "good", "some", "could", "them",
        "see", "other", "than", "then", "now", "look", "only", "come", "its", "over",
        "think", "also", "back", "after", "use", "two", "how", "our", "work", "first",
        "well", "way", "even", "new", "want", "because", "any", "these", "give", "day",
        "most", "us", "great", "between", "need", "large", "under", "might", "never", "thought",
        "world", "life", "while", "last", "right", "state", "place", "still", "light", "house",
        "flow", "speed", "test", "every", "keystroke", "counts", "focus", "mind", "quick", "clean",
        "found", "night", "high", "many", "point", "home", "water", "room", "mother", "area",
        "money", "story", "fact", "month", "lot", "right", "study", "book", "eye", "job",
        "word", "business", "issue", "side", "kind", "head", "far", "black", "long", "both",
        "little", "house", "yes", "since", "around", "friend", "father", "until", "power", "hour",
        "game", "often", "yet", "line", "end", "member", "law", "car", "city", "almost",
        "include", "continue", "set", "later", "community", "name", "five", "once", "white", "least",
        "learn", "change", "lead", "understand", "watch", "follow", "stop", "face", "create", "speak",
        "read", "allow", "add", "spend", "grow", "open", "walk", "win", "offer", "remember",
        "love", "consider", "appear", "buy", "wait", "serve", "die", "send", "expect", "build",
        "stay", "fall", "cut", "reach", "kill", "remain", "suggest", "raise", "pass", "sell",
        "require", "report", "decide", "pull", "return", "explain", "hope", "develop", "carry", "break",
        "receive", "agree", "support", "hit", "produce", "eat", "cover", "catch", "draw", "choose",
        "cause", "point", "listen", "simple", "music", "human", "sound", "number", "part", "field",
        "space", "system", "group", "problem", "force", "river", "order", "early", "voice", "ready"
    };

    public PassageService() {}

    /**
     * Generates a continuous, randomized sequence of clean English words.
     * Prevents consecutive identical words so the typing rhythm stays fluid.
     */
    public List<String> generateWords(int count) {
        int targetCount = Math.max(count, 50);
        List<String> list = new ArrayList<>(targetCount);
        String lastWord = "";

        for (int i = 0; i < targetCount; i++) {
            String word;
            do {
                int idx = ThreadLocalRandom.current().nextInt(COMMON_WORDS.length);
                word = COMMON_WORDS[idx];
            } while (word.equalsIgnoreCase(lastWord) && COMMON_WORDS.length > 1);

            list.add(word);
            lastWord = word;
        }
        return list;
    }

    /**
     * Generates a sufficiently large word-based passage (default 350 words,
     * which exceeds what even a 150 WPM typist can type in 2 minutes: 300 words).
     */
    public PassageDto getRandomPassage(String excludeId) {
        // Generates 350 words initially — plenty for 30s, 60s, or 120s
        List<String> words = generateWords(350);
        String fullText = String.join(" ", words);
        String id = "flow-" + UUID.randomUUID().toString().substring(0, 8);
        return new PassageDto(id, fullText, "Continuous Word Flow", "TypeFlow Corpus", "Standard Words");
    }

    public List<String> getMoreWords(int count) {
        return generateWords(count > 0 ? count : 100);
    }

    public PassageDto getPassageById(String id) {
        return getRandomPassage(null);
    }

    public String getNextParagraph(String currentId) {
        List<String> words = generateWords(100);
        return " " + String.join(" ", words);
    }
}
