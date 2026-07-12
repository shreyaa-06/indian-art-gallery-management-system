const Auth = {
  async checkSession() {
    try {
      return await API.get('/api/auth/me');
    } catch {
      return null;
    }
  },

  async login(username, password) {
    const data = await API.post('/api/auth/login', { username, password });
    return data.user;
  },

  async logout() {
    await API.post('/api/auth/logout');
    window.location.href = '/pages/login.html';
  },

  async requireAuth() {
    const user = await this.checkSession();
    if (!user) {
      window.location.href = '/pages/login.html';
      return null;
    }
    return user;
  }
};
