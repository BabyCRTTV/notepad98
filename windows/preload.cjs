'use strict';
const {contextBridge,ipcRenderer}=require('electron');
contextBridge.exposeInMainWorld('notepadUpdates',{check:()=>ipcRenderer.invoke('notepad98:check-updates')});
