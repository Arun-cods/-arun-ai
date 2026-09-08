const CACHE_NAME = 'arun-ai-v3-speed';
const ASSETS_TO_CACHE = [
  '/icon.svg',
  '/icon-192.png',
  '/icon-512.png',
  '/manifest.json'
];

self.addEventListener('install', (e) => {
  self.skipWaiting();
  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS_TO_CACHE))
  );
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.map((k) => k !== CACHE_NAME && caches.delete(k)))
    ).then(() => self.clients.claim())
  );
});

self.addEventListener('message', (e) => {
  if (e.data && (e.data === 'SKIP_WAITING' || e.data.action === 'skipWaiting' || e.data.type === 'SKIP_WAITING')) {
    self.skipWaiting();
  }
});

self.addEventListener('fetch', (e) => {
  const url = new URL(e.request.url);

  // Network-First for HTML navigation so updates are instant and users never see stale UI
  if (e.request.mode === 'navigate' || e.request.destination === 'document' || url.pathname === '/' || url.pathname.endsWith('.html')) {
    e.respondWith(
      fetch(e.request).catch(() => caches.match('/') || caches.match(e.request))
    );
    return;
  }

  // Never cache chat, image, or dynamic API endpoints
  if (url.pathname.startsWith('/chat') || url.pathname.startsWith('/api') || url.pathname.startsWith('/qr')) {
    return;
  }

  if (e.request.method === 'GET' && e.request.url.startsWith(self.location.origin)) {
    e.respondWith(
      caches.match(e.request).then((cached) => cached || fetch(e.request))
    );
  }
});

