/**
 * TypeFlow Authentication and User Session Manager
 */
class AuthManager {
    constructor() {
        this.currentUser = null;
        this.pendingGuestResultId = null;
        this.init();
    }

    async init() {
        this.bindEvents();
        await this.checkStatus();
    }

    async checkStatus() {
        try {
            const user = await API.getAuthStatus();
            this.setCurrentUser(user);
        } catch (e) {
            this.setCurrentUser(null);
        }
    }

    setCurrentUser(user) {
        this.currentUser = user;
        this.updateNavUi();
        this.updateDashboardUserStats();
        // Dispatch global auth changed event
        window.dispatchEvent(new CustomEvent('auth-changed', { detail: user }));
    }

    updateNavUi() {
        const guestNav = document.getElementById('nav-guest-actions');
        const userNav = document.getElementById('nav-user-actions');
        const userAvatar = document.getElementById('nav-user-avatar');
        const userName = document.getElementById('nav-user-name');
        const statsNavLink = document.getElementById('nav-link-stats');

        if (this.currentUser) {
            if (guestNav) guestNav.style.display = 'none';
            if (userNav) userNav.style.display = 'flex';
            if (statsNavLink) statsNavLink.style.display = 'inline-flex';
            if (userAvatar) userAvatar.textContent = this.currentUser.avatar || this.currentUser.username.charAt(0).toUpperCase();
            if (userName) userName.textContent = this.currentUser.username;
        } else {
            if (guestNav) guestNav.style.display = 'flex';
            if (userNav) userNav.style.display = 'none';
            if (statsNavLink) statsNavLink.style.display = 'none';
        }
    }

    updateDashboardUserStats() {
        const loggedInStats = document.getElementById('dashboard-user-metrics');
        const onboardingCard = document.getElementById('dashboard-onboarding');

        if (!loggedInStats || !onboardingCard) return;

        if (this.currentUser && this.currentUser.totalTestsCompleted > 0) {
            loggedInStats.style.display = 'grid';
            onboardingCard.style.display = 'none';

            document.getElementById('dash-pb-wpm').textContent = this.currentUser.personalBestNetWpm != null ? this.currentUser.personalBestNetWpm : '0.0';
            document.getElementById('dash-avg-wpm').textContent = this.currentUser.averageNetWpm != null ? this.currentUser.averageNetWpm : '0.0';
            document.getElementById('dash-best-acc').textContent = this.currentUser.bestAccuracy != null ? `${this.currentUser.bestAccuracy}%` : '0%';
            document.getElementById('dash-total-tests').textContent = this.currentUser.totalTestsCompleted;
        } else {
            loggedInStats.style.display = 'none';
            onboardingCard.style.display = 'block';
            if (this.currentUser) {
                document.getElementById('onboard-welcome-title').textContent = `Welcome, ${this.currentUser.username}!`;
                document.getElementById('onboard-welcome-sub').textContent = 'Complete your first test to unlock live analytics, keyboard heatmaps, and rolling leaderboard eligibility.';
            } else {
                document.getElementById('onboard-welcome-title').textContent = 'Ready to test your speed?';
                document.getElementById('onboard-welcome-sub').textContent = 'Take a free test right now as a guest, or create an account to save your stats and claim your rank.';
            }
        }
    }

    openAuthModal(mode = 'login', pendingResultId = null) {
        if (pendingResultId) {
            this.pendingGuestResultId = pendingResultId;
        }
        const modal = document.getElementById('auth-modal');
        if (!modal) return;
        this.switchTab(mode);
        modal.classList.add('active');
        document.body.classList.add('modal-open');
    }

    closeAuthModal() {
        const modal = document.getElementById('auth-modal');
        if (!modal) return;
        modal.classList.remove('active');
        document.body.classList.remove('modal-open');
        this.clearErrors();
    }

    switchTab(tab) {
        const loginTabBtn = document.getElementById('tab-login-btn');
        const regTabBtn = document.getElementById('tab-register-btn');
        const loginForm = document.getElementById('login-form-container');
        const regForm = document.getElementById('register-form-container');
        const claimNotice = document.getElementById('auth-guest-claim-notice');

        if (this.pendingGuestResultId && claimNotice) {
            claimNotice.style.display = 'block';
        } else if (claimNotice) {
            claimNotice.style.display = 'none';
        }

        if (tab === 'login') {
            loginTabBtn.classList.add('active');
            regTabBtn.classList.remove('active');
            loginForm.style.display = 'block';
            regForm.style.display = 'none';
        } else {
            loginTabBtn.classList.remove('active');
            regTabBtn.classList.add('active');
            loginForm.style.display = 'none';
            regForm.style.display = 'block';
        }
        this.clearErrors();
    }

