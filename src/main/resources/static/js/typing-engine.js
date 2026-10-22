/**
 * TypeFlow Typing Engine
 * Implements high-precision character-level typing test with passage auto-replenishment,
 * stable countdown timer, real-time WPM/accuracy, and Case A/B keystroke tracking.
 */
class TypingEngine {
    constructor() {
        this.duration = 60; // 30, 60, 120
        this.passageId = null;
        this.passageText = '';
        this.currentCharIndex = 0;
        
        // State
        this.state = 'idle'; // 'idle', 'running', 'completed'
        this.startTime = null;
        this.timerInterval = null;
        this.remainingSeconds = 60;
        this.elapsedSeconds = 0;

        // Keystroke statistics
        this.correctChars = 0;
        this.incorrectChars = 0;
        this.totalTypedChars = 0;
        this.backspaces = 0;
        
        // Key-level error tracking for Heatmap & Analytics
        // key -> { expected: N, correct: N, errors: N, substitutions: { 'wrong': count } }
        this.keyStats = {};
        this.keyErrorsList = []; // for submission to backend

        // Speed history sampling every second for progress charts
        this.speedHistory = [];
        this.lastSampleTime = 0;

        // DOM elements cache
        this.dom = {};
        this.initDom();
        this.bindEvents();
    }

