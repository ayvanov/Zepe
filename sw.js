const CACHE_NAME = "zepe-cache-v1";
const urlsToCache = [
  "/icons",
  "/public/style.css",
  "/public/cog.svg",
  "https://fonts.googleapis.com/css2?family=Inter:wght@400;800&display=swap",
  "https://cdn.jsdelivr.net/gh/vanjs-org/van/public/van-1.2.8.nomodule.min.js",
  "https://cdn.jsdelivr.net/npm/vanjs-ui@0.10.0/dist/van-ui.nomodule.min.js",
];

self.addEventListener("install", function (e) {
  e.waitUntil(
    caches.open(CACHE_NAME).then(function (cache) {
      console.log("Opened cache");
      return cache.addAll(urlsToCache);
    })
  );
});

self.addEventListener("fetch", (e) => {
  e.respondWith(
    caches.match(e.request).then((response) => {
      if (response) return response;
      else return fetch(e.request);
    })
  );
});
