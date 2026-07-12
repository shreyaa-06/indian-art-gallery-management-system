document.addEventListener('DOMContentLoaded', async () => {
  const user = await Auth.checkSession();
  if (user) {
    window.location.href = '/pages/dashboard.html';
    return;
  }

  const form = document.getElementById('login-form');
  const errorEl = document.getElementById('login-error');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    Utils.clearFormErrors(form);
    errorEl.classList.remove('visible');

    const username = form.username.value.trim();
    const password = form.password.value;

    if (!username) {
      Utils.showFieldError(form.username, 'Username is required');
      return;
    }
    if (!password) {
      Utils.showFieldError(form.password, 'Password is required');
      return;
    }

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Signing in...';

    try {
      await Auth.login(username, password);
      window.location.href = '/pages/dashboard.html';
    } catch (err) {
      errorEl.textContent = err.message || 'Login failed';
      errorEl.classList.add('visible');
    } finally {
      btn.disabled = false;
      btn.textContent = 'Sign In';
    }
  });
});
