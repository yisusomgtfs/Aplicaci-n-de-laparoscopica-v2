const CACHE_NAME = 'hgv-auditoria-v8-1-enterprise';
const ASSETS_TO_CACHE = [
    './',
    './index.html',
    './manifest.json',
    'https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js',
    'icon-192.png',
    'icon-512.png'
];

// Fase 1: Instalación y Captura de Archivos
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
        .then(cache => {
            console.log('Caché clínico asegurado para operación offline.');
            return cache.addAll(ASSETS_TO_CACHE);
        })
    );
});

// Fase 2: Estrategia Cache-First (Intercepta las peticiones de red)
self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request)
        .then(response => {
            // Si el archivo está en la memoria del teléfono, lo entrega instantáneamente
            if (response) { return response; }
            // Si no está, lo busca en internet
            return fetch(event.request);
        })
    );
});

// Fase 3: Mantenimiento (Elimina versiones viejas cuando actualicemos el código futuro)
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cache => {
                    if (cache !== CACHE_NAME) { return caches.delete(cache); }
                })
            );
        })
    );
});
