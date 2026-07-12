const Sidebar = {
  NAV_ITEMS: [
    { href: '/pages/dashboard.html', label: 'Dashboard', icon: 'grid', page: 'dashboard' },
    { href: '/pages/artworks.html', label: 'Artworks', icon: 'image', page: 'artworks' },
    { href: '/pages/artists.html', label: 'Artists', icon: 'users', page: 'artists' },
    { href: '/pages/exhibitions.html', label: 'Exhibitions', icon: 'calendar', page: 'exhibitions' },
    { href: '/pages/customers.html', label: 'Customers', icon: 'user', page: 'customers' },
    { href: '/pages/reports.html', label: 'Reports', icon: 'chart', page: 'reports' }
  ],

  ICONS: {
    grid: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>',
    image: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>',
    users: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>',
    calendar: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>',
    user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
    chart: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>'
  },

  render(activePage) {
    const nav = this.NAV_ITEMS.map(item => `
      <a href="${item.href}" class="nav-item ${item.page === activePage ? 'active' : ''}">
        ${this.ICONS[item.icon]}
        ${item.label}
      </a>
    `).join('');

    return `
      <aside class="sidebar" id="sidebar">
        <div class="sidebar-brand">
          <h1>ShilpSangraha</h1>
          <span>Indian Heritage Gallery</span>
        </div>
        <nav class="sidebar-nav" aria-label="Main navigation">${nav}</nav>
        <div class="sidebar-footer">&copy; 2026 ShilpSangraha</div>
      </aside>
      <div class="sidebar-overlay" id="sidebar-overlay"></div>`;
  },

  init(activePage) {
    const container = document.getElementById('app-sidebar');
    if (container) {
      container.innerHTML = this.render(activePage);
    }

    const toggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebar-overlay');

    if (toggle && sidebar) {
      toggle.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        overlay?.classList.toggle('visible');
      });
    }
    overlay?.addEventListener('click', () => {
      sidebar?.classList.remove('open');
      overlay.classList.remove('visible');
    });
  }
};
