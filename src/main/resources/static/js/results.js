/**
 * TypeFlow Detailed Results & Performance Analysis Manager
 */
class ResultsManager {
    constructor() {
        this.currentResult = null;
        this.keyboardHeatmap = new KeyboardHeatmap('results-keyboard-heatmap');
        this.speedChart = null;
        this.bindEvents();
    }

    bindEvents() {
        document.getElementById('btn-results-retry')?.addEventListener('click', () => {
            window.location.hash = '#test';
            window.typingEngine?.restartTest();
        });

        document.getElementById('btn-results-dashboard')?.addEventListener('click', () => {
            window.location.hash = '#home';
        });

        document.getElementById('btn-results-claim')?.addEventListener('click', () => {
            if (this.currentResult && this.currentResult.id) {
                window.authManager?.openAuthModal('register', this.currentResult.id);
            }
        });
    }

    displayResult(resultData, keyStats = {}) {
        this.currentResult = resultData;
        window.location.hash = '#results';

        // 1. Overview metrics
        document.getElementById('res-net-wpm').textContent = resultData.netWpm != null ? resultData.netWpm.toFixed(1) : '0.0';
        document.getElementById('res-raw-wpm').textContent = resultData.rawWpm != null ? resultData.rawWpm.toFixed(1) : '0.0';
        document.getElementById('res-accuracy').textContent = resultData.accuracy != null ? `${resultData.accuracy.toFixed(1)}%` : '0%';
        document.getElementById('res-correct-chars').textContent = resultData.correctChars;
        document.getElementById('res-incorrect-chars').textContent = resultData.incorrectChars;
        document.getElementById('res-total-chars').textContent = resultData.totalChars;
        document.getElementById('res-backspaces').textContent = resultData.backspaces || 0;
        document.getElementById('res-duration').textContent = `${resultData.duration}s`;

        // Personal best badge
        const pbBadge = document.getElementById('res-pb-badge');
        if (pbBadge) {
            pbBadge.style.display = resultData.personalBest ? 'inline-flex' : 'none';
        }

        // Guest Save Score CTA
        const guestBanner = document.getElementById('res-guest-banner');
        if (guestBanner) {
            if (!window.authManager?.currentUser) {
                guestBanner.style.display = 'flex';
            } else {
                guestBanner.style.display = 'none';
            }
        }

        // 2. Render Virtual Keyboard Heatmap
        // If keyStats were provided from typingEngine, use them. If not, reconstruct from keyErrors
        const computedStats = Object.keys(keyStats).length > 0 ? keyStats : this.reconstructKeyStats(resultData.keyErrors);
        this.keyboardHeatmap.render(computedStats);

        // 3. Render Performance Insights
        this.renderInsights(computedStats);

        // 4. Render Speed Consistency Chart
        this.renderSpeedChart(resultData.speedHistoryJson);
    }

    displayResultFallback(submission, keyStats) {
        // Fallback for offline / network interruption
        const fallbackData = {
            id: null,
            duration: submission.duration,
            netWpm: submission.clientNetWpm,
            rawWpm: submission.clientRawWpm,
            accuracy: submission.clientAccuracy,
            correctChars: submission.correctChars,
            incorrectChars: submission.incorrectChars,
            totalChars: submission.totalChars,
            backspaces: submission.backspaces,
            personalBest: false,
            speedHistoryJson: submission.speedHistoryJson
        };
        this.displayResult(fallbackData, keyStats);
    }

    reconstructKeyStats(keyErrors) {
        const stats = {};
        if (!keyErrors || !Array.isArray(keyErrors)) return stats;
        keyErrors.forEach(ke => {
            const exp = ke.expectedKey.toLowerCase();
            if (!stats[exp]) {
                stats[exp] = { expected: ke.errorCount, correct: 0, errors: ke.errorCount, substitutions: {} };
            } else {
                stats[exp].expected += ke.errorCount;
                stats[exp].errors += ke.errorCount;
            }
            if (ke.actualKey) {
                stats[exp].substitutions[ke.actualKey.toLowerCase()] = ke.errorCount;
            }
        });
        return stats;
    }

