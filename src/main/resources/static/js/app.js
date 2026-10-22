/**
 * TypeFlow Main Application Controller
 * Handles SPA navigation, theme switching, sound toggle, and global notifications.
 */
class TypeFlowApp {
    constructor() {
        this.currentView = 'home';
        this.theme = localStorage.getItem('typeflow_theme') || 'dark';
        this.init();
    }

    init() {
        this.applyTheme(this.theme);
        this.bindGlobalEvents();
        this.handleRoute();
        window.addEventListener('hashchange', () => this.handleRoute());
    }

    applyTheme(theme) {
        this.theme = theme;
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('typeflow_theme', theme);
        
        const themeBtn = document.getElementById('btn-theme-toggle');
        if (themeBtn) {
            themeBtn.textContent = theme === 'dark' ? '☀️' : '🌙';
            themeBtn.title = theme === 'dark' ? 'Switch to Light Theme' : 'Switch to Dark Theme';
        }
    }

    toggleTheme() {
        const nextTheme = this.theme === 'dark' ? 'light' : 'dark';
        this.applyTheme(nextTheme);
    }

    bindGlobalEvents() {
        // Theme toggle
        document.getElementById('btn-theme-toggle')?.addEventListener('click', () => {
            this.toggleTheme();
        });

        // Sound toggle
        const soundBtn = document.getElementById('btn-sound-toggle');
        soundBtn?.addEventListener('click', () => {
            const enabled = window.soundEffects?.toggle();
            soundBtn.textContent = enabled ? '🔊' : '🔇';
            soundBtn.title = enabled ? 'Mute Mechanical Sounds' : 'Unmute Mechanical Sounds';
            this.showToast(enabled ? 'Mechanical keyboard audio enabled' : 'Audio muted', 'info');
        });

        // Hero CTA button
        document.getElementById('btn-hero-start')?.addEventListener('click', () => {
            window.location.hash = '#test';
        });

        // Test mode cards on home dashboard
        document.querySelectorAll('.hero-mode-card').forEach(card => {
            card.addEventListener('click', (e) => {
                const duration = parseInt(card.getAttribute('data-duration'), 10);
                if (window.typingEngine) {
                    window.typingEngine.setDuration(duration);
                }
                window.location.hash = '#test';
            });
        });

        // About modal
        document.getElementById('nav-link-about')?.addEventListener('click', (e) => {
            e.preventDefault();
            this.openAboutModal();
        });
        document.getElementById('about-modal-close')?.addEventListener('click', () => {
            this.closeAboutModal();
        });
        document.getElementById('about-modal')?.addEventListener('click', (e) => {
            if (e.target.id === 'about-modal') this.closeAboutModal();
        });
    }

    openAboutModal() {
        const modal = document.getElementById('about-modal');
        if (modal) {
            modal.classList.add('active');
            document.body.classList.add('modal-open');
        }
    }

    closeAboutModal() {
        const modal = document.getElementById('about-modal');
        if (modal) {
            modal.classList.remove('active');
            document.body.classList.remove('modal-open');
        }
    }

    handleRoute() {
        const hash = window.location.hash.replace('#', '') || 'home';
        this.showView(hash);
    }

    showView(viewName) {
        const views = document.querySelectorAll('.app-view');
        views.forEach(v => v.classList.remove('active'));

        const target = document.getElementById(`view-${viewName}`) || document.getElementById('view-home');
        if (target) {
            target.classList.add('active');
            this.currentView = viewName;
        }

        // Update active nav links
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            if (link.getAttribute('href') === `#${viewName}`) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });

        // Route-specific triggers
        if (viewName === 'test') {
            window.typingEngine?.focusInput();
            if (window.typingEngine?.state === 'idle') {
                window.typingEngine.loadPassage();
            }
        } else if (viewName === 'leaderboard') {
            window.leaderboardManager?.loadLeaderboard();
        } else if (viewName === 'stats') {
            window.statsManager?.loadStats();
        } else if (viewName === 'home') {
            window.authManager?.updateDashboardUserStats();
        }

        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let icon = 'ℹ️';
        if (type === 'success') icon = '✅';
        else if (type === 'error') icon = '⚠️';
        else if (type === 'warning') icon = '⚡';

        toast.innerHTML = `
            <span class="toast-icon">${icon}</span>
            <span class="toast-msg">${message}</span>
        `;

        container.appendChild(toast);

        // Animate entrance
        setTimeout(() => toast.classList.add('toast-show'), 10);

        // Remove after 3.5s
        setTimeout(() => {
            toast.classList.remove('toast-show');
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    }
}

window.typeFlowApp = new TypeFlowApp();
window.showToast = (msg, type) => window.typeFlowApp.showToast(msg, type);
