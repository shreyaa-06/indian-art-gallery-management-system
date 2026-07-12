const CATEGORIES = ['Painting', 'Sculpture', 'Photography', 'Digital', 'Mixed Media', 'Installation', 'Other'];
const STATUSES = ['available', 'on_loan', 'sold', 'in_exhibition'];

document.addEventListener('DOMContentLoaded', async () => {
  const user = await Auth.requireAuth();
  if (!user) return;

  Sidebar.init('artworks');
  Navbar.init('Artworks', user);

  let currentPage = 1;
  let artists = [];

  try {
    artists = await API.get('/api/artists/all');
  } catch { /* ignore */ }

  const searchInput = document.getElementById('search-input');
  const categoryFilter = document.getElementById('category-filter');
  const grid = document.getElementById('artwork-grid');
  const paginationEl = document.getElementById('pagination');

  CATEGORIES.forEach(cat => {
    categoryFilter.innerHTML += `<option value="${cat}">${cat}</option>`;
  });

  async function loadArtworks() {
    grid.innerHTML = '<div class="loading"><div class="spinner"></div></div>';
    const search = searchInput.value.trim();
    const category = categoryFilter.value;
    let url = `/api/artworks?page=${currentPage}&pageSize=12`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    if (category) url += `&category=${encodeURIComponent(category)}`;

    try {
      const result = await API.get(url);
      if (result.data.length === 0) {
        grid.innerHTML = '<div class="empty-state"><p>No artworks found</p></div>';
      } else {
        grid.innerHTML = result.data.map(a => renderCard(a)).join('');
        grid.querySelectorAll('.artwork-card').forEach(card => {
          card.addEventListener('click', () => {
            window.location.href = `/pages/artwork-detail.html?id=${card.dataset.id}`;
          });
        });
      }
      Pagination.render(paginationEl, {
        page: result.page,
        totalPages: result.totalPages,
        onPageChange: (p) => { currentPage = p; loadArtworks(); }
      });
    } catch {
      grid.innerHTML = '<div class="empty-state"><p>Failed to load artworks</p></div>';
    }
  }

  function renderCard(a) {
    const img = a.imageUrl
      ? `<img src="${a.imageUrl}" alt="${a.title}">`
      : `<svg class="placeholder" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>`;
    return `
      <div class="artwork-card" data-id="${a.id}">
        <div class="artwork-card-image">${img}</div>
        <div class="artwork-card-body">
          <h4>${a.title}</h4>
          <div class="artist">${a.artistName}</div>
          <div class="artwork-card-meta">
            <span class="badge badge-${a.status}">${Utils.capitalize(a.status)}</span>
            <span>${Utils.formatCurrency(a.price)}</span>
          </div>
        </div>
      </div>`;
  }

  function openForm(artwork = null) {
    const isEdit = !!artwork;
    const form = document.createElement('form');
    form.id = 'artwork-form';
    form.innerHTML = `
      <div class="form-group">
        <label for="title">Title *</label>
        <input class="form-control" id="title" name="title" required value="${artwork?.title || ''}">
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="artistId">Artist *</label>
          <select class="form-control" id="artistId" name="artistId" required>
            <option value="">Select artist</option>
            ${artists.map(ar => `<option value="${ar.id}" ${artwork?.artistId === ar.id ? 'selected' : ''}>${ar.name}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label for="category">Category *</label>
          <select class="form-control" id="category" name="category" required>
            ${CATEGORIES.map(c => `<option value="${c}" ${artwork?.category === c ? 'selected' : ''}>${c}</option>`).join('')}
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="medium">Medium</label>
          <input class="form-control" id="medium" name="medium" value="${artwork?.medium || ''}">
        </div>
        <div class="form-group">
          <label for="yearCreated">Year</label>
          <input class="form-control" id="yearCreated" name="yearCreated" type="number" value="${artwork?.yearCreated || ''}">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="dimensions">Dimensions</label>
          <input class="form-control" id="dimensions" name="dimensions" value="${artwork?.dimensions || ''}">
        </div>
        <div class="form-group">
          <label for="price">Price (₹)</label>
          <input class="form-control" id="price" name="price" type="number" step="0.01" value="${artwork?.price || ''}">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="status">Status *</label>
          <select class="form-control" id="status" name="status" required>
            ${STATUSES.map(s => `<option value="${s}" ${artwork?.status === s ? 'selected' : ''}>${Utils.capitalize(s)}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label for="imageUrl">Image URL</label>
          <input class="form-control" id="imageUrl" name="imageUrl" value="${artwork?.imageUrl || ''}">
        </div>
      </div>
      <div class="form-group">
        <label for="description">Description</label>
        <textarea class="form-control" id="description" name="description" rows="3">${artwork?.description || ''}</textarea>
      </div>`;

    const footer = document.createElement('div');
    footer.style.display = 'flex';
    footer.style.gap = '0.75rem';
    footer.innerHTML = `
      <button type="button" class="btn btn-secondary" id="modal-cancel">Cancel</button>
      <button type="submit" class="btn btn-primary" form="artwork-form">${isEdit ? 'Update' : 'Add'} Artwork</button>`;

    Modal.open({ title: isEdit ? 'Edit Artwork' : 'Add Artwork', body: form, footer });

    footer.querySelector('#modal-cancel').addEventListener('click', () => Modal.close());

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      Utils.clearFormErrors(form);

      const data = Object.fromEntries(new FormData(form));
      if (!data.title.trim()) { Utils.showFieldError(form.title, 'Required'); return; }
      if (!data.artistId) { Utils.showFieldError(form.artistId, 'Required'); return; }

      try {
        if (isEdit) {
          await API.put(`/api/artworks/${artwork.id}`, data);
          Toast.success('Artwork updated');
        } else {
          await API.post('/api/artworks', data);
          Toast.success('Artwork added');
        }
        Modal.close();
        loadArtworks();
      } catch (err) {
        Toast.error(err.message);
      }
    });
  }

  searchInput.addEventListener('input', Utils.debounce(() => { currentPage = 1; loadArtworks(); }));
  categoryFilter.addEventListener('change', () => { currentPage = 1; loadArtworks(); });
  document.getElementById('add-btn').addEventListener('click', () => openForm());

  loadArtworks();
});
