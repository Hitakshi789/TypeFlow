/**
 * TypeFlow Personal Statistics & Analytics Manager
 */
class StatsManager {
    constructor() {
        this.statsData = null;
        this.wpmChart = null;
        this.accChart = null;
        this.historyKeyboardHeatmap = new KeyboardHeatmap('stats-cumulative-heatmap');
        this.modalKeyboardHeatmap = new KeyboardHeatmap('modal-past-heatmap');
        this.bindEvents();
    }

    bindEvents() {
        document.getElementById('btn-stats-clear-history')?.addEventListener('click', async () => {
            if (confirm('Are you sure you want to clear all your past typing test results? This cannot be undone.')) {
                try {
                    await API.clearAllResults();
                    window.showToast?.('All test history cleared.', 'success');
                    this.loadStats();
                } catch (e) {
                    window.showToast?.('Failed to clear history: ' + e.message, 'error');
                }
            }
        });

        document.getElementById('btn-stats-delete-account')?.addEventListener('click', async () => {
            if (confirm('CAUTION: Are you sure you want to permanently delete your TypeFlow account and all associated test data?')) {
                try {
                    await API.deleteAccount();
                    window.authManager?.setCurrentUser(null);
                    window.location.hash = '#home';
                    window.showToast?.('Account deleted.', 'info');
                } catch (e) {
                    window.showToast?.('Failed to delete account: ' + e.message, 'error');
                }
            }
        });

        document.getElementById('past-modal-close')?.addEventListener('click', () => {
            this.closePastModal();
        });

        document.getElementById('past-result-modal')?.addEventListener('click', (e) => {
            if (e.target.id === 'past-result-modal') this.closePastModal();
        });
    }

    async loadStats() {
        const statsView = document.getElementById('view-stats');
        const unauthNotice = document.getElementById('stats-unauthenticated');
        const statsContent = document.getElementById('stats-content');

        if (!window.authManager?.currentUser) {
            if (unauthNotice) unauthNotice.style.display = 'block';
            if (statsContent) statsContent.style.display = 'none';
            return;
        }

        if (unauthNotice) unauthNotice.style.display = 'none';
        if (statsContent) statsContent.style.display = 'block';

        try {
            const data = await API.getPersonalStats();
            this.statsData = data;
            this.renderOverview(data);
            this.renderCharts(data);
            this.renderHistoryTable(data.recentResults);
            this.renderCumulativeHeatmap(data);
        } catch (err) {
            console.error('Failed to load stats', err);
            window.showToast?.('Failed to load personal stats: ' + err.message, 'error');
        }
    }

    renderOverview(data) {
        document.getElementById('stat-pb-wpm').textContent = data.personalBestNetWpm != null ? data.personalBestNetWpm.toFixed(1) : '0.0';
        document.getElementById('stat-avg-wpm').textContent = data.averageNetWpm != null ? data.averageNetWpm.toFixed(1) : '0.0';
        document.getElementById('stat-best-acc').textContent = data.bestAccuracy != null ? `${data.bestAccuracy.toFixed(1)}%` : '0%';
        document.getElementById('stat-total-tests').textContent = data.totalTestsCompleted;

        // Duration cards (30s, 60s, 120s)
        const d30 = data.durationStats?.['30'] || {};
        const d60 = data.durationStats?.['60'] || {};
        const d120 = data.durationStats?.['120'] || {};

        document.getElementById('stat-d30-pb').textContent = d30.personalBestNetWpm != null ? `${d30.personalBestNetWpm} WPM` : '-';
        document.getElementById('stat-d30-count').textContent = d30.testCount != null ? `${d30.testCount} tests` : '0 tests';

        document.getElementById('stat-d60-pb').textContent = d60.personalBestNetWpm != null ? `${d60.personalBestNetWpm} WPM` : '-';
        document.getElementById('stat-d60-count').textContent = d60.testCount != null ? `${d60.testCount} tests` : '0 tests';

        document.getElementById('stat-d120-pb').textContent = d120.personalBestNetWpm != null ? `${d120.personalBestNetWpm} WPM` : '-';
        document.getElementById('stat-d120-count').textContent = d120.testCount != null ? `${d120.testCount} tests` : '0 tests';
    }

