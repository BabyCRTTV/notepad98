package com.eightbittrade.notepad98;
import android.app.*;import android.content.*;import android.content.pm.*;import android.net.Uri;import android.os.Build;import android.provider.Settings;
import java.io.*;import java.net.*;import java.nio.charset.StandardCharsets;import java.security.*;import java.util.*;import org.json.*;
final class UpdateManager {
 static final String FEED="https://babycrttv.github.io/notepad98/updates.json";
 final MainActivity activity;boolean busy;File pending;
 UpdateManager(MainActivity activity){this.activity=activity;}
 void ui(Runnable task){activity.runOnUiThread(()->{if(!activity.isFinishing()&&!activity.isDestroyed())task.run();});}
 void message(String title,String text){ui(()->new AlertDialog.Builder(activity).setTitle(title).setMessage(text).setPositiveButton("OK",null).show());}
 static InputStream stream(String address)throws Exception{
  URL url=new URL(address);for(int redirects=0;redirects<6;redirects++){
   if(!"https".equals(url.getProtocol()))throw new IOException("Secure connection required");
   HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setInstanceFollowRedirects(false);c.setUseCaches(false);int code=c.getResponseCode();
   if(code>=300&&code<400){String next=c.getHeaderField("Location");c.disconnect();if(next==null)throw new IOException("Missing redirect");url=new URL(url,next);continue;}
   if(code!=200){c.disconnect();throw new IOException("Update service unavailable");}
   InputStream in=c.getInputStream();return new FilterInputStream(in){public void close()throws IOException{try{super.close();}finally{c.disconnect();}}};
  }throw new IOException("Too many redirects");
 }
 static void copy(InputStream in,OutputStream out,long limit)throws Exception{byte[] b=new byte[16384];long size=0;int n;while((n=in.read(b))!=-1){size+=n;if(size>limit)throw new IOException("Update is too large");out.write(b,0,n);}}
 static String hash(File file)throws Exception{MessageDigest d=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(file)){byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)d.update(b,0,n);}StringBuilder s=new StringBuilder();for(byte b:d.digest())s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
 void check(){
  if(busy)return;busy=true;
  AlertDialog progress=new AlertDialog.Builder(activity).setTitle("Check for updates").setMessage("Checking the current version…").setCancelable(false).show();
  new Thread(()->{try{
   ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream in=stream(FEED)){copy(in,b,65536);}
   JSONObject feed=new JSONObject(new String(b.toByteArray(),StandardCharsets.UTF_8));if(feed.getInt("schema")!=1)throw new IOException("Invalid update feed");
   JSONObject e=feed.getJSONObject("android");int code=e.getInt("versionCode");String version=e.getString("version"),url=e.getString("url"),sha=e.getString("sha256");
   if(!version.matches("\\d+\\.\\d+\\.\\d+")||!sha.matches("[a-f0-9]{64}")||!url.equals("https://babycrttv.github.io/notepad98/Notepad98-v"+version+".apk"))throw new IOException("Invalid update information");
   PackageInfo current=activity.getPackageManager().getPackageInfo(activity.getPackageName(),0);
   ui(()->{progress.dismiss();busy=false;if(code<=current.versionCode){message("Check for updates","You’re up to date.\nInstalled version: "+current.versionName);return;}
    new AlertDialog.Builder(activity).setTitle("Update available").setMessage("Notepad 98 "+version+" is available.\nYou have "+current.versionName+".\n\n"+e.optString("notes","")+"\n\nDownload and apply the update? Android will ask you to confirm installation.").setNegativeButton("Later",null).setPositiveButton("Download update",(d,w)->download(url,sha,code)).show();});
  }catch(Exception error){ui(()->{progress.dismiss();busy=false;message("Check for updates","Could not check for updates. Check your internet connection and try again.");});}} ,"Notepad98-update-check").start();
 }
 void download(String url,String sha,int versionCode){
  if(busy)return;busy=true;File file=UpdateProvider.file(activity);pending=null;
  AlertDialog progress=new AlertDialog.Builder(activity).setTitle("Downloading update").setMessage("Downloading and verifying Notepad 98…").setCancelable(false).show();
  new Thread(()->{try{
   try(InputStream in=stream(url);OutputStream out=new FileOutputStream(file)){copy(in,out,16*1024*1024);}
   if(!hash(file).equals(sha))throw new IOException("Download verification failed");
   PackageManager pm=activity.getPackageManager();int flags=Build.VERSION.SDK_INT>=28?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES;
   PackageInfo installed=pm.getPackageInfo(activity.getPackageName(),flags),downloaded=pm.getPackageArchiveInfo(file.getPath(),flags);
   if(downloaded==null||!activity.getPackageName().equals(downloaded.packageName)||downloaded.versionCode!=versionCode||downloaded.versionCode<=installed.versionCode)throw new IOException("Invalid update package");
   android.content.pm.Signature[] oldSign=Build.VERSION.SDK_INT>=28?installed.signingInfo.getApkContentsSigners():installed.signatures;
   android.content.pm.Signature[] newSign=Build.VERSION.SDK_INT>=28?downloaded.signingInfo.getApkContentsSigners():downloaded.signatures;
   if(!new HashSet<android.content.pm.Signature>(Arrays.asList(oldSign)).equals(new HashSet<android.content.pm.Signature>(Arrays.asList(newSign))))throw new IOException("This update uses a different signing key");
   ui(()->{progress.dismiss();busy=false;pending=file;new AlertDialog.Builder(activity).setTitle("Update ready").setMessage("Apply the update now? Your local draft and preferences will be kept.").setNegativeButton("Later",(d,w)->pending=null).setPositiveButton("Apply update",(d,w)->install()).show();});
  }catch(Exception error){file.delete();ui(()->{progress.dismiss();busy=false;message("Update not installed",error.getMessage()==null?"Could not download the update. Try again.":error.getMessage());});}} ,"Notepad98-update-download").start();
 }
 void install(){
  if(pending==null||!pending.exists())return;
  if(!activity.getPackageManager().canRequestPackageInstalls()){
   new AlertDialog.Builder(activity).setTitle("Allow app updates").setMessage("Android requires permission for Notepad 98 to install its update. Enable Allow from this source, then return here.").setNegativeButton("Cancel",(d,w)->pending=null).setPositiveButton("Open settings",(d,w)->{try{activity.startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+activity.getPackageName())),904);}catch(Exception e){pending=null;message("Update not installed","Android’s installation settings could not be opened.");}}).show();return;
  }
  activity.handler.removeCallbacks(activity.recovery);activity.recovery.run();
  try{Intent intent=new Intent(Intent.ACTION_INSTALL_PACKAGE).setDataAndType(Uri.parse("content://com.eightbittrade.notepad98.updates/update.apk"),"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);activity.startActivity(intent);pending=null;}
  catch(Exception e){pending=null;message("Update not installed","Android’s installer could not be opened. You can download the latest APK from the Notepad 98 website.");}
 }
 void settingsReturned(){if(pending!=null){if(activity.getPackageManager().canRequestPackageInstalls())install();else{pending=null;message("Update not installed","Installation permission was not enabled. You can check for updates again later.");}}}
}
