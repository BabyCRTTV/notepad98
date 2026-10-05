'use strict';
const demo = document.getElementById('demo');
const status = document.getElementById('theme-status');
document.querySelectorAll('[data-theme]').forEach(button => {
  button.addEventListener('click', () => {
    demo.className = 'note ' + button.dataset.theme;
    document.querySelectorAll('[data-theme]').forEach(other => other.setAttribute('aria-pressed', String(other === button)));
    status.textContent = 'Previewing ' + button.textContent;
  });
});
