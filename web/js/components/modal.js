const Modal = {
  overlay: null,

  init() {
    if (!this.overlay) {
      this.overlay = document.createElement('div');
      this.overlay.className = 'modal-overlay';
      this.overlay.innerHTML = `
        <div class="modal" role="dialog" aria-modal="true">
          <div class="modal-header">
            <h3 id="modal-title"></h3>
            <button class="modal-close" aria-label="Close">&times;</button>
          </div>
          <div class="modal-body" id="modal-body"></div>
          <div class="modal-footer" id="modal-footer"></div>
        </div>`;
      document.body.appendChild(this.overlay);

      this.overlay.querySelector('.modal-close').addEventListener('click', () => this.close());
      this.overlay.addEventListener('click', (e) => {
        if (e.target === this.overlay) this.close();
      });
    }
  },

  open({ title, body, footer }) {
    this.init();
    this.overlay.querySelector('#modal-title').textContent = title;
    const bodyEl = this.overlay.querySelector('#modal-body');
    const footerEl = this.overlay.querySelector('#modal-footer');

    if (typeof body === 'string') {
      bodyEl.innerHTML = body;
    } else {
      bodyEl.innerHTML = '';
      bodyEl.appendChild(body);
    }

    footerEl.innerHTML = '';
    if (footer) footerEl.appendChild(footer);

    this.overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
  },

  close() {
    if (this.overlay) {
      this.overlay.classList.remove('active');
      document.body.style.overflow = '';
    }
  }
};
