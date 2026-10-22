/**
 * TypeFlow Interactive Realistic QWERTY Keyboard Heatmap
 * Visualizes per-key accuracy, Case A (Expected vs Actual substitutions), and Case B (Omissions/Extra keys).
 */
class KeyboardHeatmap {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.keyStats = {}; // key -> { expected: N, correct: N, errors: N, substitutions: { 'r': 2 } }
        this.tooltip = null;
        this.createTooltipElement();
    }

    createTooltipElement() {
        let tip = document.getElementById('keyboard-tooltip');
        if (!tip) {
            tip = document.createElement('div');
            tip.id = 'keyboard-tooltip';
            tip.className = 'keyboard-tooltip';
            tip.style.display = 'none';
            document.body.appendChild(tip);
        }
        this.tooltip = tip;
    }

    /**
     * Renders the physical QWERTY layout inside the container
     */
    render(keyStats = {}) {
        if (!this.container) return;
        this.keyStats = keyStats;

        const layout = [
            [
                { label: '`', code: 'Backquote', width: 'normal' },
                { label: '1', code: 'Digit1', width: 'normal' },
                { label: '2', code: 'Digit2', width: 'normal' },
                { label: '3', code: 'Digit3', width: 'normal' },
                { label: '4', code: 'Digit4', width: 'normal' },
                { label: '5', code: 'Digit5', width: 'normal' },
                { label: '6', code: 'Digit6', width: 'normal' },
                { label: '7', code: 'Digit7', width: 'normal' },
                { label: '8', code: 'Digit8', width: 'normal' },
                { label: '9', code: 'Digit9', width: 'normal' },
                { label: '0', code: 'Digit0', width: 'normal' },
                { label: '-', code: 'Minus', width: 'normal' },
                { label: '=', code: 'Equal', width: 'normal' },
                { label: 'Backspace', code: 'Backspace', width: 'w-backspace', display: '⌫' }
            ],
            [
                { label: 'Tab', code: 'Tab', width: 'w-tab', display: 'Tab' },
                { label: 'Q', code: 'KeyQ', char: 'q' },
                { label: 'W', code: 'KeyW', char: 'w' },
                { label: 'E', code: 'KeyE', char: 'e' },
                { label: 'R', code: 'KeyR', char: 'r' },
                { label: 'T', code: 'KeyT', char: 't' },
                { label: 'Y', code: 'KeyY', char: 'y' },
                { label: 'U', code: 'KeyU', char: 'u' },
                { label: 'I', code: 'KeyI', char: 'i' },
                { label: 'O', code: 'KeyO', char: 'o' },
                { label: 'P', code: 'KeyP', char: 'p' },
                { label: '[', code: 'BracketLeft' },
                { label: ']', code: 'BracketRight' },
                { label: '\\', code: 'Backslash' }
            ],
            [
                { label: 'Caps', code: 'CapsLock', width: 'w-caps', display: 'Caps' },
                { label: 'A', code: 'KeyA', char: 'a' },
                { label: 'S', code: 'KeyS', char: 's' },
                { label: 'D', code: 'KeyD', char: 'd' },
                { label: 'F', code: 'KeyF', char: 'f' },
                { label: 'G', code: 'KeyG', char: 'g' },
                { label: 'H', code: 'KeyH', char: 'h' },
                { label: 'J', code: 'KeyJ', char: 'j' },
                { label: 'K', code: 'KeyK', char: 'k' },
                { label: 'L', code: 'KeyL', char: 'l' },
                { label: ';', code: 'Semicolon' },
                { label: "'", code: 'Quote' },
                { label: 'Enter', code: 'Enter', width: 'w-enter', display: '↵' }
            ],
            [
                { label: 'Shift', code: 'ShiftLeft', width: 'w-shift', display: 'Shift' },
                { label: 'Z', code: 'KeyZ', char: 'z' },
                { label: 'X', code: 'KeyX', char: 'x' },
                { label: 'C', code: 'KeyC', char: 'c' },
                { label: 'V', code: 'KeyV', char: 'v' },
                { label: 'B', code: 'KeyB', char: 'b' },
                { label: 'N', code: 'KeyN', char: 'n' },
                { label: 'M', code: 'KeyM', char: 'm' },
                { label: ',', code: 'Comma' },
                { label: '.', code: 'Period' },
                { label: '/', code: 'Slash' },
                { label: 'Shift', code: 'ShiftRight', width: 'w-shift', display: 'Shift' }
            ],
            [
                { label: 'Space', code: 'Space', char: ' ', width: 'w-space', display: 'Space' }
            ]
        ];

        let html = '<div class="virtual-keyboard">';
        layout.forEach((row, rowIdx) => {
            html += `<div class="kb-row kb-row-${rowIdx}">`;
            row.forEach(key => {
                const searchChar = (key.char !== undefined ? key.char : key.label).toLowerCase();
                const stat = this.keyStats[searchChar] || null;
                
                let heatClass = 'key-untested';
                let accuracyDisplay = '';

                if (stat && stat.expected > 0) {
                    const acc = ((stat.correct / stat.expected) * 100);
                    if (acc >= 95) {
                        heatClass = 'key-mint';
                    } else if (acc >= 80) {
                        heatClass = 'key-yellow';
                    } else {
                        heatClass = 'key-red';
                    }
                    accuracyDisplay = `${Math.round(acc)}%`;
                }

                const widthClass = key.width || 'w-normal';
                const labelText = key.display || key.label;
                const encodedKey = encodeURIComponent(searchChar);

                html += `
                    <div class="kb-key ${widthClass} ${heatClass}" 
                         data-key="${encodedKey}" 
                         data-label="${labelText}">
                        <span class="key-label">${labelText}</span>
                        ${accuracyDisplay ? `<span class="key-acc-sub">${accuracyDisplay}</span>` : ''}
                    </div>
                `;
            });
            html += '</div>';
        });
        html += '</div>';

        // Add Legend
        html += `
            <div class="kb-legend">
                <div class="legend-item"><span class="legend-dot dot-mint"></span> Accurate (≥95%)</div>
                <div class="legend-item"><span class="legend-dot dot-yellow"></span> Occasional Errors (80–94%)</div>
                <div class="legend-item"><span class="legend-dot dot-red"></span> Frequent Errors (<80%)</div>
                <div class="legend-item"><span class="legend-dot dot-gray"></span> Untested / Neutral</div>
            </div>
        `;

        this.container.innerHTML = html;
        this.attachEvents();
    }

    attachEvents() {
        const keys = this.container.querySelectorAll('.kb-key');
        keys.forEach(el => {
            el.addEventListener('mouseenter', (e) => this.showTooltip(e, el));
            el.addEventListener('mousemove', (e) => this.positionTooltip(e));
            el.addEventListener('mouseleave', () => this.hideTooltip());
            el.addEventListener('click', (e) => {
                this.showTooltip(e, el);
            });
        });
    }

    showTooltip(event, element) {
        if (!this.tooltip) return;
        const char = decodeURIComponent(element.getAttribute('data-key'));
        const label = element.getAttribute('data-label');
        const stat = this.keyStats[char];

        let content = '';
        if (!stat || stat.expected === 0) {
            content = `
                <div class="tip-header"><strong>Key: [${label.toUpperCase()}]</strong></div>
                <div class="tip-body tip-muted">Not tested in this passage or insufficient keystroke samples.</div>
            `;
        } else {
            const acc = Math.round((stat.correct / stat.expected) * 100);
            let topSub = 'None';
            if (stat.substitutions) {
                let maxSubCount = 0;
                let topSubChar = '';
                for (const [subChar, count] of Object.entries(stat.substitutions)) {
                    if (count > maxSubCount) {
                        maxSubCount = count;
                        topSubChar = subChar;
                    }
                }
                if (topSubChar) {
                    topSub = `'${topSubChar.toUpperCase()}' (${maxSubCount} time${maxSubCount > 1 ? 's' : ''})`;
                }
            }

            content = `
                <div class="tip-header">
                    <strong>Key: [${label.toUpperCase()}]</strong>
                    <span class="tip-badge ${acc >= 95 ? 'badge-mint' : (acc >= 80 ? 'badge-yellow' : 'badge-red')}">${acc}% Acc</span>
                </div>
                <div class="tip-grid">
                    <div><span>Times Expected:</span> <strong>${stat.expected}</strong></div>
                    <div><span>Correct Presses:</span> <strong class="text-mint">${stat.correct}</strong></div>
                    <div><span>Incorrect Presses:</span> <strong class="text-red">${stat.errors}</strong></div>
                    <div class="tip-col-span"><span>Common Wrong Key:</span> <strong>${topSub}</strong></div>
                </div>
                <div class="tip-case-note">
                    <small>Case A: Checked expected vs entered keystroke.</small>
                </div>
            `;
        }

        this.tooltip.innerHTML = content;
        this.tooltip.style.display = 'block';
        this.positionTooltip(event);
    }

    positionTooltip(event) {
        if (!this.tooltip) return;
        const offset = 14;
        let left = event.pageX + offset;
        let top = event.pageY + offset;

        // Prevent overflow beyond viewport
        const tipRect = this.tooltip.getBoundingClientRect();
        if (left + tipRect.width > window.innerWidth - 10) {
            left = event.pageX - tipRect.width - offset;
        }
        if (top + tipRect.height > window.innerHeight + window.scrollY - 10) {
            top = event.pageY - tipRect.height - offset;
        }

        this.tooltip.style.left = `${left}px`;
        this.tooltip.style.top = `${top}px`;
    }

    hideTooltip() {
        if (this.tooltip) {
            this.tooltip.style.display = 'none';
        }
    }
}

window.KeyboardHeatmap = KeyboardHeatmap;
