import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

const directory=path.join(import.meta.dirname,'fixtures');
fs.mkdirSync(directory,{recursive:true});
const table=Uint32Array.from({length:256},(_,i)=>{let n=i;for(let j=0;j<8;j++)n=(n&1)?0xedb88320^(n>>>1):n>>>1;return n>>>0;});
function crc(bytes){let n=0xffffffff;for(const b of bytes)n=table[(n^b)&255]^(n>>>8);return(n^0xffffffff)>>>0;}
function chunk(type,data){const name=Buffer.from(type);const out=Buffer.alloc(data.length+12);out.writeUInt32BE(data.length);name.copy(out,4);data.copy(out,8);out.writeUInt32BE(crc(Buffer.concat([name,data])),out.length-4);return out;}
function png(name,width,height,alternate=false){
  const bytes=Buffer.alloc(height*(width*4+1));
  for(let y=0;y<height;y++)for(let x=0;x<width;x++){
    const i=y*(width*4+1)+1+x*4;
    let r=Math.round(255*x/(width-1)),g=Math.round(255*y/(height-1)),b=((Math.floor(x/(width/8))+Math.floor(y/(height/8)))&1)?195:30;
    if(Math.hypot(x-width*.7,y-height*.28)<height*.07)r=g=b=255;
    if(y>height*.72&&y<height*.88)r=g=b=Math.round(255*x/(width-1));
    bytes[i]=alternate?255-r:r;bytes[i+1]=alternate?b:g;bytes[i+2]=alternate?g:b;
    bytes[i+3]=x<width*.12?0:x<width*.3?128:255;
  }
  const header=Buffer.alloc(13);header.writeUInt32BE(width);header.writeUInt32BE(height,4);header[8]=8;header[9]=6;
  const gamma=Buffer.alloc(4);gamma.writeUInt32BE(45455);
  const file=path.join(directory,name);
  fs.writeFileSync(file,Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',header),chunk('sRGB',Buffer.from([0])),chunk('gAMA',gamma),chunk('IDAT',zlib.deflateSync(bytes)),chunk('IEND',Buffer.alloc(0))]));
  console.log(JSON.stringify({file,width,height,bytes:fs.statSync(file).size}));
}
png('sf7b-reference-grid.png',1280,960);
png('sf7b-reference-colors.png',1280,960,true);
png('sf7b-performance-2000.png',2000,2000);
