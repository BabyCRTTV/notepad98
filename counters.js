'use strict';
(() => {
  const visits = document.getElementById('visit-count');
  const downloads = document.getElementById('download-count');
  if (!visits || !downloads || location.hostname !== 'babycrttv.github.io') return;
  const base = 'https://countapi.mileshilliard.com/api/v1/';
  const keys = {visits: '8bittrade_notepad98_site_visits_v1', downloads: '8bittrade_notepad98_download_clicks_v1'};
  const sessionKey = 'notepad98.site.visit-counted.v1';
  const render = (element, value) => {
    element.textContent = value.toLocaleString();
    element.removeAttribute('title');
  };
  const unavailable = element => {
    element.textContent = '—';
    element.title = 'Counter temporarily unavailable';
  };
  async function request(operation, key) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 8000);
    try {
      const response = await fetch(base + operation + '/' + key, {
        mode: 'cors', credentials: 'omit', referrerPolicy: 'no-referrer',
        cache: 'no-store', keepalive: true, signal: controller.signal
      });
      const data = await response.json();
      if (operation === 'get' && response.status === 404 && data.error === 'Key not found') return 0;
      if (!response.ok || !/^\d+$/.test(String(data.value))) throw new Error('Counter unavailable');
      const value = Number(data.value);
      if (!Number.isSafeInteger(value)) throw new Error('Invalid counter');
      return value;
    } finally { clearTimeout(timer); }
  }
  let counted = false;
  try { counted = sessionStorage.getItem(sessionKey) === '1'; } catch (_) {}
  if (!counted) { try { sessionStorage.setItem(sessionKey, '1'); } catch (_) {} }
  request(counted ? 'get' : 'hit', keys.visits).then(value => render(visits, value)).catch(() => {
    if (!counted) { try { sessionStorage.removeItem(sessionKey); } catch (_) {} }
    unavailable(visits);
  });
  request('get', keys.downloads).then(value => render(downloads, value)).catch(() => unavailable(downloads));
  document.querySelectorAll('a[data-count-download]').forEach(link => {
    const count = event => {
      if ((event.type === 'click' && event.button !== 0) || (event.type === 'auxclick' && event.button !== 1)) return;
      // Keep the anchor's normal download/navigation behavior. The request can finish after navigation.
      request('hit', keys.downloads).then(value => render(downloads, value)).catch(() => unavailable(downloads));
    };
    link.addEventListener('click', count);
    link.addEventListener('auxclick', count);
  });
})();
