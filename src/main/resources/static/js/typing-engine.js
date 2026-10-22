/**
 * TypeFlow Typing Engine — 10FastFingers Word-by-Word Model
 * Implements continuous word-based typing, spacebar evaluation, accurate character
 * scoring (crediting partial matches), seamless word prefetching (never runs out),
 * and precise keyboard heatmap diagnostics.
 */
class TypingEngine {
    constructor() {
        this.duration = 60; // 30, 60, 120
        this.passageId = null;
        this.words = [];
        this.currentWordIndex = 0;
        
        // State
        this.state = 'idle'; // 'idle', 'running', 'completed'
        this.startTime = null;
        this.timerInterval = null;
        this.remainingSeconds = 60;
        this.elapsedSeconds = 0;
        this.isSubmitting = false;

        // Statistics
        this.correctWords = 0;
        this.incorrectWords = 0;
        this.correctChars = 0;
        this.incorrectChars = 0;
        this.totalTypedChars = 0;
        this.backspaces = 0;
        
        // Key-level error tracking for Heatmap & Analytics
        // key -> { expected: N, correct: N, errors: N, substitutions: { 'wrong': count } }
        this.keyStats = {};

        // Speed history sampling every second for progress charts
        this.speedHistory = [];
        this.lastSampleTime = 0;

        // DOM elements cache
        this.dom = {};
        this.initDom();
        this.bindEvents();
    }

    initDom() {
        this.dom.wordsBox = document.getElementById('typing-passage-box');
        this.dom.wordsStream = document.getElementById('typing-words-stream');
        this.dom.wordInput = document.getElementById('typing-word-input');
        this.dom.timerDisplay = document.getElementById('typing-timer');
        this.dom.wpmDisplay = document.getElementById('typing-live-wpm');
        this.dom.accuracyDisplay = document.getElementById('typing-live-acc');
        this.dom.correctDisplay = document.getElementById('typing-live-correct');
        this.dom.incorrectDisplay = document.getElementById('typing-live-incorrect');
        this.dom.progressBar = document.getElementById('typing-progress-bar');
        this.dom.statusBadge = document.getElementById('typing-status-badge');
        this.dom.restartBtn = document.getElementById('btn-typing-restart');
        this.dom.modeBtns = document.querySelectorAll('.mode-duration-btn');
    }

