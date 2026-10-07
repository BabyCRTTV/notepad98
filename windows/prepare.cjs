'use strict';
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const ui = path.join(__dirname, 'ui');
fs.mkdirSync(ui, {recursive:true});
for (const file of ['browser.css', 'browser.js', 'icon.svg', 'icon-512.png']) fs.copyFileSync(path.join(root,file),path.join(ui,file));
let html=fs.readFileSync(path.join(root,'browser.html'),'utf8');
html=html.replace('<link rel="manifest" href="manifest.webmanifest">','')
 .replace('<script src="install.js"></script>','')
 .replace(/<header class="site-header">[\s\S]*?<\/header>/,'<header class="site-header"><strong>Notepad 98</strong><span>8BitTrade · Windows 1.8.1</span></header>')
 .replace('<span class="edition">WEB</span>','<span class="edition">PC</span>')
 .replace('<button id="install-app" type="button">Install app</button>','')
 .replace(/<p class="privacy" id="offline-status"[\s\S]*?<\/p>/,'')
 .replace(/<dialog id="install-dialog">[\s\S]*?<\/dialog>/,'')
 .replace('<a href="./#history">Android changelog</a>','<p>Windows 1.8.1: standalone installer and offline editor.</p>')
 .replace('<a href="./">About Notepad 98</a>','')
 .replaceAll('this browser','this app').replaceAll('browser draft','desktop draft')
 .replace('A working browser companion','A standalone Windows companion')
 .replace('separate from the Android app.','separate from the Android and browser apps.')
 .replace('The hosting provider may log page visits.','The editor is bundled locally and does not require internet.')
 .replace('clearing browser data removes drafts','clearing app data removes drafts');
html=html.replace('<title>', '<meta http-equiv="Content-Security-Policy" content="default-src \'self\'; script-src \'self\'; style-src \'self\' \'unsafe-inline\'; img-src \'self\'; connect-src \'none\'; object-src \'none\'; base-uri \'none\'; form-action \'none\'"><title>');
fs.writeFileSync(path.join(ui,'browser.html'),html);
