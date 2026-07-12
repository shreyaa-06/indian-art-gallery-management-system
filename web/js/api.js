const API = {
  async request(url, options = {}) {
    const defaults = {
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' }
    };
    const config = { ...defaults, ...options };
    if (config.body && typeof config.body === 'object') {
      config.body = JSON.stringify(config.body);
    }

    const response = await fetch(url, config);
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      const error = new Error(data.error || 'Request failed');
      error.status = response.status;
      throw error;
    }
    return data;
  },

  get(url) { return this.request(url); },

  post(url, body) { return this.request(url, { method: 'POST', body }); },

  put(url, body) { return this.request(url, { method: 'PUT', body }); },

  delete(url) { return this.request(url, { method: 'DELETE' }); }
};
