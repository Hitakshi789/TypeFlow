/**
 * TypeFlow Web Audio Synthesizer for Mechanical Keyboard Effects
 * High fidelity, zero external asset dependencies, instant response.
 */
class SoundEffects {
    constructor() {
        this.ctx = null;
        this.enabled = true;
        this.volume = 0.25;
        this.initOnFirstInteraction();
    }

    initOnFirstInteraction() {
        const resumeAudio = () => {
            if (!this.ctx) {
                const AudioCtx = window.AudioContext || window.webkitAudioContext;
                if (AudioCtx) {
                    this.ctx = new AudioCtx();
                }
            }
            if (this.ctx && this.ctx.state === 'suspended') {
                this.ctx.resume();
            }
            window.removeEventListener('keydown', resumeAudio);
            window.removeEventListener('click', resumeAudio);
        };
        window.addEventListener('keydown', resumeAudio, { once: true });
        window.addEventListener('click', resumeAudio, { once: true });
    }

    toggle() {
        this.enabled = !this.enabled;
        return this.enabled;
    }

    playClick(isSpace = false) {
        if (!this.enabled || !this.ctx) return;
        try {
            const now = this.ctx.currentTime;
            
            // Primary high-frequency click (snap)
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            osc.type = isSpace ? 'triangle' : 'sine';
            const baseFreq = isSpace ? 180 + Math.random() * 20 : 650 + Math.random() * 80;
            osc.frequency.setValueAtTime(baseFreq, now);
            osc.frequency.exponentialRampToValueAtTime(80, now + 0.04);

            gain.gain.setValueAtTime(this.volume * (isSpace ? 1.2 : 0.8), now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + (isSpace ? 0.06 : 0.045));

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start(now);
            osc.stop(now + (isSpace ? 0.06 : 0.045));

            // Transient burst
            const bufferSize = this.ctx.sampleRate * 0.01;
            const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
            const data = buffer.getChannelData(0);
            for (let i = 0; i < bufferSize; i++) {
                data[i] = (Math.random() * 2 - 1) * 0.3;
            }

            const noise = this.ctx.createBufferSource();
            noise.buffer = buffer;
            const noiseFilter = this.ctx.createBiquadFilter();
            noiseFilter.type = 'highpass';
            noiseFilter.frequency.value = isSpace ? 600 : 1800;

            const noiseGain = this.ctx.createGain();
            noiseGain.gain.setValueAtTime(this.volume * 0.5, now);
            noiseGain.gain.exponentialRampToValueAtTime(0.001, now + 0.015);

            noise.connect(noiseFilter);
            noiseFilter.connect(noiseGain);
            noiseGain.connect(this.ctx.destination);

            noise.start(now);
            noise.stop(now + 0.015);
        } catch (e) {
            // Audio context safely ignored if blocked by browser policy
        }
    }

    playError() {
        if (!this.enabled || !this.ctx) return;
        try {
            const now = this.ctx.currentTime;
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            osc.type = 'sawtooth';
            osc.frequency.setValueAtTime(140, now);
            osc.frequency.linearRampToValueAtTime(110, now + 0.08);

            gain.gain.setValueAtTime(this.volume * 0.7, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.08);

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start(now);
            osc.stop(now + 0.08);
        } catch (e) {}
    }

    playSuccess() {
        if (!this.enabled || !this.ctx) return;
        try {
            const now = this.ctx.currentTime;
            const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6 arpeggio
            notes.forEach((freq, idx) => {
                const noteTime = now + idx * 0.07;
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();

                osc.type = 'sine';
                osc.frequency.setValueAtTime(freq, noteTime);

                gain.gain.setValueAtTime(this.volume * 0.5, noteTime);
                gain.gain.exponentialRampToValueAtTime(0.001, noteTime + 0.18);

                osc.connect(gain);
                gain.connect(this.ctx.destination);

                osc.start(noteTime);
                osc.stop(noteTime + 0.18);
            });
        } catch (e) {}
    }
}

window.soundEffects = new SoundEffects();
