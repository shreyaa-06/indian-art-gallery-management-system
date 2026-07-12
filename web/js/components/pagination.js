const Pagination = {
  render(container, { page, totalPages, onPageChange }) {
    if (totalPages <= 1) {
      container.innerHTML = '';
      return;
    }

    let html = '';
    html += `<button ${page <= 1 ? 'disabled' : ''} data-page="${page - 1}">Prev</button>`;

    const range = this.getPageRange(page, totalPages);
    range.forEach(p => {
      if (p === '...') {
        html += `<button disabled>...</button>`;
      } else {
        html += `<button class="${p === page ? 'active' : ''}" data-page="${p}">${p}</button>`;
      }
    });

    html += `<button ${page >= totalPages ? 'disabled' : ''} data-page="${page + 1}">Next</button>`;
    container.innerHTML = html;

    container.querySelectorAll('button[data-page]').forEach(btn => {
      btn.addEventListener('click', () => {
        const newPage = parseInt(btn.dataset.page);
        if (newPage >= 1 && newPage <= totalPages) onPageChange(newPage);
      });
    });
  },

  getPageRange(current, total) {
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const pages = [1];
    if (current > 3) pages.push('...');
    for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
      pages.push(i);
    }
    if (current < total - 2) pages.push('...');
    pages.push(total);
    return pages;
  }
};