    renderInsights(keyStats) {
        const weakList = document.getElementById('res-weak-keys-list');
        const subsList = document.getElementById('res-subs-list');
        const recommendations = document.getElementById('res-recommendations');

        const errorsArr = [];
        const subsArr = [];

        for (const [key, stat] of Object.entries(keyStats)) {
            if (stat.errors > 0) {
                errorsArr.push({ key, errors: stat.errors, expected: stat.expected });
                if (stat.substitutions) {
                    for (const [subKey, count] of Object.entries(stat.substitutions)) {
                        subsArr.push({ expected: key, actual: subKey, count });
                    }
                }
            }
        }

        errorsArr.sort((a, b) => b.errors - a.errors);
        subsArr.sort((a, b) => b.count - a.count);

        if (weakList) {
            if (errorsArr.length === 0) {
                weakList.innerHTML = '<li class="text-mint"><span class="badge badge-mint">Flawless</span> Zero key errors recorded!</li>';
            } else {
                weakList.innerHTML = errorsArr.slice(0, 5).map(item => `
                    <li>
                        <span class="key-pill key-red">${item.key.toUpperCase()}</span>
                        <span class="insight-detail">${item.errors} error${item.errors > 1 ? 's' : ''} (${item.expected} tested)</span>
                    </li>
                `).join('');
            }
        }

        if (subsList) {
            if (subsArr.length === 0) {
                subsList.innerHTML = '<li class="text-muted">No key substitutions detected.</li>';
            } else {
                subsList.innerHTML = subsArr.slice(0, 5).map(item => `
                    <li>
                        <span>Expected <strong class="text-mint">${item.expected.toUpperCase()}</strong> → Typed <strong class="text-red">${item.actual.toUpperCase()}</strong></span>
                        <span class="sub-count-tag">${item.count}×</span>
                    </li>
                `).join('');
            }
        }

        if (recommendations) {
            if (errorsArr.length === 0) {
                recommendations.textContent = 'Exceptional accuracy! Consider increasing your speed target or practicing with the 2-minute endurance mode.';
            } else {
                const weakestKey = errorsArr[0].key.toUpperCase();
                recommendations.textContent = `Focus on finger placement for '${weakestKey}'. Keep a steady rhythm and prioritize accuracy before accelerating speed.`;
            }
        }
    }

    renderSpeedChart(speedJson) {
        const canvas = document.getElementById('results-speed-chart');
        if (!canvas) return;

        let dataPoints = [];
        try {
            if (speedJson) {
                dataPoints = JSON.parse(speedJson);
            }
        } catch (e) {}

        if (dataPoints.length === 0) {
            // Generate synthetic representation if history is empty
            const totalSec = this.currentResult ? this.currentResult.duration : 60;
            const avgWpm = this.currentResult ? this.currentResult.netWpm : 60;
            for (let s = 1; s <= totalSec; s += Math.max(1, Math.floor(totalSec / 10))) {
                dataPoints.push({ sec: s, wpm: avgWpm + (Math.random() * 4 - 2) });
            }
        }

        const labels = dataPoints.map(p => `${p.sec}s`);
        const wpmData = dataPoints.map(p => Math.round(p.wpm * 10) / 10);

        if (this.speedChart) {
            this.speedChart.destroy();
        }

        if (window.Chart) {
            const ctx = canvas.getContext('2d');
            const gradient = ctx.createLinearGradient(0, 0, 0, 220);
            gradient.addColorStop(0, 'rgba(16, 185, 129, 0.45)');
            gradient.addColorStop(1, 'rgba(16, 185, 129, 0.02)');

            this.speedChart = new window.Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Net WPM',
                        data: wpmData,
                        borderColor: '#10b981',
                        backgroundColor: gradient,
                        borderWidth: 2.5,
                        fill: true,
                        tension: 0.35,
                        pointBackgroundColor: '#10b981',
                        pointBorderColor: '#090d16',
                        pointHoverRadius: 6,
                        pointRadius: 3
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: '#1e293b',
                            titleColor: '#f8fafc',
                            bodyColor: '#10b981',
                            borderColor: '#334155',
                            borderWidth: 1,
                            callbacks: {
                                label: (ctx) => `Speed: ${ctx.parsed.y} WPM`
                            }
                        }
                    },
                    scales: {
                        x: {
                            grid: { color: 'rgba(255, 255, 255, 0.05)' },
                            ticks: { color: '#94a3b8', font: { size: 11 } }
                        },
                        y: {
                            grid: { color: 'rgba(255, 255, 255, 0.05)' },
                            ticks: { color: '#94a3b8', font: { size: 11 } },
                            beginAtZero: true
                        }
                    }
                }
            });
        }
    }
}

window.resultsManager = new ResultsManager();
