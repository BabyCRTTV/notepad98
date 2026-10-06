 'use strict';
(() => {
 const button = document.getElementById('install-app');
 const status = document.getElementById('offline-status');
 let prompt;
 const standalone = () => window.matchMedia('(display-mode: standalone)').matches;
 if (standalone()) button.hidden = true;
 window.addEventListener('beforeinstallprompt', event => {
   event.preventDefault(); prompt = event;
 });
 button.addEventListener('click', async () => {
   if (!prompt) { document.getElementById('install-dialog').showModal(); return; }
   const pending = prompt; prompt = null;
   try { await pending.prompt(); await pending.userChoice; }
   catch (_) { document.getElementById('install-dialog').showModal(); }
 });
 window.addEventListener('appinstalled', () => { button.hidden = true; prompt = null; });
 if ('serviceWorker' in navigator && location.protocol === 'https:') {
   navigator.serviceWorker.register('sw.js').then(() => navigator.serviceWorker.ready)
     .then(() => { status.textContent = 'Ready for offline use. APK downloads and external links still need internet.'; })
     .catch(() => { status.textContent = 'Offline setup unavailable. You can still edit online and download notes.'; });
 } else { status.textContent = 'Offline installation requires a supported browser and HTTPS.'; }
})();
