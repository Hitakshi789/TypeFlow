/**
 * TypeFlow Rolling 24-Hour Leaderboard Manager
 */
class LeaderboardManager {
    constructor() {
        this.currentDuration = null; // null = all, 30, 60, 120
        this.entries = [];
        this.bindEvents();
    }

    bindEvents() {
        const filterBtns = document.querySelectorAll('.lb-filter-btn');
        filterBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                filterBtns.forEach(b => b.classList.remove('active'));
                e.currentTarget.classList.add('active');
                const val = e.currentTarget.getAttribute('data-duration');
                this.currentDuration = val ? parseInt(val, 10) : null;
                this.loadLeaderboard();
            });
        });

        // Refresh button
        document.getElementById('btn-lb-refresh')?.addEventListener('click', () => {
            this.loadLeaderboard();
        });
    }

    async loadLeaderboard() {
        const tbody = document.getElementById('leaderboard-tbody');
        const userRankNotice = document.getElementById('lb-user-rank-notice');
        const recentList = document.getElementById('lb-recent-activity-list');

        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="7" class="td-loading"><div class="spinner"></div> Loading 24-hour rankings...</td></tr>';
        }

        try {
            const data = await API.getLeaderboard(this.currentDuration);
            this.entries = data;
            this.renderTable(tbody, data);

            // User rank encouragement
            if (userRankNotice) {
                const myEntry = data.find(e => e.currentUser);
                if (myEntry) {
                    userRankNotice.innerHTML = `
                        <div class="user-rank-banner">
                            <span class="banner-icon">🏆</span>
                            <div>
                                <strong>Your current 24-Hour Rank: #${myEntry.rank}</strong>
                                <p>Scored <strong>${myEntry.netWpm} WPM</strong> (${myEntry.accuracy}% accuracy) in ${myEntry.duration}s mode. ${myEntry.timeRemainingFormatted}.</p>
                            </div>
                            <button class="btn btn-sm btn-outline" onclick="window.location.hash='#test'">Defend Rank</button>
                        </div>
                    `;
                    userRankNotice.style.display = 'block';
                } else if (window.authManager?.currentUser) {
                    userRankNotice.innerHTML = `
                        <div class="user-rank-banner">
                            <span class="banner-icon">⚡</span>
                            <div>
                                <strong>You have no active score in the rolling 24-hour window!</strong>
                                <p>Take a 30s, 60s, or 120s test now to secure your spot on today's leaderboard.</p>
                            </div>
                            <button class="btn btn-sm btn-mint" onclick="window.location.hash='#test'">Start Test</button>
                        </div>
                    `;
                    userRankNotice.style.display = 'block';
                } else {
                    userRankNotice.style.display = 'none';
                }
            }

            // Load recent activity feed
            this.loadRecentActivity(recentList);
        } catch (err) {
            console.error('Failed to load leaderboard', err);
            if (tbody) {
                tbody.innerHTML = `<tr><td colspan="7" class="td-error">Failed to load leaderboard. ${err.message}</td></tr>`;
            }
        }
    }

    renderTable(tbody, data) {
        if (!tbody) return;
        if (!data || data.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="td-empty">
                        <div class="empty-state">
                            <span class="empty-icon">⏱️</span>
                            <h4>No scores yet. Be the first to set a record!</h4>
                            <p>Complete a 30s, 60s, or 120s test to establish today's high score!</p>
                            <button class="btn btn-sm btn-mint" onclick="window.location.hash='#test'">Take the Test</button>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        let html = '';
        data.forEach(entry => {
            const rowClass = entry.currentUser ? 'lb-row lb-row-current' : 'lb-row';
            let rankBadge = `<span class="rank-number">${entry.rank}</span>`;
            if (entry.rank === 1) rankBadge = '<span class="rank-badge rank-gold">🥇 1</span>';
            else if (entry.rank === 2) rankBadge = '<span class="rank-badge rank-silver">🥈 2</span>';
            else if (entry.rank === 3) rankBadge = '<span class="rank-badge rank-bronze">🥉 3</span>';

            const avatarInitial = entry.avatar || entry.username.charAt(0).toUpperCase();

            html += `
                <tr class="${rowClass}">
                    <td class="td-rank">${rankBadge}</td>
                    <td class="td-user">
                        <div class="user-cell">
                            <div class="user-avatar-sm">${avatarInitial}</div>
                            <div class="user-meta">
                                <span class="user-name">${escapeHtml(entry.username)}</span>
                                ${entry.currentUser ? '<span class="badge badge-you">You</span>' : ''}
                            </div>
                        </div>
                    </td>
                    <td class="td-net-wpm"><strong>${entry.netWpm.toFixed(1)}</strong></td>
                    <td class="td-raw-wpm">${entry.rawWpm.toFixed(1)}</td>
                    <td class="td-acc">${entry.accuracy.toFixed(1)}%</td>
                    <td class="td-mode"><span class="mode-pill">${entry.duration}s</span></td>
                    <td class="td-expires"><span class="expires-pill" title="Qualifies until ${entry.completionTimestamp}">${entry.timeRemainingFormatted}</span></td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    async loadRecentActivity(container) {
        if (!container) return;
        try {
            const recents = await API.getRecentActivity();
            if (!recents || recents.length === 0) {
                container.innerHTML = '<li class="text-muted">No recent activity recorded yet today.</li>';
                return;
            }

            container.innerHTML = recents.map(r => `
                <li class="recent-item">
                    <div class="recent-left">
                        <span class="recent-avatar">${r.avatar || r.username.charAt(0).toUpperCase()}</span>
                        <span class="recent-name"><strong>${escapeHtml(r.username)}</strong> scored <strong class="text-mint">${r.netWpm} WPM</strong> in ${r.duration}s</span>
                    </div>
                    <span class="recent-time text-muted">${r.timeRemainingFormatted}</span>
                </li>
            `).join('');
        } catch (e) {
            console.error('Failed to load recent activity', e);
        }
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, function(m) {
        return {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        }[m];
    });
}

window.leaderboardManager = new LeaderboardManager();
