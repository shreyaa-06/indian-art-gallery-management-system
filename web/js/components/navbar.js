const Navbar = {
  render(title, user) {
    return `
      <header class="top-navbar">
        <div class="navbar-left">
          <button class="menu-toggle" id="menu-toggle" aria-label="Toggle menu">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
            </svg>
          </button>
          <h2 class="page-title">${title}</h2>
        </div>
        <div class="navbar-right">
          <div class="user-info">
            <div class="user-avatar">${Utils.getInitials(user.fullName)}</div>
            <span class="user-name">${user.fullName}</span>
          </div>
          <button class="btn btn-secondary btn-sm" id="logout-btn">Logout</button>
        </div>
      </header>`;
  },

  init(title, user) {
    const container = document.getElementById('app-navbar');
    if (container) {
      container.innerHTML = this.render(title, user);
    }
    document.getElementById('logout-btn')?.addEventListener('click', () => Auth.logout());
  }
};
