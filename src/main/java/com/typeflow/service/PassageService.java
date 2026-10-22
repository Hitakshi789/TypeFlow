package com.typeflow.service;

import com.typeflow.dto.PassageDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PassageService {

    private final List<PassageDto> passages = new ArrayList<>();

    public PassageService() {
        initPassages();
    }

    private void initPassages() {
        passages.add(new PassageDto(
            "tech-1",
            "The art of programming is the art of organizing complexity, of mastering multitude and avoiding chaos as effectively as possible. Clean code reads like well written prose. Every function should do one thing and do it with elegance. The architecture of modern software reflects human thought turned into executable logic, transforming abstract ideas into concrete systems that power our interconnected world.",
            "The Art of Programming",
            "Edsger W. Dijkstra",
            "Technology"
        ));

        passages.add(new PassageDto(
            "lit-1",
            "The true measure of any typist is not merely rapid speed, but the rhythm of unbroken concentration. Words stream across the screen like music across a stave, each keystroke striking its precise note in harmony with the mind. When you enter a state of deep flow, the boundary between intention and action dissolves completely, leaving only the pure momentum of language unfolding forward.",
            "Rhythm and Mind",
            "Flow Philosophy",
            "Literature"
        ));

        passages.add(new PassageDto(
            "sci-1",
            "In quantum mechanics, particles can exist in multiple states simultaneously until an observation forces a determination. This principle reveals a universe far stranger and more dynamic than classical physics ever imagined. Light behaves as both a wave and a particle, space and time bend under the weight of cosmic gravity, and the cosmos continues to expand into the boundless unknown.",
            "Quantum Horizons",
            "Cosmic Science",
            "Science"
        ));

        passages.add(new PassageDto(
            "hist-1",
            "The invention of the mechanical typewriter in the late nineteenth century forever altered the speed of communication and human correspondence. The Christopher Sholes QWERTY layout was engineered to prevent mechanical typebars from jamming during rapid strokes. More than a century later, that same mechanical legacy guides the fingers of millions across digital keyboards worldwide.",
            "The Typist's Legacy",
            "Historical Technology",
            "History"
        ));

        passages.add(new PassageDto(
            "phil-1",
            "We are what we repeatedly do. Excellence, then, is not an act, but a habit. True mastery in any craft arises from relentless deliberate practice, paying mindful attention to every small error, and refining your technique stroke by stroke. When patience unites with sustained focus, even the most formidable obstacles yield to persistent determination and quiet resolve.",
            "Habit and Mastery",
            "Philosophical Reflections",
            "Philosophy"
        ));

        passages.add(new PassageDto(
            "nature-1",
            "Beneath the ocean surface lies a realm of silent wonder, where bioluminescent creatures drift through twilight depths like living constellations. Coral reefs pulse with vibrant life, supporting vast marine ecosystems through intricate symbiotic relationships that have flourished for millions of years in fragile equilibrium with the tides.",
            "Oceanic Depths",
            "Natural Wonders",
            "Nature"
        ));

        passages.add(new PassageDto(
            "tech-2",
            "Distributed computing networks form the resilient backbone of contemporary civilization. Data packets traverse transcontinental fiber optic cables beneath oceans and across continents in mere milliseconds. Encryption algorithms safeguard digital privacy, while decentralized consensus protocols demonstrate how distributed trust can flourish without central authority.",
            "Digital Architecture",
            "Cyber Systems",
            "Technology"
        ));

        passages.add(new PassageDto(
            "lit-2",
            "There is a unique stillness in early morning hours before the world awakens. In that quiet threshold, thought is lucid and creative energy flows without distraction. The keys respond with crisp precision, recording reflections, narratives, and visions that will soon echo in the minds of readers searching for clarity and inspiration.",
            "Morning Reflections",
            "Literary Flow",
            "Literature"
        ));
    }

    public PassageDto getRandomPassage(String excludeId) {
        if (passages.isEmpty()) {
            return new PassageDto("default", "The quick brown fox jumps over the lazy dog.", "Default", "Anonymous", "General");
        }
        if (passages.size() == 1) {
            return passages.get(0);
        }
        List<PassageDto> eligible = new ArrayList<>();
        for (PassageDto p : passages) {
            if (excludeId == null || !p.getId().equalsIgnoreCase(excludeId)) {
                eligible.add(p);
            }
        }
        int index = ThreadLocalRandom.current().nextInt(eligible.size());
        return eligible.get(index);
    }

    public List<PassageDto> getAllPassages() {
        return Collections.unmodifiableList(passages);
    }

    public PassageDto getPassageById(String id) {
        return passages.stream()
                .filter(p -> p.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(getRandomPassage(null));
    }

    /**
     * Supplies an additional sentence or paragraph to seamlessly append
     * when a fast typist approaches the end of their current passage.
     */
    public String getNextParagraph(String currentId) {
        PassageDto next = getRandomPassage(currentId);
        return " " + next.getText();
    }
}
