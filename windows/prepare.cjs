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
html=html.replace(/<header class="site-header">[\s\S]*?<\/header>/,'')
 .replace(/<div class="below">[\s\S]*?<\/div>/,'')
 .replace(/<p class="privacy">[\s\S]*?<\/p>/,'')
 .replace('</main>','<div class="desktop-notice" id="notice" role="status" aria-live="polite">Ready.</div></main>')
 .replace('Browser Edition','Windows Edition')
 .replace('Try Notepad 98 in your browser. A free retro text editor with 17 themes, local draft recovery, and text-file downloads.','Notepad 98 for Windows: an offline text editor with 17 themes.')
 .replace('Save / Download .txt','Save as…').replaceAll('Rename download','Name saved copy')
 .replace('This names your next download. It does not rename a file already on your device.','Choose the suggested name for your next saved copy. Existing files are not renamed.')
 .replace(/<dialog id="about-dialog">[\s\S]*?<\/dialog>/,`<dialog id="about-dialog"><h2>Notepad 98 for Windows</h2><p>Version 1.8.2 · 8BitTrade</p><p>Pick one of 17 themes under View and start writing.</p><p>Open reads a local UTF-8 text file (up to 1 MB). Save as opens a Windows save dialog. Drafts and preferences are stored locally; save important notes as files.</p><p>Ctrl+S: Save as · Ctrl+F: Find · Ctrl+Z: Undo · Ctrl+Shift+Z: Redo.</p><p>No accounts, ads, analytics, or note uploads. Independent software, unaffiliated with Microsoft or Apple.</p><button data-close>Back to writing</button></dialog>`)
 .replace('Windows 1.8.1: standalone installer and offline editor.','Windows 1.8.2: cleaner layout and improved installer.')
 .replace(/<noscript>[\s\S]*?<\/noscript>/,'')
 .replace('</head>','<link rel="stylesheet" href="desktop.css"></head>');
fs.copyFileSync(path.join(__dirname,'desktop.css'),path.join(ui,'desktop.css'));
let js=fs.readFileSync(path.join(ui,'browser.js'),'utf8');
js=js.replaceAll('Draft saved in this browser. Download a copy to keep.','Draft saved locally. Save a file to keep a copy.')
 .replaceAll('This note has changes that have not been downloaded. Replace it? Download a copy first if you want to keep it.','This note has unsaved changes. Replace it? Save a copy first if you want to keep it.')
 .replaceAll('Download requested: ','Save dialog opened: ').replaceAll('. Check your browser’s downloads.','. Choose a location to save your file.')
 .replaceAll('Recovered your previous browser draft.','Recovered your previous draft.')
 .replaceAll('Save downloads a new copy.','Use Save as to save a copy.');
fs.writeFileSync(path.join(ui,'browser.js'),js);
fs.writeFileSync(path.join(ui,'browser.html'),html);
