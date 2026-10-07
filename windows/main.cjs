'use strict';
const {app, BrowserWindow, session} = require('electron');
const path = require('node:path');
const fs = require('node:fs');
const smoke = process.argv.includes('--smoke-test');
if (smoke) app.setPath('userData', path.join(app.getPath('temp'), 'notepad98-smoke-'+process.pid));
app.enableSandbox();
let win;
app.whenReady().then(async () => {
 session.defaultSession.setPermissionRequestHandler((_wc,_permission,callback)=>callback(false));
 win = new BrowserWindow({width:520,height:440,minWidth:340,minHeight:280,frame:true,thickFrame:true,resizable:true,maximizable:true,fullscreenable:false,center:true,backgroundColor:'#008080',icon:path.join(__dirname,'ui/icon-512.png'),autoHideMenuBar:true,webPreferences:{nodeIntegration:false,contextIsolation:true,sandbox:true,webSecurity:true}});
 win.setMenu(null);
 win.webContents.setWindowOpenHandler(()=>({action:'deny'}));
 win.webContents.on('will-navigate',event=>event.preventDefault());
 session.defaultSession.on('will-download',(_event,item)=> {
   if(smoke) item.setSavePath(path.join(app.getPath('userData'),'smoke.txt'));
   else item.setSaveDialogOptions({title:'Save text file',defaultPath:path.join(app.getPath('documents'),path.basename(item.getFilename())),filters:[{name:'Text files',extensions:['txt']}]});
 });
 await win.loadFile(path.join(__dirname,'ui/browser.html'));
 if(smoke) {
   const timer=setTimeout(()=>{console.error('Smoke test timed out');app.exit(1);},30000);
   try {
     if(!win.isResizable()||!win.isMaximizable())throw Error('Native window resizing disabled');
     const initial=win.getSize();
     if(initial[0]!==520||initial[1]!==440)throw Error('Compact startup size incorrect');
     for(const [width,height] of [[900,700],[340,280],[520,440]]) {
       win.setSize(width,height);
       const size=win.getSize();
       if(size[0]!==width||size[1]!==height)throw Error('Native window resize failed');
       await win.webContents.executeJavaScript(`new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)))`);
       const fits=await win.webContents.executeJavaScript(`(() => {const r=document.querySelector('.window').getBoundingClientRect();return Math.abs(r.width-innerWidth)<1&&Math.abs(r.height-innerHeight)<1&&document.getElementById('editor').clientHeight>0;})()`);
       if(!fits)throw Error('Editor does not adapt to window size');
     }
     const result=await win.webContents.executeJavaScript(`(() => {
       if(document.querySelectorAll('#theme option').length!==17)throw Error('Themes missing');
       const r=document.querySelector('.window').getBoundingClientRect();
       if(r.x!==0||r.y!==0||Math.abs(r.width-innerWidth)>1||Math.abs(r.height-innerHeight)>1)throw Error('Editor does not fit window');
       if(/browser|download/i.test(document.body.innerText))throw Error('Web wording remains');
       if(typeof require!=='undefined')throw Error('Node exposed');
       const e=document.getElementById('editor');e.value='Desktop smoke test\\nNotepad 98';e.dispatchEvent(new Event('input'));
       const t=document.getElementById('theme');t.value='13';t.dispatchEvent(new Event('change'));
       return {text:e.value,theme:t.value};
     })()`);
     await win.loadFile(path.join(__dirname,'ui/browser.html'));
     const restored=await win.webContents.executeJavaScript(`({text:document.getElementById('editor').value,theme:document.getElementById('theme').value})`);
     if(JSON.stringify(result)!==JSON.stringify(restored))throw Error('Draft recovery mismatch');
     const downloaded=new Promise((resolve,reject)=>session.defaultSession.once('will-download',(_ev,item)=>item.once('done',(_e,state)=>state==='completed'?resolve():reject(Error(state)))));
     await win.webContents.executeJavaScript(`document.querySelector('[data-action=save]').click()`);
     await downloaded;
     if(fs.readFileSync(path.join(app.getPath('userData'),'smoke.txt'),'utf8')!==result.text)throw Error('Download contents mismatch');
     fs.writeFileSync(process.env.NOTEPAD98_SMOKE_OUTPUT,JSON.stringify({passed:true,checks:['compact startup','native resizing larger and smaller','responsive editor','edge-to-edge editor','desktop wording','17 themes','sandbox','editing','draft and theme recovery','text download'],version:app.getVersion()}));
     clearTimeout(timer);app.exit(0);
   }catch(error){console.error(error);clearTimeout(timer);app.exit(1);}
 }
});
app.on('window-all-closed',()=>app.quit());
