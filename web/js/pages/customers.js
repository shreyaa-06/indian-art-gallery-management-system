document.addEventListener('DOMContentLoaded', async () => {
  const user = await Auth.requireAuth();
  if (!user) return;

  Sidebar.init('customers');
  Navbar.init('Customers', user);

  let currentPage = 1;
  const searchInput = document.getElementById('search-input');
  const tableBody = document.getElementById('customers-table');
  const paginationEl = document.getElementById('pagination');

  async function loadCustomers() {
    tableBody.innerHTML = '<tr><td colspan="6"><div class="loading"><div class="spinner"></div></div></td></tr>';
    const search = searchInput.value.trim();
    let url = `/api/customers?page=${currentPage}&pageSize=10`;
    if (search) url += `&search=${encodeURIComponent(search)}`;

    try {
      const result = await API.get(url);
      if (result.data.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:2rem">No customers found</td></tr>';
      } else {
        tableBody.innerHTML = result.data.map(c => `
          <tr>
            <td><strong>${c.name}</strong></td>
            <td>${c.email || '—'}</td>
            <td>${c.phone || '—'}</td>
            <td>${Utils.formatDate(c.visitDate)}</td>
            <td>${c.notes ? c.notes.substring(0, 50) + (c.notes.length > 50 ? '...' : '') : '—'}</td>
            <td class="table-actions">
              <button class="btn btn-secondary btn-sm edit-btn" data-id="${c.id}">Edit</button>
              <button class="btn btn-danger btn-sm delete-btn" data-id="${c.id}">Delete</button>
            </td>
          </tr>
        `).join('');

        tableBody.querySelectorAll('.edit-btn').forEach(btn => {
          btn.addEventListener('click', () => openForm(result.data.find(c => c.id === parseInt(btn.dataset.id))));
        });
        tableBody.querySelectorAll('.delete-btn').forEach(btn => {
          btn.addEventListener('click', () => deleteCustomer(parseInt(btn.dataset.id)));
        });
      }
      Pagination.render(paginationEl, {
        page: result.page,
        totalPages: result.totalPages,
        onPageChange: (p) => { currentPage = p; loadCustomers(); }
      });
    } catch {
      tableBody.innerHTML = '<tr><td colspan="6">Failed to load</td></tr>';
    }
  }

  function openForm(customer = null) {
    const isEdit = !!customer;
    const form = document.createElement('form');
    form.id = 'customer-form';
    form.innerHTML = `
      <div class="form-group">
        <label for="name">Name *</label>
        <input class="form-control" id="name" name="name" required value="${customer?.name || ''}">
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="email">Email</label>
          <input class="form-control" id="email" name="email" type="email" value="${customer?.email || ''}">
        </div>
        <div class="form-group">
          <label for="phone">Phone</label>
          <input class="form-control" id="phone" name="phone" value="${customer?.phone || ''}">
        </div>
      </div>
      <div class="form-group">
        <label for="address">Address</label>
        <input class="form-control" id="address" name="address" value="${customer?.address || ''}">
      </div>
      <div class="form-group">
        <label for="visitDate">Visit Date</label>
        <input class="form-control" id="visitDate" name="visitDate" type="date" value="${customer?.visitDate || ''}">
      </div>
      <div class="form-group">
        <label for="notes">Notes</label>
        <textarea class="form-control" id="notes" name="notes" rows="3">${customer?.notes || ''}</textarea>
      </div>`;

    const footer = document.createElement('div');
    footer.style.display = 'flex';
    footer.style.gap = '0.75rem';
    footer.innerHTML = `
      <button type="button" class="btn btn-secondary" id="modal-cancel">Cancel</button>
      <button type="submit" class="btn btn-primary" form="customer-form">${isEdit ? 'Update' : 'Add'} Customer</button>`;

    Modal.open({ title: isEdit ? 'Edit Customer' : 'Add Customer', body: form, footer });
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
          await API.put(`/api/customers/${customer.id}`, data);
          Toast.success('Customer updated');
        } else {
          await API.post('/api/customers', data);
          Toast.success('Customer added');
        }
        Modal.close();
        loadCustomers();
      } catch (err) {
        Toast.error(err.message);
      }
    });
  }

  async function deleteCustomer(id) {
    if (!confirm('Delete this customer?')) return;
    try {
      await API.delete(`/api/customers/${id}`);
      Toast.success('Customer deleted');
      loadCustomers();
    } catch (err) {
      Toast.error(err.message);
    }
  }

  searchInput.addEventListener('input', Utils.debounce(() => { currentPage = 1; loadCustomers(); }));
  document.getElementById('add-btn').addEventListener('click', () => openForm());
  loadCustomers();
});
