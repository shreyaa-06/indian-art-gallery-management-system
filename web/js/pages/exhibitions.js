const EXHIBITION_STATUSES = ['upcoming', 'active', 'completed', 'cancelled'];

document.addEventListener('DOMContentLoaded', async () => {
  const user = await Auth.requireAuth();
  if (!user) return;

  Sidebar.init('exhibitions');
  Navbar.init('Exhibitions', user);

  let currentPage = 1;
  const statusFilter = document.getElementById('status-filter');
  const tableBody = document.getElementById('exhibitions-table');
  const paginationEl = document.getElementById('pagination');

  EXHIBITION_STATUSES.forEach(s => {
    statusFilter.innerHTML += `<option value="${s}">${Utils.capitalize(s)}</option>`;
  });

  async function loadExhibitions() {
    tableBody.innerHTML = '<tr><td colspan="6"><div class="loading"><div class="spinner"></div></div></td></tr>';
    const status = statusFilter.value;
    let url = `/api/exhibitions?page=${currentPage}&pageSize=10`;
    if (status) url += `&status=${status}`;

    try {
      const result = await API.get(url);
      if (result.data.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:2rem">No exhibitions found</td></tr>';
      } else {
        tableBody.innerHTML = result.data.map(ex => `
          <tr>
            <td><strong>${ex.title}</strong></td>
            <td>${ex.location || '—'}</td>
            <td>${Utils.formatDate(ex.startDate)} – ${Utils.formatDate(ex.endDate)}</td>
            <td><span class="badge badge-${ex.status}">${Utils.capitalize(ex.status)}</span></td>
            <td class="table-actions">
              <button class="btn btn-secondary btn-sm view-btn" data-id="${ex.id}">View</button>
              <button class="btn btn-secondary btn-sm edit-btn" data-id="${ex.id}">Edit</button>
              <button class="btn btn-secondary btn-sm assign-btn" data-id="${ex.id}">Assign</button>
              <button class="btn btn-danger btn-sm delete-btn" data-id="${ex.id}">Delete</button>
            </td>
          </tr>
        `).join('');

        tableBody.querySelectorAll('.edit-btn').forEach(btn => {
          btn.addEventListener('click', () => openForm(result.data.find(e => e.id === parseInt(btn.dataset.id))));
        });
        tableBody.querySelectorAll('.view-btn').forEach(btn => {
          btn.addEventListener('click', () => viewExhibition(parseInt(btn.dataset.id)));
        });
        tableBody.querySelectorAll('.assign-btn').forEach(btn => {
          btn.addEventListener('click', () => openAssign(parseInt(btn.dataset.id)));
        });
        tableBody.querySelectorAll('.delete-btn').forEach(btn => {
          btn.addEventListener('click', () => deleteExhibition(parseInt(btn.dataset.id)));
        });
      }
      Pagination.render(paginationEl, {
        page: result.page,
        totalPages: result.totalPages,
        onPageChange: (p) => { currentPage = p; loadExhibitions(); }
      });
    } catch {
      tableBody.innerHTML = '<tr><td colspan="6">Failed to load</td></tr>';
    }
  }

  function openForm(exhibition = null) {
    const isEdit = !!exhibition;
    const form = document.createElement('form');
    form.id = 'exhibition-form';
    form.innerHTML = `
      <div class="form-group">
        <label for="title">Title *</label>
        <input class="form-control" id="title" name="title" required value="${exhibition?.title || ''}">
      </div>
      <div class="form-group">
        <label for="description">Description</label>
        <textarea class="form-control" id="description" name="description" rows="3">${exhibition?.description || ''}</textarea>
      </div>
      <div class="form-group">
        <label for="location">Location</label>
        <input class="form-control" id="location" name="location" value="${exhibition?.location || ''}">
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="startDate">Start Date *</label>
          <input class="form-control" id="startDate" name="startDate" type="date" required value="${exhibition?.startDate || ''}">
        </div>
        <div class="form-group">
          <label for="endDate">End Date *</label>
          <input class="form-control" id="endDate" name="endDate" type="date" required value="${exhibition?.endDate || ''}">
        </div>
      </div>
      <div class="form-group">
        <label for="status">Status *</label>
        <select class="form-control" id="status" name="status" required>
          ${EXHIBITION_STATUSES.map(s => `<option value="${s}" ${exhibition?.status === s ? 'selected' : ''}>${Utils.capitalize(s)}</option>`).join('')}
        </select>
      </div>`;

    const footer = document.createElement('div');
    footer.style.display = 'flex';
    footer.style.gap = '0.75rem';
    footer.innerHTML = `
      <button type="button" class="btn btn-secondary" id="modal-cancel">Cancel</button>
      <button type="submit" class="btn btn-primary" form="exhibition-form">${isEdit ? 'Update' : 'Create'} Exhibition</button>`;

    Modal.open({ title: isEdit ? 'Edit Exhibition' : 'Create Exhibition', body: form, footer });
    footer.querySelector('#modal-cancel').addEventListener('click', () => Modal.close());

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      Utils.clearFormErrors(form);
      const data = Object.fromEntries(new FormData(form));
      if (!data.title.trim()) { Utils.showFieldError(form.title, 'Required'); return; }
      if (!data.startDate) { Utils.showFieldError(form.startDate, 'Required'); return; }
      if (!data.endDate) { Utils.showFieldError(form.endDate, 'Required'); return; }
      if (data.startDate > data.endDate) {
        Utils.showFieldError(form.endDate, 'End date must be after start date'); return;
      }

      try {
        if (isEdit) {
          await API.put(`/api/exhibitions/${exhibition.id}`, data);
          Toast.success('Exhibition updated');
        } else {
          await API.post('/api/exhibitions', data);
          Toast.success('Exhibition created');
        }
        Modal.close();
        loadExhibitions();
      } catch (err) {
        Toast.error(err.message);
      }
    });
  }

  async function viewExhibition(id) {
    try {
      const ex = await API.get(`/api/exhibitions/${id}`);
      const artworks = ex.artworks && ex.artworks.length > 0
        ? ex.artworks.map(a => `<li>${a.title} — ${a.artistName}</li>`).join('')
        : '<li>No artworks assigned</li>';

      const body = document.createElement('div');
      body.innerHTML = `
        <p><strong>Location:</strong> ${ex.location || '—'}</p>
        <p><strong>Schedule:</strong> ${Utils.formatDate(ex.startDate)} – ${Utils.formatDate(ex.endDate)}</p>
        <p><strong>Status:</strong> <span class="badge badge-${ex.status}">${Utils.capitalize(ex.status)}</span></p>
        ${ex.description ? `<p>${ex.description}</p>` : ''}
        <h4 style="margin-top:1rem">Assigned Artworks</h4>
        <ul style="list-style:disc;padding-left:1.5rem;margin-top:0.5rem">${artworks}</ul>`;

      const footer = document.createElement('div');
      footer.innerHTML = '<button class="btn btn-secondary" id="modal-close-btn">Close</button>';
      Modal.open({ title: ex.title, body, footer });
      footer.querySelector('#modal-close-btn').addEventListener('click', () => Modal.close());
    } catch (err) {
      Toast.error(err.message);
    }
  }

  async function openAssign(exhibitionId) {
    try {
      const [exhibition, artworksResult] = await Promise.all([
        API.get(`/api/exhibitions/${exhibitionId}`),
        API.get('/api/artworks?page=1&pageSize=100')
      ]);

      const assignedIds = new Set((exhibition.artworks || []).map(a => a.id));
      const form = document.createElement('form');
      form.id = 'assign-form';
      form.innerHTML = `
        <p style="margin-bottom:1rem;color:var(--color-text-muted);font-size:0.875rem">
          Select artworks for "${exhibition.title}"
        </p>
        <div class="checkbox-list">
          ${artworksResult.data.map(a => `
            <div class="checkbox-item">
              <input type="checkbox" id="art-${a.id}" name="artworkIds" value="${a.id}"
                ${assignedIds.has(a.id) ? 'checked' : ''}>
              <label for="art-${a.id}">
                ${a.title}
                <div class="sub">${a.artistName} · ${a.category}</div>
              </label>
            </div>
          `).join('')}
        </div>`;

      const footer = document.createElement('div');
      footer.style.display = 'flex';
      footer.style.gap = '0.75rem';
      footer.innerHTML = `
        <button type="button" class="btn btn-secondary" id="modal-cancel">Cancel</button>
        <button type="submit" class="btn btn-primary" form="assign-form">Save Assignment</button>`;

      Modal.open({ title: 'Assign Artworks', body: form, footer });
      footer.querySelector('#modal-cancel').addEventListener('click', () => Modal.close());

      form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const checked = [...form.querySelectorAll('input[name="artworkIds"]:checked')].map(c => parseInt(c.value));
        try {
          await API.put(`/api/exhibitions/${exhibitionId}/artworks`, { artworkIds: checked });
          Toast.success('Artworks assigned');
          Modal.close();
        } catch (err) {
          Toast.error(err.message);
        }
      });
    } catch (err) {
      Toast.error(err.message);
    }
  }

  async function deleteExhibition(id) {
    if (!confirm('Delete this exhibition?')) return;
    try {
      await API.delete(`/api/exhibitions/${id}`);
      Toast.success('Exhibition deleted');
      loadExhibitions();
    } catch (err) {
      Toast.error(err.message);
    }
  }

  statusFilter.addEventListener('change', () => { currentPage = 1; loadExhibitions(); });
  document.getElementById('add-btn').addEventListener('click', () => openForm());
  loadExhibitions();
});
