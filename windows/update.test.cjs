'use strict';
const {test}=require('node:test');const assert=require('node:assert/strict');const crypto=require('node:crypto');
const {newer,entry,verified,bytes}=require('./update.cjs');
const fixture={schema:1,windows:{version:'1.8.4',sha256:'a'.repeat(64),url:'https://github.com/BabyCRTTV/notepad98/releases/download/windows-v1.8.4/Notepad98-Setup-1.8.4-x64.exe'}};
test('numeric versions avoid lexical mistakes and downgrades',()=>{assert(newer('1.10.0','1.9.9'));assert(!newer('1.8.4','1.8.4'));assert(!newer('1.8.3','1.8.4'));assert.throws(()=>newer('bad','1.0.0'));});
test('only exact official release installers are accepted',()=>{assert.equal(entry(fixture).version,'1.8.4');for(const url of ['http://github.com/x','https://evil.test/app.exe',fixture.windows.url+'?x'])assert.throws(()=>entry({...fixture,windows:{...fixture.windows,url}}));assert.throws(()=>entry({...fixture,windows:{...fixture.windows,sha256:'bad'}}));});
test('corrupt installer bytes are refused',()=>{const b=Buffer.from('test');const hash=crypto.createHash('sha256').update(b).digest('hex');assert(verified(b,hash));assert(!verified(Buffer.from('changed'),hash));});
test('offline and oversized update responses fail cleanly',async()=>{await assert.rejects(()=>bytes(async()=>{throw Error('offline');},'unused',1));await assert.rejects(()=>bytes(async()=>({ok:true,body:[Buffer.from('large')]}),'unused',1));});
