 'use strict';
const CACHE = 'notepad98-shell-1.8.0';
const FILES = ['browser.html','browser.css','browser.js','install.js','manifest.webmanifest','icon.svg','icon-192.png','icon-512.png'];
const URLS = FILES.map(file => new URL(file, self.registration.scope).href);
self.addEventListener('install', event => {
 event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(URLS)));
});
self.addEventListener('activate', event => {
 event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key.startsWith('notepad98-shell-') && key !== CACHE).map(key => caches.delete(key)))).then(() => self.clients.claim()));
});
// Only cache the editor shell. Never cache APKs, user files or other sites.
// Cache-first keeps each installed shell version consistent; a new worker updates it after all old windows close.
self.addEventListener('fetch', event => {
 if (event.request.method !== 'GET') return;
 const url = new URL(event.request.url); url.search = ''; url.hash = '';
 if (!URLS.includes(url.href)) return;
 event.respondWith(caches.open(CACHE).then(cache => cache.match(url.href)).then(hit => hit || fetch(event.request)));
});
