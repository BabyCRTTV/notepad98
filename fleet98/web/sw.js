self.addEventListener('install',()=>self.skipWaiting());
self.addEventListener('activate',event=>event.waitUntil((async()=>{await caches.delete('fleet98-v1.0.0');const windows=await self.clients.matchAll({type:'window'});await Promise.all(windows.map(client=>client.navigate('https://babycrttv.github.io/fleet98/')));await self.registration.unregister();})()));
