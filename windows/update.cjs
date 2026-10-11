'use strict';
const crypto=require('node:crypto');
const fs=require('node:fs');
const path=require('node:path');
const FEED='https://babycrttv.github.io/notepad98/updates.json';
const MAX=200*1024*1024;
function newer(remote,current){
 const parse=v=>{if(!/^\d+\.\d+\.\d+$/.test(v))throw Error('Invalid version');return v.split('.').map(Number);};
 const a=parse(remote),b=parse(current);for(let i=0;i<3;i++){if(a[i]!==b[i])return a[i]>b[i];}return false;
}
function entry(feed){
 const e=feed.windows;
 if(feed.schema!==1||!e||!/^\d+\.\d+\.\d+$/.test(e.version)||!/^[a-f0-9]{64}$/.test(e.sha256))throw Error('Invalid update information');
 const expected=`https://github.com/BabyCRTTV/notepad98/releases/download/windows-v${e.version}/Notepad98-Setup-${e.version}-x64.exe`;
 if(e.url!==expected)throw Error('Invalid update location');return e;
}
async function bytes(fetch,url,limit){
 const controller=new AbortController();const timer=setTimeout(()=>controller.abort(),120000);
 try{const response=await fetch(url,{cache:'no-store',signal:controller.signal});if(!response.ok)throw Error('Update server unavailable');
 let total=0;const parts=[];for await(const chunk of response.body){total+=chunk.length;if(total>limit)throw Error('Update too large');parts.push(Buffer.from(chunk));}return Buffer.concat(parts);
 }finally{clearTimeout(timer);}
}
function verified(buffer,expected){return crypto.createHash('sha256').update(buffer).digest('hex')===expected;}
function register({app,win,ipcMain,dialog,net,spawn}){
 let busy=false;
 ipcMain.handle('notepad98:check-updates',async event=>{
  if(event.sender!==win.webContents||event.senderFrame!==win.webContents.mainFrame)throw Error('Invalid caller');
  if(busy)return 'An update check is already in progress.';busy=true;
  try{
   const e=entry(JSON.parse((await bytes(net.fetch,FEED,65536)).toString('utf8')));
   if(!newer(e.version,app.getVersion())){await dialog.showMessageBox(win,{type:'info',title:'Check for updates',message:'You’re up to date.',detail:`Installed version: ${app.getVersion()}`,buttons:['OK']});return 'You’re up to date.';}
   const choice=await dialog.showMessageBox(win,{type:'info',title:'Update available',message:`Notepad 98 ${e.version} is available.`,detail:`You have ${app.getVersion()}.\n${String(e.notes||'').slice(0,1000)}\n\nDownload and install the update? Save important notes as files before restarting.`,buttons:['Download update','Later'],cancelId:1,defaultId:0});
   if(choice.response!==0)return 'Update postponed.';
   win.setProgressBar(2);
   const buffer=await bytes(net.fetch,e.url,MAX);
   if(!verified(buffer,e.sha256))throw Error('The downloaded installer failed verification. Please try again.');
   const dir=path.join(app.getPath('temp'),'notepad98-updates');fs.mkdirSync(dir,{recursive:true});
   const installer=path.join(dir,`Notepad98-Setup-${e.version}-x64.exe`);fs.writeFileSync(installer,buffer,{mode:0o600});win.setProgressBar(-1);
   const apply=await dialog.showMessageBox(win,{type:'question',title:'Install update',message:'The update is ready.',detail:'Notepad 98 will close and its installer will open. Your local draft and preferences will be kept.',buttons:['Install and restart','Later'],cancelId:1,defaultId:0});
   if(apply.response!==0)return 'Update downloaded. Check again when ready to install.';
   await win.webContents.executeJavaScript(`document.getElementById('editor').dispatchEvent(new Event('input'))`);
   await new Promise(resolve=>setTimeout(resolve,500));
   const child=spawn(installer,['/currentuser'],{detached:true,stdio:'ignore'});
   await new Promise((resolve,reject)=>{child.once('spawn',resolve);child.once('error',reject);});child.unref();app.quit();return 'Installer opened.';
  }catch(error){await dialog.showMessageBox(win,{type:'error',title:'Check for updates',message:'Could not complete the update.',detail:error.message.includes('verification')?error.message:'Check your internet connection and try again.',buttons:['OK']});return 'Update check failed.';
  }finally{busy=false;if(!win.isDestroyed())win.setProgressBar(-1);}
 });
}
module.exports={newer,entry,verified,bytes,register};
