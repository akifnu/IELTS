const CACHE = 'shine-v11';
const SHELL = [
  './manifest.json',
  './icons/icon.svg',
  './auth-config.js',
  './images/splash/sunrise-alps.jpg',
  './images/splash/golden-dawn.jpg',
  './images/splash/seedling.jpg',
  './images/splash/open-road.jpg',
  './images/splash/ocean-dawn.jpg',
  './images/splash/ocean-waves.jpg',
  './images/splash/open-book.jpg',
  './images/splash/forest-mist.jpg',
];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(CACHE).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

function isHtmlRequest(req) {
  const url = new URL(req.url);
  return req.mode === 'navigate' || url.pathname.endsWith('/index.html') || url.pathname.endsWith('/');
}

self.addEventListener('fetch', (e) => {
  if (e.request.method !== 'GET') return;
  const url = new URL(e.request.url);
  if (url.origin !== self.location.origin) return;

  if (isHtmlRequest(e.request) || url.pathname.endsWith('/sw.js')) {
    e.respondWith(
      fetch(e.request)
        .then((res) => {
          const copy = res.clone();
          caches.open(CACHE).then((c) => c.put(e.request, copy));
          return res;
        })
        .catch(() => caches.match(e.request))
    );
    return;
  }

  e.respondWith(
    caches.match(e.request).then((cached) => {
      if (cached) return cached;
      return fetch(e.request).then((res) => {
        const copy = res.clone();
        caches.open(CACHE).then((c) => c.put(e.request, copy));
        return res;
      });
    })
  );
});
