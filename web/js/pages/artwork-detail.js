document.addEventListener('DOMContentLoaded', async () => {
  const user = await Auth.requireAuth();
  if (!user) return;

  Sidebar.init('artworks');
  Navbar.init('Artwork Details', user);

  const id = Utils.getQueryParam('id');
  if (!id) {
    window.location.href = '/pages/artworks.html';
    return;
  }

  const container = document.getElementById('detail-container');

  try {
    const artwork = await API.get(`/api/artworks/${id}`);
    const img = artwork.imageUrl
      ? `<img src="${artwork.imageUrl}" alt="${artwork.title}">`
      : `<svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.3"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>`;

    container.innerHTML = `
      <div class="artwork-detail">
        <div class="artwork-detail-image">${img}</div>
        <div class="artwork-detail-info">
          <h2>${artwork.title}</h2>
          <span class="artist-link">by ${artwork.artistName}</span>
          <span class="badge badge-${artwork.status}">${Utils.capitalize(artwork.status)}</span>
          <div class="detail-grid">
            <div class="detail-item"><label>Category</label><span>${artwork.category}</span></div>
            <div class="detail-item"><label>Medium</label><span>${artwork.medium || '—'}</span></div>
            <div class="detail-item"><label>Year</label><span>${artwork.yearCreated || '—'}</span></div>
            <div class="detail-item"><label>Dimensions</label><span>${artwork.dimensions || '—'}</span></div>
            <div class="detail-item"><label>Price</label><span>${Utils.formatCurrency(artwork.price)}</span></div>
            <div class="detail-item"><label>Added</label><span>${Utils.formatDate(artwork.createdAt)}</span></div>
          </div>
          ${artwork.description ? `<p>${artwork.description}</p>` : ''}
          <div class="artwork-detail-actions">
            <button class="btn btn-primary" id="edit-btn">Edit</button>
            <button class="btn btn-danger" id="delete-btn">Delete</button>
            <a href="/pages/artworks.html" class="btn btn-secondary">Back to Gallery</a>
          </div>
        </div>
      </div>`;

    document.getElementById('edit-btn').addEventListener('click', () => {
      window.location.href = `/pages/artworks.html?edit=${id}`;
    });

    document.getElementById('delete-btn').addEventListener('click', async () => {
      if (!confirm('Are you sure you want to delete this artwork?')) return;
      try {
        await API.delete(`/api/artworks/${id}`);
        Toast.success('Artwork deleted');
        window.location.href = '/pages/artworks.html';
      } catch (err) {
        Toast.error(err.message);
      }
    });
  } catch {
    container.innerHTML = '<div class="empty-state"><p>Artwork not found</p></div>';
  }
});
