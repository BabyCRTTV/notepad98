package com.eightbittrade.notepad98;
import java.io.*;import java.nio.charset.StandardCharsets;
public class UpdateChecks {
 public static void main(String[] args)throws Exception{
  File file=File.createTempFile("notepad98-integrity",".apk");
  try{
   try(OutputStream out=new FileOutputStream(file)){UpdateManager.copy(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)),out,4);}
   if(!UpdateManager.hash(file).equals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"))throw new AssertionError("Integrity mismatch");
   boolean refused=false;try{UpdateManager.copy(new ByteArrayInputStream(new byte[5]),new ByteArrayOutputStream(),4);}catch(IOException e){refused=true;}if(!refused)throw new AssertionError("Oversized download accepted");
   try(OutputStream out=new FileOutputStream(file)){out.write(1);}
   if(UpdateManager.hash(file).equals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"))throw new AssertionError("Corruption accepted");
   System.out.println("Android update checks passed: checksum, corruption detection, download size limit");
  }finally{file.delete();}
 }
}