    clearErrors() {
        const errs = document.querySelectorAll('.auth-error-msg');
        errs.forEach(el => { el.textContent = ''; el.style.display = 'none'; });
    }

    showError(elementId, message) {
        const el = document.getElementById(elementId);
        if (el) {
            el.textContent = message;
            el.style.display = 'block';
        }
    }

    bindEvents() {
        // Open modal triggers
        document.getElementById('btn-login-trigger')?.addEventListener('click', () => this.openAuthModal('login'));
        document.getElementById('btn-register-trigger')?.addEventListener('click', () => this.openAuthModal('register'));
        document.getElementById('auth-modal-close')?.addEventListener('click', () => this.closeAuthModal());
        document.getElementById('tab-login-btn')?.addEventListener('click', () => this.switchTab('login'));
        document.getElementById('tab-register-btn')?.addEventListener('click', () => this.switchTab('register'));

        // Close on backdrop click
        document.getElementById('auth-modal')?.addEventListener('click', (e) => {
            if (e.target.id === 'auth-modal') this.closeAuthModal();
        });

        // Login submit
        document.getElementById('login-form')?.addEventListener('submit', async (e) => {
            e.preventDefault();
            this.clearErrors();
            const identifier = document.getElementById('login-identifier').value.trim();
            const password = document.getElementById('login-password').value;
            const submitBtn = document.getElementById('btn-login-submit');

            if (!identifier || !password) {
                this.showError('login-error', 'Please enter your username/email and password.');
                return;
            }

            try {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Signing in...';
                const user = await API.login(identifier, password);
                
                // If there was a pending guest result, claim it
                if (this.pendingGuestResultId) {
                    try {
                        await API.claimGuestScore(this.pendingGuestResultId);
                        this.pendingGuestResultId = null;
                    } catch (ignore) {}
                }

                this.setCurrentUser(user);
                this.closeAuthModal();
                window.showToast?.('Welcome back, ' + user.username + '!', 'success');
            } catch (err) {
                this.showError('login-error', err.message || 'Login failed.');
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Sign In';
            }
        });

        // Register submit
        document.getElementById('register-form')?.addEventListener('submit', async (e) => {
            e.preventDefault();
            this.clearErrors();
            const username = document.getElementById('reg-username').value.trim();
            const email = document.getElementById('reg-email').value.trim();
            const password = document.getElementById('reg-password').value;
            const confirmPassword = document.getElementById('reg-confirm-password').value;
            const submitBtn = document.getElementById('btn-reg-submit');

            if (password !== confirmPassword) {
                this.showError('register-error', 'Passwords do not match.');
                return;
            }
            if (password.length < 6) {
                this.showError('register-error', 'Password must be at least 6 characters.');
                return;
            }

            try {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Creating account...';
                const user = await API.register(username, email, password, confirmPassword);

                if (this.pendingGuestResultId) {
                    try {
                        await API.claimGuestScore(this.pendingGuestResultId);
                        this.pendingGuestResultId = null;
                    } catch (ignore) {}
                }

                this.setCurrentUser(user);
                this.closeAuthModal();
                window.showToast?.('Account created successfully! Welcome to TypeFlow.', 'success');
            } catch (err) {
                this.showError('register-error', err.message || 'Registration failed.');
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Create Account';
            }
        });

        // Logout
        document.getElementById('btn-logout')?.addEventListener('click', async () => {
            try {
                await API.logout();
                this.setCurrentUser(null);
                window.showToast?.('Logged out successfully.', 'info');
                // If on stats page, redirect to home
                if (window.location.hash === '#stats') {
                    window.location.hash = '#home';
                }
            } catch (e) {
                this.setCurrentUser(null);
            }
        });

        // Password strength meter
        document.getElementById('reg-password')?.addEventListener('input', (e) => {
            this.updatePasswordStrength(e.target.value);
        });
    }

    updatePasswordStrength(val) {
        const meter = document.getElementById('pass-strength-bar');
        const text = document.getElementById('pass-strength-text');
        if (!meter || !text) return;

        let strength = 0;
        if (val.length >= 6) strength += 25;
        if (val.length >= 10) strength += 25;
        if (/[A-Z]/.test(val) && /[a-z]/.test(val)) strength += 25;
        if (/[0-9]/.test(val) || /[^A-Za-z0-9]/.test(val)) strength += 25;

        meter.style.width = `${strength}%`;
        if (strength <= 25) {
            meter.style.backgroundColor = 'var(--error-red)';
            text.textContent = 'Weak';
        } else if (strength <= 75) {
            meter.style.backgroundColor = 'var(--warning-yellow)';
            text.textContent = 'Moderate';
        } else {
            meter.style.backgroundColor = 'var(--mint)';
            text.textContent = 'Strong';
        }
    }
}

window.authManager = new AuthManager();
