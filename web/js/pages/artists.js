document.addEventListener('DOMContentLoaded', async () => {
  const user = await Auth.requireAuth();
  if (!user) return;

  Sidebar.init('artists');
  Navbar.init('Artists', user);

  let currentPage = 1;
  const searchInput = document.getElementById('search-input');
  const tableBody = document.getElementById('artists-table');
  const paginationEl = document.getElementById('pagination');

  async function loadArtists() {
    tableBody.innerHTML = '<tr><td colspan="6" class="loading"><div class="spinner"></div></td></tr>';
    const search = searchInput.value.trim();
    let url = `/api/artists?page=${currentPage}&pageSize=10`;
    if (search) url += `&search=${encodeURIComponent(search)}`;

    try {
      const result = await API.get(url);
      if (result.data.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--color-text-muted)">No artists found</td></tr>';
      } else {
        tableBody.innerHTML = result.data.map(a => `
          <tr>
            <td><strong>${a.name}</strong></td>
            <td>${a.nationality || '—'}</td>
            <td>${a.birthYear || '—'}${a.deathYear ? ' – ' + a.deathYear : ''}</td>
            <td>${a.email || '—'}</td>
            <td>${a.phone || '—'}</td>
            <td class="table-actions">
              <button class="btn btn-secondary btn-sm edit-btn" data-id="${a.id}">Edit</button>
              <button class="btn btn-danger btn-sm delete-btn" data-id="${a.id}">Delete</button>
            </td>
          </tr>
        `).join('');

        tableBody.querySelectorAll('.edit-btn').forEach(btn => {
          btn.addEventListener('click', () => openForm(result.data.find(a => a.id === parseInt(btn.dataset.id))));
        });
        tableBody.querySelectorAll('.delete-btn').forEach(btn => {
          btn.addEventListener('click', () => deleteArtist(parseInt(btn.dataset.id)));
        });
      }
      Pagination.render(paginationEl, {
        page: result.page,
        totalPages: result.totalPages,
        onPageChange: (p) => { currentPage = p; loadArtists(); }
      });
    } catch {
      tableBody.innerHTML = '<tr><td colspan="6">Failed to load</td></tr>';
    }
  }

  function openForm(artist = null) {
    const isEdit = !!artist;
    const form = document.createElement('form');
    form.id = 'artist-form';
    form.innerHTML = `
      <div class="form-group">
        <label for="name">Name *</label>
        <input class="form-control" id="name" name="name" required value="${artist?.name || ''}">
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="nationality">Nationality</label>
          <input class="form-control" id="nationality" name="nationality" value="${artist?.nationality || ''}">
        </div>
        <div class="form-group">
          <label for="email">Email</label>
          <input class="form-control" id="email" name="email" type="email" value="${artist?.email || ''}">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="birthYear">Birth Year</label>
          <input class="form-control" id="birthYear" name="birthYear" type="number" value="${artist?.birthYear || ''}">
        </div>
        <div class="form-group">
          <label for="deathYear">Death Year</label>
          <input class="form-control" id="deathYear" name="deathYear" type="number" value="${artist?.deathYear || ''}">
        </div>
      </div>
      <div class="form-group">
        <label for="phone">Phone</label>
        <input class="form-control" id="phone" name="phone" value="${artist?.phone || ''}">
      </div>
      <div class="form-group">
        <label for="bio">Biography</label>
        <textarea class="form-control" id="bio" name="bio" rows="3">${artist?.bio || ''}</textarea>
      </div>`;

    const footer = document.createElement('div');
    footer.style.display = 'flex';
    footer.style.gap = '0.75rem';
    footer.innerHTML = `
      <button type="button" class="btn btn-secondary" id="modal-cancel">Cancel</button>
      <button type="submit" class="btn btn-primary" form="artist-form">${isEdit ? 'Update' : 'Add'} Artist</button>`;

    Modal.open({ title: isEdit ? 'Edit Artist' : 'Add Artist', body: form, footer });
    footer.querySelector('#modal-cancel').addEventListener('click', () => Modal.close());

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      Utils.clearFormErrors(form);
      const data = Object.fromEntries(new FormData(form));
      if (!data.name.trim()) { Utils.showFieldError(form.name, 'Required'); return; }
      if (data.email && !Utils.validateEmail(data.email)) {
        Utils.showFieldError(form.email, 'Invalid email'); return;
      }

      try {
        if (isEdit) {
          await API.put(`/api/artists/${artist.id}`, data);
          Toast.success('Artist updated');
        } else {
          await API.post('/api/artists', data);
          Toast.success('Artist added');
        }
        Modal.close();
        loadArtists();
      } catch (err) {
        Toast.error(err.message);
      }
    });
  }

  async function deleteArtist(id) {
    if (!confirm('Delete this artist? Artworks linked to them may be affected.')) return;
    try {
      await API.delete(`/api/artists/${id}`);
      Toast.success('Artist deleted');
      loadArtists();
    } catch (err) {
      Toast.error(err.message);
    }
  }

  searchInput.addEventListener('input', Utils.debounce(() => { currentPage = 1; loadArtists(); }));
  document.getElementById('add-btn').addEventListener('click', () => openForm());
  loadArtists();
});
