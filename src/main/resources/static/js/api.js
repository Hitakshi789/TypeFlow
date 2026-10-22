/**
 * TypeFlow REST API Client
 */
const API = {
    async request(url, options = {}) {
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...options.headers
            },
            credentials: 'same-origin'
        };

        try {
            const res = await fetch(url, config);
            const data = await res.json();
            if (!res.ok || (data.success === false)) {
                const message = (data && data.message) ? data.message : `HTTP ${res.status}`;
                throw new Error(message);
            }
            return data.data !== undefined ? data.data : data;
        } catch (err) {
            console.error(`API Error on ${url}:`, err);
            throw err;
        }
    },

    // Auth
    async getAuthStatus() {
        return this.request('/api/auth/status');
    },

    async register(username, email, password, confirmPassword) {
        return this.request('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify({ username, email, password, confirmPassword })
        });
    },

    async login(usernameOrEmail, password) {
        return this.request('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ usernameOrEmail, password })
        });
    },

    async logout() {
        return this.request('/api/auth/logout', { method: 'POST' });
    },

    async claimGuestScore(resultId) {
        return this.request('/api/auth/claim-guest', {
            method: 'POST',
            body: JSON.stringify({ resultId })
        });
    },

    // Test Engine
    async getRandomPassage(excludeId = null) {
        const url = excludeId ? `/api/test/passage?excludeId=${encodeURIComponent(excludeId)}` : '/api/test/passage';
        return this.request(url);
    },

    async getNextPassage(currentId = null) {
        const url = currentId ? `/api/test/passage/next?currentId=${encodeURIComponent(currentId)}` : '/api/test/passage/next';
        return this.request(url);
    },

    async submitTypingTest(submission) {
        return this.request('/api/test/submit', {
            method: 'POST',
            body: JSON.stringify(submission)
        });
    },

    async getResult(resultId) {
        return this.request(`/api/test/result/${resultId}`);
    },

    // Leaderboard
    async getLeaderboard(duration = null) {
        const url = duration ? `/api/leaderboard?duration=${duration}` : '/api/leaderboard';
        return this.request(url);
    },

    async getRecentActivity() {
        return this.request('/api/leaderboard/recent');
    },

    // Personal Stats
    async getPersonalStats() {
        return this.request('/api/stats/personal');
    },

    async deleteResult(resultId) {
        return this.request(`/api/stats/result/${resultId}`, { method: 'DELETE' });
    },

    async clearAllResults() {
        return this.request('/api/stats/all', { method: 'DELETE' });
    },

    async deleteAccount() {
        return this.request('/api/stats/account', { method: 'DELETE' });
    }
};

window.API = API;
