const CACHE_NAME = 'zepe-cache-v1';
const urlsToCache = [
  '/icons',
  '/public/style.css',
  'https://fonts.googleapis.com/css2?family=Inter:wght@400;800&display=swap'
];

self.addEventListener('install', function(event) {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function(cache) {
        console.log('Opened cache');
        return cache.addAll(urlsToCache);
      })
  );
});