'use strict';
(() => {
const $ = id => document.getElementById(id);
const editor=$('editor'), key='notepad98.browser.v1';
const themes=[
['Windows 98','#008080','#c0c0c0','#111','linear-gradient(90deg,#000080,#1084d0)','#fff'],
['Windows XP','#3a6ea5','#ece9d8','#111','linear-gradient(#4598fc,#0555d8,#328afa)','#fff'],
['Windows Classic','#3a6e70','#c0c0c0','#111','#0a246a','#fff'],
['Windows Vista','#375466','#e8edf1','#102737','linear-gradient(#e4f2f8,#7ca3ba,#aecfdf)','#102737'],
['Windows 3.1','#008080','#c0c0c0','#111','#0000aa','#fff'],
['Windows XP Olive','#65743d','#ecebdc','#253015','linear-gradient(#d1daa9,#7c924c,#a6b87d)','#233211'],
['Windows XP Silver','#73798f','#e8e8ed','#20213a','linear-gradient(#f4f5fa,#a4abc4,#c4c9db)','#20213a'],
['Macintosh Classic','#777','#eee','#111','repeating-linear-gradient(#fff 0 3px,#333 3px 4px)','#111'],
['XP Start Green','#226f24','#dceccd','#183d15','linear-gradient(#91ce75,#248526,#3a9d2e)','#fff'],
['Macintosh Rainbow','#5a584f','#e8e5db','#24231f','linear-gradient(#61bb46 0 16.6%,#fdb827 16.6% 33.3%,#f5821f 33.3% 50%,#e03a3e 50% 66.6%,#963d97 66.6% 83.3%,#009ddc 83.3%)','#24231f'],
['Windows XP Zune','#191919','#333','#f5f5f5','linear-gradient(#606060,#202020,#333)','#fff'],
['Windows Classic Desert','#77664b','#d9cbb0','#32291e','linear-gradient(90deg,#806548,#b19b78)','#fff'],
['Amber Terminal','#100b00','#000','#ffb000','linear-gradient(#271b00,#100b00)','#ffb000'],
['Green Terminal','#031006','#000','#55ff66','linear-gradient(#08270d,#031006)','#55ff66'],
['White Terminal','#101010','#000','#eee','linear-gradient(#242424,#101010)','#eee'],
['Windows 11','#53677d','#f3f3f3','#30343b','linear-gradient(90deg,#e8eef8,#edf1f7)','#202020'],
['Apple Liquid Glass','#778dac','#e8e6f4','#293c58','linear-gradient(120deg,#effaffdd,#cce9f9bb,#e5ddfacc,#eafff9dd)','#26334d']
];
let name='Untitled.txt',saved='',theme=0,font=1,history=[],at=0,lastEdit=0,recoveryTimer,storageOK=true;
const fonts=[['monospace',14],['monospace',16],['monospace',20],['Arial, sans-serif',16],['Georgia, serif',18]];
const tell=s=>$('notice').textContent=s;
const dirty=()=>editor.value!==saved;
const cleanName=s=>{s=s.trim().replace(/[\\/:*?"<>|\x00-\x1f]/g,'_').slice(0,116);return !s?'Untitled.txt':/\.txt$/i.test(s)?s:s+'.txt';};
function update(){
 $('caption').textContent=(dirty()?'* ':'')+name+' - Notepad';
 document.title=(dirty()?'* ':'')+name+' · Notepad 98';
 const before=editor.value.slice(0,editor.selectionStart),lines=before.split('\n');
 $('position').textContent='Ln '+lines.length+', Col '+(lines[lines.length-1].length+1);
 $('count').textContent=editor.value.length.toLocaleString()+' characters';
 $('undo').disabled=at<=0;$('redo').disabled=at>=history.length-1;
}
function persist(){
 clearTimeout(recoveryTimer);
 try{localStorage.setItem(key,JSON.stringify({text:editor.value,saved,name,theme,font,wrap:$('wrap').checked,status:$('status-toggle').checked}));storageOK=true;tell('Draft saved in this browser. Download a copy to keep.');}
 catch(e){storageOK=false;tell('Draft storage unavailable or full. Download your note to keep it.');}
}
function record(force=false){
 const now=Date.now(),item={text:editor.value,start:editor.selectionStart,end:editor.selectionEnd};
 history=history.slice(0,at+1);
 if(!force&&now-lastEdit<600&&at>0)history[at]=item;
 else{history.push(item);at++;}
 // Keep history bounded, including when working with larger files.
 while(history.length>60||(history.length>2&&history.reduce((n,x)=>n+x.text.length,0)>5000000)){history.shift();at--;}
 lastEdit=now;update();clearTimeout(recoveryTimer);recoveryTimer=setTimeout(persist,250);
}
function resetHistory(){history=[{text:editor.value,start:0,end:0}];at=0;lastEdit=0;update();}
function undo(step){const next=at+step;if(next<0||next>=history.length)return;at=next;const item=history[at];editor.value=item.text;editor.focus();editor.setSelectionRange(item.start,item.end);lastEdit=0;update();persist();}
function applyTheme(){
 const t=themes[theme];document.body.dataset.theme=theme;
 const values={'--desk':t[1],'--chrome':t[2],'--ink':t[3],'--title':t[4],'--title-ink':t[5],'--paper':theme>=12&&theme<=14?'#000':'#fff','--text':theme>=12&&theme<=14?t[3]:'#111','--radius':theme>=15?'10px':theme===1||theme===3||theme===5||theme===6||theme===8?'5px':'0px','--selection':theme===12?'#664600':theme===13?'#184d20':theme===14?'#505050':'#b5d5ff'};
 for(const [p,v] of Object.entries(values))document.documentElement.style.setProperty(p,v);
 $('theme').value=theme;
}
function applyFont(){editor.style.fontFamily=fonts[font][0];editor.style.fontSize=fonts[font][1]+'px';$('font').value=font;}
function applyWrap(){editor.wrap=$('wrap').checked?'soft':'off';}
function confirmReplace(){return !dirty()||confirm('This note has changes that have not been downloaded. Replace it? Download a copy first if you want to keep it.');}
function closeMenus(){document.querySelectorAll('.menus details[open]').forEach(d=>d.open=false);}
const actions={
 new(){if(!confirmReplace())return;editor.value='';name='Untitled.txt';saved='';resetHistory();persist();editor.focus();},
 open(){$('open-file').click();},
 save(){const blob=new Blob([editor.value],{type:'text/plain;charset=utf-8'}),url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download=name;document.body.append(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),30000);saved=editor.value;update();persist();tell('Download requested: '+name+'. Check your browser’s downloads.');},
 rename(){$('filename').value=name;$('rename-dialog').showModal();$('filename').select();},
 undo(){undo(-1);},redo(){undo(1);},select(){editor.focus();editor.select();update();},
 find(){$('find-message').textContent='';$('find-dialog').showModal();$('find-text').focus();},
 defaults(){theme=0;font=1;applyTheme();applyFont();persist();tell('Windows 98 theme and Monospace 16 restored.');},
 about(){$('about-dialog').showModal();}
};
themes.forEach((t,i)=>{const o=document.createElement('option');o.value=i;o.textContent=t[0];$('theme').append(o);});
try{const d=JSON.parse(localStorage.getItem(key)||'null');if(d&&typeof d.text==='string'){
 editor.value=d.text;saved=typeof d.saved==='string'?d.saved:'';name=cleanName(typeof d.name==='string'?d.name:'Untitled.txt');
 theme=Number.isInteger(d.theme)&&themes[d.theme]?d.theme:0;font=Number.isInteger(d.font)&&fonts[d.font]?d.font:1;
 $('wrap').checked=d.wrap!==false;$('status-toggle').checked=d.status!==false;
 tell('Recovered your previous browser draft.');
}}catch(e){storageOK=false;tell('Draft recovery unavailable. Download your note to keep it.');}
applyTheme();applyFont();applyWrap();$('statusbar').hidden=!$('status-toggle').checked;resetHistory();
document.querySelectorAll('[data-action]').forEach(b=>b.addEventListener('click',()=>{closeMenus();actions[b.dataset.action]();}));
document.querySelectorAll('[data-close]').forEach(b=>b.addEventListener('click',()=>b.closest('dialog').close()));
document.querySelectorAll('.menus details').forEach(d=>d.addEventListener('toggle',()=>{if(d.open)document.querySelectorAll('.menus details').forEach(other=>{if(other!==d)other.open=false;});}));
document.addEventListener('click',e=>{if(!e.target.closest('.menus'))closeMenus();});
document.addEventListener('keydown',e=>{if(e.key==='Escape')closeMenus();if(document.querySelector('dialog[open]'))return;if(e.ctrlKey||e.metaKey){const k=e.key.toLowerCase();if(k==='s'){e.preventDefault();actions.save();}if(k==='f'){e.preventDefault();actions.find();}if(k==='z'&&document.activeElement===editor){e.preventDefault();undo(e.shiftKey?1:-1);}if(k==='y'&&document.activeElement===editor){e.preventDefault();undo(1);}}});
editor.addEventListener('input',()=>record());['click','keyup','select'].forEach(e=>editor.addEventListener(e,update));
$('theme').addEventListener('change',()=>{theme=Number($('theme').value);applyTheme();persist();});
$('font').addEventListener('change',()=>{font=Number($('font').value);applyFont();persist();});
$('wrap').addEventListener('change',()=>{applyWrap();persist();});
$('status-toggle').addEventListener('change',()=>{$('statusbar').hidden=!$('status-toggle').checked;persist();});
$('rename-form').addEventListener('submit',e=>{e.preventDefault();name=cleanName($('filename').value);$('rename-dialog').close();update();persist();});
$('open-file').addEventListener('change',async e=>{const f=e.target.files[0];e.target.value='';if(!f)return;if(f.size>1024*1024){tell('Please choose a text file smaller than 1 MB.');return;}if(!confirmReplace())return;
 try{const text=await f.text();if(text.includes('\u0000')){tell('That looks like a binary file. Choose a UTF-8 text file.');return;}editor.value=text.replace(/^\uFEFF/,'').replace(/\r\n?/g,'\n');name=cleanName(f.name);saved=editor.value;resetHistory();persist();editor.focus();tell('Opened '+name+' locally. Save downloads a new copy.');}catch(e){tell('Could not read that file. Your current note is unchanged.');}});
$('find-next').addEventListener('click',()=>{const q=$('find-text').value;if(!q){$('find-message').textContent='Enter something to find.';return;}let i=editor.value.indexOf(q,editor.selectionEnd);if(i<0)i=editor.value.indexOf(q);if(i<0){$('find-message').textContent='No match found.';return;}$('find-dialog').close();editor.focus();editor.setSelectionRange(i,i+q.length);update();tell('Match selected. Open Find again to find the next match.');});
$('replace-all').addEventListener('click',()=>{const q=$('find-text').value;if(!q){$('find-message').textContent='Enter something to find.';return;}const parts=editor.value.split(q),n=parts.length-1;if(n){editor.value=parts.join($('replace-text').value);record(true);persist();}$('find-message').textContent=n+' replacement'+(n===1?'':'s')+'.';});
window.addEventListener('pagehide',persist);
window.addEventListener('beforeunload',e=>{if(dirty()&&!storageOK){e.preventDefault();e.returnValue='';}});
})();
