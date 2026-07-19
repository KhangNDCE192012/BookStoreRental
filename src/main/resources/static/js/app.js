document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('click', event => {
      if (!window.confirm(el.dataset.confirm || 'Bạn chắc chắn muốn thực hiện?')) event.preventDefault();
    });
  });
});
