/* Fleet98 rules and fair, observation-only AI. Shared by all platforms. */
(function(root){
'use strict';
const TYPES=[['carrier','Carrier',5],['battleship','Battleship',4],['cruiser','Cruiser',3],['submarine','Submarine',3],['destroyer','Destroyer',2]];
const cells=(x,y,len,vertical)=>Array.from({length:len},(_,i)=>(y+(vertical?i:0))*10+x+(vertical?0:i));
function canPlace(ships,x,y,len,vertical,ignore){return x>=0&&y>=0&&x+(vertical?1:len)<=10&&y+(vertical?len:1)<=10&&!cells(x,y,len,vertical).some(c=>ships.some(s=>s.id!==ignore&&s.cells.includes(c)));}
function fleet(rng=Math.random){let ships=[];for(const [id,name,len]of TYPES){let placed=false;for(let i=0;i<1000&&!placed;i++){let x=Math.floor(rng()*10),y=Math.floor(rng()*10),v=rng()<.5;if(canPlace(ships,x,y,len,v)){ships.push({id,name,len,x,y,vertical:v,cells:cells(x,y,len,v)});placed=true;}}if(!placed)return fleet(rng);}return ships;}
function fire(ships,shots,c){if(!Number.isInteger(c)||c<0||c>99||shots[c])return null;let s=ships.find(s=>s.cells.includes(c));shots[c]=s?'hit':'miss';let sunk=null;if(s&&s.cells.every(c=>shots[c])){sunk=s.id;s.cells.forEach(c=>shots[c]='sunk');}return {cell:c,hit:!!s,sunk,won:ships.every(s=>s.cells.every(c=>shots[c]))};}
function chooseShot(shots,lengths,difficulty='normal',rng=Math.random){let open=Array.from({length:100},(_,i)=>i).filter(i=>!shots[i]);if(!open.length)return null;const pick=a=>a[Math.floor(rng()*a.length)];if(difficulty==='easy')return pick(open);let hits=Object.keys(shots).filter(c=>shots[c]==='hit').map(Number);let near=open.filter(c=>hits.some(h=>(Math.abs(c-h)===10)||(Math.floor(c/10)===Math.floor(h/10)&&Math.abs(c-h)===1)));
if(difficulty==='normal')return pick(near.length?near:open.filter(c=>(c%10+Math.floor(c/10))%2===0).length?open.filter(c=>(c%10+Math.floor(c/10))%2===0):open);
let scores=Array(100).fill(0);for(let len of lengths)for(let v of [false,true])for(let y=0;y<10;y++)for(let x=0;x<10;x++){if(x+(v?1:len)>10||y+(v?len:1)>10)continue;let cs=cells(x,y,len,v);if(cs.some(c=>shots[c]==='miss'||shots[c]==='sunk'))continue;let n=cs.filter(c=>shots[c]==='hit').length;let weight=hits.length?(n?Math.pow(15,n):0):1;cs.forEach(c=>{if(!shots[c])scores[c]+=weight;});}let best=Math.max(...open.map(c=>scores[c]));return pick(open.filter(c=>scores[c]===best));}
const api={TYPES,cells,canPlace,fleet,fire,chooseShot};if(typeof module!=='undefined')module.exports=api;else root.FleetEngine=api;
})(typeof window==='undefined'?globalThis:window);