    initDom() {
        this.dom.passageContainer = document.getElementById('typing-passage');
        this.dom.hiddenInput = document.getElementById('typing-hidden-input');
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

        // Typing container click focuses hidden input
        this.dom.passageContainer?.addEventListener('click', () => {
            this.focusInput();
        });

        // Prevent pasting
        this.dom.hiddenInput?.addEventListener('paste', (e) => {
            e.preventDefault();
            window.showToast?.('Pasting is disabled for fair evaluation.', 'warning');
        });

        // Keydown listener on hidden input
        this.dom.hiddenInput?.addEventListener('keydown', (e) => {
            this.handleKeyDown(e);
        });

        // Global shortcut: Tab + Enter or Escape to restart test
        window.addEventListener('keydown', (e) => {
            // If typing view is active
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
        if (this.dom.hiddenInput && this.state !== 'completed') {
            this.dom.hiddenInput.focus();
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

    async loadPassage(excludeId = null) {
        try {
            if (this.dom.passageContainer) {
                this.dom.passageContainer.innerHTML = '<div class="passage-loading"><div class="spinner"></div> Generating curated passage...</div>';
            }
            const passage = await API.getRandomPassage(excludeId);
            this.passageId = passage.id;
            this.passageText = passage.text;
            this.renderPassage();
        } catch (err) {
            console.error('Error loading passage:', err);
            // Fallback default passage
            this.passageId = 'default-fallback';
            this.passageText = 'The art of programming is the art of organizing complexity, of mastering multitude and avoiding chaos as effectively as possible. Clean code reads like well written prose. Every function should do one thing and do it with elegance.';
            this.renderPassage();
        }
    }

    renderPassage() {
        if (!this.dom.passageContainer) return;
        this.dom.passageContainer.innerHTML = '';

        const frag = document.createDocumentFragment();
        for (let i = 0; i < this.passageText.length; i++) {
            const span = document.createElement('span');
            span.className = 'char-char char-untyped';
            span.setAttribute('data-idx', i);
            const ch = this.passageText.charAt(i);
            span.textContent = ch === ' ' ? ' ' : ch;
            if (ch === ' ') {
                span.classList.add('char-space');
            }
            frag.appendChild(span);
        }
        this.dom.passageContainer.appendChild(frag);
        this.currentCharIndex = 0;
        this.updateCursorHighlight();
    }

    updateCursorHighlight() {
        const allSpans = this.dom.passageContainer.querySelectorAll('.char-char');
        allSpans.forEach(span => span.classList.remove('char-current'));

        if (this.currentCharIndex < allSpans.length) {
            const currentSpan = allSpans[this.currentCharIndex];
            currentSpan.classList.add('char-current');
            
            // Auto-scroll passage if cursor moves down
            const container = this.dom.passageContainer;
            const spanOffset = currentSpan.offsetTop;
            const containerScroll = container.scrollTop;
            const containerHeight = container.clientHeight;

            if (spanOffset > containerScroll + containerHeight - 80) {
                container.scrollTop = spanOffset - 60;
            } else if (spanOffset < containerScroll) {
                container.scrollTop = Math.max(0, spanOffset - 20);
            }
        }
    }

    async appendNextPassageText() {
        try {
            const data = await API.getNextPassage(this.passageId);
            if (data && data.text) {
                const additionalText = data.text;
                const startIndex = this.passageText.length;
                this.passageText += additionalText;

                const frag = document.createDocumentFragment();
                for (let i = 0; i < additionalText.length; i++) {
                    const span = document.createElement('span');
                    span.className = 'char-char char-untyped';
                    span.setAttribute('data-idx', startIndex + i);
                    const ch = additionalText.charAt(i);
                    span.textContent = ch === ' ' ? ' ' : ch;
                    if (ch === ' ') {
                        span.classList.add('char-space');
                    }
                    frag.appendChild(span);
                }
                this.dom.passageContainer.appendChild(frag);
            }
        } catch (e) {
            console.error('Failed to append passage text', e);
        }
    }

    startTest() {
        this.state = 'running';
        this.startTime = performance.now();
        this.speedHistory = [];
        this.lastSampleTime = 0;
        
        if (this.dom.statusBadge) {
            this.dom.statusBadge.textContent = 'Typing in Progress';
            this.dom.statusBadge.className = 'status-badge status-active';
        }

        // High precision interval using performance.now()
        this.timerInterval = setInterval(() => {
            const now = performance.now();
            const elapsed = (now - this.startTime) / 1000;
            this.elapsedSeconds = elapsed;
            const left = Math.max(0, Math.ceil(this.duration - elapsed));
            this.remainingSeconds = left;

            this.updateTimerDisplay();
            this.updateLiveStats();

            // Sample speed history every ~1 second
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

    handleKeyDown(e) {
        if (this.state === 'completed') return;

        // Ignore meta/modifier keys
        if (e.ctrlKey || e.altKey || e.metaKey) return;
        if (e.key === 'Shift' || e.key === 'CapsLock' || e.key === 'Control' || e.key === 'Alt') return;

        // Start on first valid typing stroke
        if (this.state === 'idle') {
            if (e.key.length === 1 || e.key === ' ') {
                this.startTest();
            } else {
                return;
            }
        }

        // Handle Backspace
        if (e.key === 'Backspace') {
            e.preventDefault();
            this.handleBackspace();
            return;
        }

        // Single printable character
        if (e.key.length === 1) {
            e.preventDefault();
            this.handleCharacterInput(e.key);
        }
    }

    handleBackspace() {
        if (this.currentCharIndex <= 0) return;

        this.backspaces++;
        this.currentCharIndex--;

        const span = this.dom.passageContainer.querySelector(`.char-char[data-idx="${this.currentCharIndex}"]`);
        if (span) {
            if (span.classList.contains('char-correct')) {
                this.correctChars = Math.max(0, this.correctChars - 1);
            } else if (span.classList.contains('char-error')) {
                this.incorrectChars = Math.max(0, this.incorrectChars - 1);
            }
            this.totalTypedChars = Math.max(0, this.totalTypedChars - 1);

            span.className = 'char-char char-untyped';
            if (this.passageText.charAt(this.currentCharIndex) === ' ') {
                span.classList.add('char-space');
            }
        }

        window.soundEffects?.playClick(false);
        this.updateCursorHighlight();
        this.updateLiveStats();
    }

    handleCharacterInput(typedChar) {
        if (this.currentCharIndex >= this.passageText.length) {
            return;
        }

        const expectedChar = this.passageText.charAt(this.currentCharIndex);
        const span = this.dom.passageContainer.querySelector(`.char-char[data-idx="${this.currentCharIndex}"]`);
        const isMatch = (typedChar === expectedChar);

        this.totalTypedChars++;

        // Track stats for key error heatmap
        const expLower = expectedChar.toLowerCase();
        if (!this.keyStats[expLower]) {
            this.keyStats[expLower] = { expected: 0, correct: 0, errors: 0, substitutions: {} };
        }
        this.keyStats[expLower].expected++;

        if (isMatch) {
            this.correctChars++;
            this.keyStats[expLower].correct++;
            if (span) {
                span.className = 'char-char char-correct';
            }
            window.soundEffects?.playClick(expectedChar === ' ');
        } else {
            this.incorrectChars++;
            this.keyStats[expLower].errors++;
            
            // Case A: Expected key vs actual typed key
            const typedLower = typedChar.toLowerCase();
            this.keyStats[expLower].substitutions[typedLower] = (this.keyStats[expLower].substitutions[typedLower] || 0) + 1;

            if (span) {
                span.className = 'char-char char-error';
            }
            window.soundEffects?.playError();
        }

        this.currentCharIndex++;

        // Auto replenish if near the end (15 chars away)
        if (this.currentCharIndex >= this.passageText.length - 20) {
            this.appendNextPassageText();
        }

        this.updateCursorHighlight();
        this.updateLiveStats();
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
        clearInterval(this.timerInterval);
        this.state = 'completed';
        this.elapsedSeconds = this.duration; // Standard completed duration
        this.remainingSeconds = 0;
        this.updateTimerDisplay();

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
        this.currentCharIndex = 0;
        this.startTime = null;
        this.remainingSeconds = this.duration;
        this.elapsedSeconds = 0;
        this.correctChars = 0;
        this.incorrectChars = 0;
        this.totalTypedChars = 0;
        this.backspaces = 0;
        this.keyStats = {};
        this.keyErrorsList = [];
        this.speedHistory = [];

        this.updateTimerDisplay();
        this.updateLiveStats();

        if (this.dom.statusBadge) {
            this.dom.statusBadge.textContent = 'Waiting to Start';
            this.dom.statusBadge.className = 'status-badge status-idle';
        }

        this.loadPassage(this.passageId);
        setTimeout(() => this.focusInput(), 100);
    }
}

window.typingEngine = new TypingEngine();
