document.addEventListener('DOMContentLoaded', async () => {
  const user = await Auth.requireAuth();
  if (!user) return;

  Sidebar.init('reports');
  Navbar.init('Reports', user);

  try {
    const data = await API.get('/api/reports');

    renderBarChart('category-chart', data.artworksByCategory);
    renderBarChart('status-chart', data.artworksByStatus);
    renderBarChart('exhibition-chart', data.exhibitionsByStatus);

    document.getElementById('total-value').textContent = Utils.formatCurrency(data.totalArtworkValue);
    document.getElementById('recent-visitors').textContent = data.recentCustomers;
  } catch {
    Toast.error('Failed to load reports');
  }

  function renderBarChart(containerId, items) {
    const container = document.getElementById(containerId);
    if (!items || items.length === 0) {
      container.innerHTML = '<p style="color:var(--color-text-muted);font-size:0.875rem">No data available</p>';
      return;
    }
    const max = Math.max(...items.map(i => i.count));
    container.innerHTML = items.map(item => `
      <div class="report-bar">
        <span class="report-bar-label">${Utils.capitalize(item.label)}</span>
        <div class="report-bar-track">
          <div class="report-bar-fill" style="width:${(item.count / max) * 100}%"></div>
        </div>
        <span class="report-bar-count">${item.count}</span>
      </div>
    `).join('');
  }
});
