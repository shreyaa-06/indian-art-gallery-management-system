document.addEventListener('DOMContentLoaded', async () => {
  const user = await Auth.requireAuth();
  if (!user) return;

  Sidebar.init('dashboard');
  Navbar.init('Dashboard', user);

  try {
    const stats = await API.get('/api/dashboard');

    document.getElementById('stat-artworks').textContent = stats.totalArtworks;
    document.getElementById('stat-artists').textContent = stats.totalArtists;
    document.getElementById('stat-exhibitions').textContent = stats.activeExhibitions;

    const activityList = document.getElementById('activity-list');
    if (stats.recentActivity && stats.recentActivity.length > 0) {
      activityList.innerHTML = stats.recentActivity.map(a => `
        <div class="activity-item">
          <div class="activity-dot"></div>
          <div>
            <div class="activity-text">${a.description}</div>
            <div class="activity-time">${Utils.formatDateTime(a.createdAt)}</div>
          </div>
        </div>
      `).join('');
    } else {
      activityList.innerHTML = '<div class="empty-state"><p>No curatorial activity yet</p></div>';
    }
  } catch (err) {
    Toast.error('Failed to load dashboard data');
  }
});