    bindEvents() {
        // Duration selector buttons
        this.dom.modeBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const newDuration = parseInt(e.currentTarget.getAttribute('data-duration'), 10);
                this.setDuration(newDuration);
            });
        });

        // Restart button
        this.dom.restartBtn?.addEventListener('click', () => {
            this.restartTest();
        });

        // Words box click focuses input field
        this.dom.wordsBox?.addEventListener('click', () => {
            this.focusInput();
        });

        // Prevent pasting into typing input
        this.dom.wordInput?.addEventListener('paste', (e) => {
            e.preventDefault();
            window.showToast?.('Pasting is disabled for fair evaluation.', 'warning');
        });

        // Real-time input listener
        this.dom.wordInput?.addEventListener('input', () => {
            this.handleInput();
        });

        // Keydown listener for Space, Backspace, Enter, Esc
        this.dom.wordInput?.addEventListener('keydown', (e) => {
            this.handleKeyDown(e);
        });

        // Global shortcut: Escape to restart test
        window.addEventListener('keydown', (e) => {
            const testSection = document.getElementById('view-test');
            if (testSection && testSection.classList.contains('active')) {
                if (e.key === 'Escape') {
                    e.preventDefault();
                    this.restartTest();
                }
            }
        });
    }

    focusInput() {
        if (this.dom.wordInput && this.state !== 'completed') {
            this.dom.wordInput.focus();
        }
    }

    setDuration(seconds) {
        if (this.state === 'running') {
            if (!confirm('Test in progress. Change duration and restart?')) return;
        }
        this.duration = seconds;
        this.remainingSeconds = seconds;
        this.dom.modeBtns.forEach(btn => {
            if (parseInt(btn.getAttribute('data-duration'), 10) === seconds) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
        this.restartTest();
    }

    async loadPassage() {
        try {
            if (this.dom.wordsStream) {
                this.dom.wordsStream.innerHTML = '<div class="passage-loading"><div class="spinner"></div> Generating word sequence...</div>';
            }
            const passage = await API.getRandomPassage();
            this.passageId = passage.id;
            
            // Split into clean words
            const rawWords = passage.text.trim().split(/\s+/);
            this.words = rawWords.filter(w => w.length > 0);
            
            // Ensure at least 350 words so fast typists never run out
            if (this.words.length < 300) {
                const more = await API.getWordBatch(150);
                if (more && Array.isArray(more)) {
                    this.words = this.words.concat(more);
                }
            }

            this.renderWords();
        } catch (err) {
            console.error('Error loading word sequence:', err);
            // Fallback word bank
            this.words = [
                "the", "be", "to", "of", "and", "a", "in", "that", "have", "I", "it", "for", "not", "on", "with", "he", "as",
                "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she", "or", "an", "will",
                "my", "one", "all", "would", "there", "their", "what", "so", "up", "out", "if", "about", "who", "get", "which",
                "go", "me", "when", "make", "can", "like", "time", "no", "just", "him", "know", "take", "people", "into", "year",
                "your", "good", "some", "could", "them", "see", "other", "than", "then", "now", "look", "only", "come", "its",
                "over", "think", "also", "back", "after", "use", "two", "how", "our", "work", "first", "well", "way", "even",
                "new", "want", "because", "any", "these", "give", "day", "most", "us", "flow", "speed", "test", "every", "counts"
            ];
            this.renderWords();
        }
    }

    renderWords() {
        if (!this.dom.wordsStream) return;
        this.dom.wordsStream.innerHTML = '';

        const frag = document.createDocumentFragment();
        this.words.forEach((word, idx) => {
            const span = document.createElement('span');
            span.className = 'word';
            span.id = `word-${idx}`;
            span.setAttribute('data-idx', idx);
            span.textContent = word;
            frag.appendChild(span);
        });

        this.dom.wordsStream.appendChild(frag);
        this.currentWordIndex = 0;
        this.updateActiveWord();
    }

    async prefetchMoreWords() {
        try {
            const more = await API.getWordBatch(150);
            if (more && Array.isArray(more) && more.length > 0) {
                const startIndex = this.words.length;
                this.words = this.words.concat(more);

                const frag = document.createDocumentFragment();
                more.forEach((word, idx) => {
                    const span = document.createElement('span');
                    span.className = 'word';
                    span.id = `word-${startIndex + idx}`;
                    span.setAttribute('data-idx', startIndex + idx);
                    span.textContent = word;
                    frag.appendChild(span);
                });
                this.dom.wordsStream.appendChild(frag);
            }
        } catch (e) {
            console.error('Failed to prefetch additional words', e);
        }
    }

    updateActiveWord() {
        // Remove active class from all
        const currentActive = this.dom.wordsStream.querySelector('.word.word-active');
        if (currentActive) {
            currentActive.classList.remove('word-active');
        }

        const nextWordEl = document.getElementById(`word-${this.currentWordIndex}`);
        if (nextWordEl) {
            nextWordEl.classList.add('word-active');

            // 10FastFingers Rolling Line Scrolling
            // Check if active word has wrapped to a new line
            const firstWordEl = document.getElementById('word-0');
            if (firstWordEl && this.dom.wordsBox) {
                const firstTop = firstWordEl.offsetTop;
                const activeTop = nextWordEl.offsetTop;
                const lineDiff = activeTop - firstTop;

                // When moving past line 1, scroll container so active line is always near the top
                if (lineDiff > 35) {
                    this.dom.wordsBox.scrollTop = lineDiff - 20;
                } else {
                    this.dom.wordsBox.scrollTop = 0;
                }
            }
        }

        // Prefetch when within 40 words of the end (guarantees words never run out)
        if (this.currentWordIndex >= this.words.length - 40) {
            this.prefetchMoreWords();
        }
    }

    handleInput() {
        if (this.state === 'completed') return;

        const val = this.dom.wordInput.value;

        // Start test on first valid typed character
        if (this.state === 'idle' && val.trim().length > 0) {
            this.startTest();
        }

        // Real-time prefix validation for tactile feedback
        const currentExpected = this.words[this.currentWordIndex] || '';
        const currentTyped = val.trim();

        if (currentTyped.length > 0 && !currentExpected.startsWith(currentTyped)) {
            this.dom.wordInput.classList.add('input-mistake');
        } else {
            this.dom.wordInput.classList.remove('input-mistake');
        }
    }

    handleKeyDown(e) {
        if (this.state === 'completed') return;

        // Prevent browser page scroll on Space
        if (e.key === ' ' || e.code === 'Space') {
            e.preventDefault();

            // Prevent key-repeat from firing multiple times if held down
            if (e.repeat) return;

            const typed = this.dom.wordInput.value.trim();

            // Ignore empty submissions (pressing Space on empty input does nothing)
            if (typed === '') {
                this.dom.wordInput.value = '';
                return;
            }

            // Start test if not already running
            if (this.state === 'idle') {
                this.startTest();
            }

            // Evaluate the completed word
            this.submitWord(typed);
            return;
        }

        // Track backspace count
        if (e.key === 'Backspace') {
            this.backspaces++;
            window.soundEffects?.playClick(false);
            return;
        }

        // Prevent Enter from accidentally advancing
        if (e.key === 'Enter') {
            e.preventDefault();
        }
    }

    submitWord(typedWord) {
        if (this.currentWordIndex >= this.words.length) return;

        const expectedWord = this.words[this.currentWordIndex];
        const isMatch = (typedWord === expectedWord);
        const wordEl = document.getElementById(`word-${this.currentWordIndex}`);

        if (isMatch) {
            this.correctWords++;
            if (wordEl) {
                wordEl.classList.remove('word-active');
                wordEl.classList.add('word-correct');
            }
            window.soundEffects?.playClick(false);

            // Calculate character accuracy & key error metrics for exact match
            for (let i = 0; i < expectedWord.length; i++) {
                const ch = expectedWord[i];
                this.correctChars++;
                this.recordKeySuccess(ch);
            }
            this.totalTypedChars += expectedWord.length;
        } else {
            this.incorrectWords++;
            if (wordEl) {
                wordEl.classList.remove('word-active');
                wordEl.classList.add('word-incorrect');
            }
            window.soundEffects?.playError();

            // Calculate character-level accuracy for partially correct words
            // (e.g. expected "world", typed "worlf" -> 4 correct, 1 incorrect)
            const minLen = Math.min(typedWord.length, expectedWord.length);
            for (let i = 0; i < minLen; i++) {
                const expCh = expectedWord[i];
                const actCh = typedWord[i];
                if (expCh === actCh) {
                    this.correctChars++;
                    this.recordKeySuccess(expCh);
                } else {
                    this.incorrectChars++;
                    this.recordKeySubstitution(expCh, actCh);
                }
            }

            // Extra characters typed beyond expected word length
            if (typedWord.length > expectedWord.length) {
                for (let i = minLen; i < typedWord.length; i++) {
                    this.incorrectChars++;
                    this.recordKeyExtra(typedWord[i]);
                }
            }

            // Missed characters when typed word was shorter
            if (expectedWord.length > typedWord.length) {
                for (let i = minLen; i < expectedWord.length; i++) {
                    this.recordKeyOmission(expectedWord[i]);
                }
            }

            this.totalTypedChars += typedWord.length;
        }

        // Clear input field for the next word
        this.dom.wordInput.value = '';
        this.dom.wordInput.classList.remove('input-mistake');

        // Advance to next word
        this.currentWordIndex++;
        this.updateActiveWord();
        this.updateLiveStats();
    }

    recordKeySuccess(char) {
        const key = char.toLowerCase();
        if (!this.keyStats[key]) {
            this.keyStats[key] = { expected: 0, correct: 0, errors: 0, substitutions: {} };
        }
        this.keyStats[key].expected++;
        this.keyStats[key].correct++;
    }

    recordKeySubstitution(expectedChar, actualChar) {
        const expKey = expectedChar.toLowerCase();
        const actKey = actualChar.toLowerCase();
        if (!this.keyStats[expKey]) {
            this.keyStats[expKey] = { expected: 0, correct: 0, errors: 0, substitutions: {} };
        }
        this.keyStats[expKey].expected++;
        this.keyStats[expKey].errors++;
        this.keyStats[expKey].substitutions[actKey] = (this.keyStats[expKey].substitutions[actKey] || 0) + 1;
    }

    recordKeyExtra(extraChar) {
        const key = extraChar.toLowerCase();
        if (!this.keyStats[key]) {
            this.keyStats[key] = { expected: 0, correct: 0, errors: 0, substitutions: {} };
        }
        this.keyStats[key].errors++;
    }

    recordKeyOmission(missedChar) {
        const key = missedChar.toLowerCase();
        if (!this.keyStats[key]) {
            this.keyStats[key] = { expected: 0, correct: 0, errors: 0, substitutions: {} };
        }
        this.keyStats[key].expected++;
        this.keyStats[key].errors++;
        this.keyStats[key].substitutions['[MISSED]'] = (this.keyStats[key].substitutions['[MISSED]'] || 0) + 1;
    }

    startTest() {
        this.state = 'running';
        this.startTime = performance.now();
        this.speedHistory = [];
        this.lastSampleTime = 0;
        this.isSubmitting = false;

        if (this.dom.statusBadge) {
            this.dom.statusBadge.textContent = 'Typing in Progress';
            this.dom.statusBadge.className = 'status-badge status-active';
        }

        // High-precision elapsed time interval
        this.timerInterval = setInterval(() => {
            const now = performance.now();
            const elapsed = (now - this.startTime) / 1000;
            this.elapsedSeconds = elapsed;
            const left = Math.max(0, Math.ceil(this.duration - elapsed));
            this.remainingSeconds = left;

            this.updateTimerDisplay();
            this.updateLiveStats();

            // Sample speed history every ~1 second for consistency graphs
            if (Math.floor(elapsed) > this.lastSampleTime && elapsed <= this.duration) {
                this.lastSampleTime = Math.floor(elapsed);
                const currentNetWpm = this.calculateNetWpm(elapsed);
                this.speedHistory.push({
                    sec: this.lastSampleTime,
                    wpm: currentNetWpm,
                    acc: this.calculateAccuracy()
                });
            }

            if (elapsed >= this.duration) {
                this.endTest();
            }
        }, 100);
    }

    updateTimerDisplay() {
        if (this.dom.timerDisplay) {
            const mins = Math.floor(this.remainingSeconds / 60);
            const secs = this.remainingSeconds % 60;
            this.dom.timerDisplay.textContent = `${mins}:${secs < 10 ? '0' : ''}${secs}`;
        }
        if (this.dom.progressBar) {
            const pct = Math.min(100, (this.elapsedSeconds / this.duration) * 100);
            this.dom.progressBar.style.width = `${pct}%`;
        }
    }

    calculateRawWpm(elapsedSec = null) {
        const sec = elapsedSec || (this.elapsedSeconds > 0 ? this.elapsedSeconds : this.duration);
        if (sec <= 0 || this.totalTypedChars <= 0) return 0.0;
        const mins = sec / 60.0;
        const raw = (this.totalTypedChars / 5.0) / mins;
        return Math.round(raw * 10) / 10;
    }

    calculateNetWpm(elapsedSec = null) {
        const sec = elapsedSec || (this.elapsedSeconds > 0 ? this.elapsedSeconds : this.duration);
        if (sec <= 0 || this.correctChars <= 0) return 0.0;
        const mins = sec / 60.0;
        const net = (this.correctChars / 5.0) / mins;
        return Math.round(Math.max(0, net) * 10) / 10;
    }

    calculateAccuracy() {
        if (this.totalTypedChars <= 0) return 100.0;
        const acc = (this.correctChars / this.totalTypedChars) * 100.0;
        return Math.round(acc * 100) / 100;
    }

    updateLiveStats() {
        const netWpm = this.calculateNetWpm();
        const acc = this.calculateAccuracy();

        if (this.dom.wpmDisplay) this.dom.wpmDisplay.textContent = netWpm.toFixed(0);
        if (this.dom.accuracyDisplay) this.dom.accuracyDisplay.textContent = `${acc.toFixed(0)}%`;
        if (this.dom.correctDisplay) this.dom.correctDisplay.textContent = this.correctChars;
        if (this.dom.incorrectDisplay) this.dom.incorrectDisplay.textContent = this.incorrectChars;
    }

    async endTest() {
        if (this.isSubmitting) return;
        this.isSubmitting = true;

        clearInterval(this.timerInterval);
        this.state = 'completed';
        this.elapsedSeconds = this.duration; // Use standard completed duration
        this.remainingSeconds = 0;
        this.updateTimerDisplay();

        // If user typed characters in the input field when time expired, evaluate them
        const remainingInput = this.dom.wordInput ? this.dom.wordInput.value.trim() : '';
        if (remainingInput.length > 0) {
            this.submitWord(remainingInput);
        }

        // Freeze input
        if (this.dom.wordInput) {
            this.dom.wordInput.disabled = true;
        }

        if (this.dom.statusBadge) {
            this.dom.statusBadge.textContent = 'Test Completed';
            this.dom.statusBadge.className = 'status-badge status-completed';
        }

        window.soundEffects?.playSuccess();

        // Compile KeyErrors list for backend payload
        const keyErrorsPayload = [];
        for (const [expKey, stat] of Object.entries(this.keyStats)) {
            if (stat.substitutions) {
                for (const [actKey, count] of Object.entries(stat.substitutions)) {
                    keyErrorsPayload.push({
                        expectedKey: expKey,
                        actualKey: actKey,
                        errorType: 'CASE_A_SUBSTITUTION',
                        errorCount: count
                    });
                }
            }
        }

        const submission = {
            duration: this.duration,
            elapsedSeconds: this.elapsedSeconds,
            totalChars: this.totalTypedChars,
            correctChars: this.correctChars,
            incorrectChars: this.incorrectChars,
            backspaces: this.backspaces,
            clientRawWpm: this.calculateRawWpm(),
            clientNetWpm: this.calculateNetWpm(),
            clientAccuracy: this.calculateAccuracy(),
            speedHistoryJson: JSON.stringify(this.speedHistory),
            keyErrors: keyErrorsPayload
        };

        // Submit to backend
        try {
            window.showResultsLoading?.();
            const resultResponse = await API.submitTypingTest(submission);
            // Display results page with heatmap
            window.resultsManager?.displayResult(resultResponse, this.keyStats);
        } catch (err) {
            console.error('Failed to submit test', err);
            window.showToast?.('Error submitting result: ' + err.message, 'error');
            // Show client-side result fallback if network fails
            window.resultsManager?.displayResultFallback(submission, this.keyStats);
        }
    }

    restartTest() {
        clearInterval(this.timerInterval);
        this.state = 'idle';
        this.isSubmitting = false;
        this.currentWordIndex = 0;
        this.startTime = null;
        this.remainingSeconds = this.duration;
        this.elapsedSeconds = 0;
        this.correctWords = 0;
        this.incorrectWords = 0;
        this.correctChars = 0;
        this.incorrectChars = 0;
        this.totalTypedChars = 0;
        this.backspaces = 0;
        this.keyStats = {};
        this.speedHistory = [];

        if (this.dom.wordInput) {
            this.dom.wordInput.disabled = false;
            this.dom.wordInput.value = '';
            this.dom.wordInput.classList.remove('input-mistake');
        }

        if (this.dom.wordsBox) {
            this.dom.wordsBox.scrollTop = 0;
        }

        this.updateTimerDisplay();
        this.updateLiveStats();

        if (this.dom.statusBadge) {
            this.dom.statusBadge.textContent = 'Waiting to Start';
            this.dom.statusBadge.className = 'status-badge status-idle';
        }

        this.loadPassage();
        setTimeout(() => this.focusInput(), 100);
    }
}

window.typingEngine = new TypingEngine();
