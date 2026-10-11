'use strict';
document.getElementById('check-updates').addEventListener('click',async event=>{
 const button=event.currentTarget;button.disabled=true;
 document.querySelectorAll('.menus details').forEach(menu=>menu.open=false);
 document.getElementById('notice').textContent='Checking for updates…';
 try{document.getElementById('notice').textContent=await window.notepadUpdates.check();}
 catch{document.getElementById('notice').textContent='Could not check for updates. Try again.';}
 finally{button.disabled=false;}
});