    renderCharts(data) {
        const history = data.progressHistory || [];
        const labels = history.map((_, i) => `#${i + 1}`);
        const wpmPoints = history.map(h => h.netWpm);
        const accPoints = history.map(h => h.accuracy);

        // 1. WPM Progress Chart
        const wpmCanvas = document.getElementById('stats-wpm-chart');
        if (wpmCanvas && window.Chart) {
            if (this.wpmChart) this.wpmChart.destroy();
            const ctx = wpmCanvas.getContext('2d');
            const grad = ctx.createLinearGradient(0, 0, 0, 200);
            grad.addColorStop(0, 'rgba(16, 185, 129, 0.4)');
            grad.addColorStop(1, 'rgba(16, 185, 129, 0.01)');

            this.wpmChart = new window.Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Net WPM',
                        data: wpmPoints,
                        borderColor: '#10b981',
                        backgroundColor: grad,
                        borderWidth: 2.5,
                        fill: true,
                        tension: 0.3,
                        pointBackgroundColor: '#10b981',
                        pointRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { color: '#94a3b8' } },
                        y: { grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { color: '#94a3b8' }, beginAtZero: false }
                    }
                }
            });
        }

        // 2. Accuracy Trend Chart
        const accCanvas = document.getElementById('stats-acc-chart');
        if (accCanvas && window.Chart) {
            if (this.accChart) this.accChart.destroy();
            const ctx = accCanvas.getContext('2d');
            const grad = ctx.createLinearGradient(0, 0, 0, 200);
            grad.addColorStop(0, 'rgba(59, 130, 246, 0.4)');
            grad.addColorStop(1, 'rgba(59, 130, 246, 0.01)');

            this.accChart = new window.Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Accuracy (%)',
                        data: accPoints,
                        borderColor: '#3b82f6',
                        backgroundColor: grad,
                        borderWidth: 2.5,
                        fill: true,
                        tension: 0.3,
                        pointBackgroundColor: '#3b82f6',
                        pointRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { color: '#94a3b8' } },
                        y: { grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { color: '#94a3b8' }, max: 100 }
                    }
                }
            });
        }
    }

    renderCumulativeHeatmap(data) {
        // Convert top mistyped keys and substitutions to KeyStats format for the heatmap
        const cumulativeMap = {};
        if (data.keyAccuracyMap) {
            for (const [k, v] of Object.entries(data.keyAccuracyMap)) {
                cumulativeMap[k.toLowerCase()] = {
                    expected: Math.max(v.errorCount * 3, 10), // proportional baseline
                    correct: Math.max(0, Math.max(v.errorCount * 3, 10) - v.errorCount),
                    errors: v.errorCount,
                    substitutions: v.mostCommonWrongKey ? { [v.mostCommonWrongKey]: v.errorCount } : {}
                };
            }
        }
        this.historyKeyboardHeatmap.render(cumulativeMap);
    }

    renderHistoryTable(results) {
        const tbody = document.getElementById('stats-history-tbody');
        if (!tbody) return;

        if (!results || results.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="td-empty">No tests completed yet. Take your first test now!</td></tr>';
            return;
        }

        tbody.innerHTML = results.map(r => {
            const dateStr = new Date(r.completionTimestamp).toLocaleString();
            return `
                <tr>
                    <td><strong>${r.netWpm.toFixed(1)}</strong> WPM</td>
                    <td>${r.rawWpm.toFixed(1)}</td>
                    <td>${r.accuracy.toFixed(1)}%</td>
                    <td><span class="mode-pill">${r.duration}s</span></td>
                    <td>${r.correctChars}/${r.incorrectChars}</td>
                    <td class="text-muted"><small>${dateStr}</small></td>
                    <td>
                        <button class="btn btn-xs btn-outline" onclick="window.statsManager.openPastResult(${r.id})">Details</button>
                        <button class="btn btn-xs btn-danger-icon" onclick="window.statsManager.deleteSingleResult(${r.id})" title="Delete">🗑️</button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    async deleteSingleResult(id) {
        if (!confirm('Delete this typing test attempt?')) return;
        try {
            await API.deleteResult(id);
            window.showToast?.('Result deleted', 'info');
            this.loadStats();
        } catch (e) {
            window.showToast?.('Failed to delete: ' + e.message, 'error');
        }
    }

    async openPastResult(resultId) {
        try {
            const res = await API.getResult(resultId);
            const modal = document.getElementById('past-result-modal');
            if (!modal) return;

            document.getElementById('past-res-wpm').textContent = `${res.netWpm} Net WPM`;
            document.getElementById('past-res-acc').textContent = `${res.accuracy}% Accuracy`;
            document.getElementById('past-res-meta').textContent = `${res.duration}s test • ${new Date(res.completionTimestamp).toLocaleString()}`;
            document.getElementById('past-res-chars').textContent = `Correct: ${res.correctChars} | Errors: ${res.incorrectChars} | Backspaces: ${res.backspaces || 0}`;

            // Reconstruct heatmap for this single past test
            const keyStats = {};
            if (res.keyErrors) {
                res.keyErrors.forEach(ke => {
                    const exp = ke.expectedKey.toLowerCase();
                    if (!keyStats[exp]) {
                        keyStats[exp] = { expected: ke.errorCount * 2, correct: ke.errorCount, errors: ke.errorCount, substitutions: {} };
                    }
                    if (ke.actualKey) {
                        keyStats[exp].substitutions[ke.actualKey.toLowerCase()] = ke.errorCount;
                    }
                });
            }
            this.modalKeyboardHeatmap.render(keyStats);

            modal.classList.add('active');
            document.body.classList.add('modal-open');
        } catch (e) {
            window.showToast?.('Failed to load past test details', 'error');
        }
    }

    closePastModal() {
        const modal = document.getElementById('past-result-modal');
        if (modal) {
            modal.classList.remove('active');
            document.body.classList.remove('modal-open');
        }
    }
}

window.statsManager = new StatsManager();
