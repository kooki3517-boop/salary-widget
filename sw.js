const C='mind-fragment-v20261109-webaudio';
const ASSETS=['./','./index.html','./cards.json','./bgm.wav','./rail-ambience.wav','./click.wav','./pen-stroke.wav','./icon-192.png','./icon-512.png','./kami-lost-found.webp','./manifest.webmanifest'];
self.addEventListener('install',event=>{event.waitUntil(caches.open(C).then(cache=>cache.addAll(ASSETS)).then(()=>self.skipWaiting()));});
self.addEventListener('activate',event=>{event.waitUntil(Promise.all([caches.keys().then(keys=>Promise.all(keys.filter(key=>key!==C).map(key=>caches.delete(key)))),self.clients.claim()]));});
self.addEventListener('fetch',event=>{const req=event.request;if(req.method!=='GET'||new URL(req.url).origin!==self.location.origin)return;event.respondWith(fetch(req).then(res=>{if(res.ok){const copy=res.clone();event.waitUntil(caches.open(C).then(cache=>cache.put(req,copy)));}return res;}).catch(()=>caches.match(req)));});
