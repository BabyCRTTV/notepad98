package com.eightbittrade.notepad98;
import android.content.*;import android.database.*;import android.net.Uri;import android.os.*;import java.io.*;
public class UpdateProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 static File file(Context context){return new File(context.getCacheDir(),"notepad98-update.apk");}
 private void check(Uri uri){if(!uri.equals(Uri.parse("content://com.eightbittrade.notepad98.updates/update.apk")))throw new SecurityException("Invalid update URI");}
 public String getType(Uri uri){check(uri);return "application/vnd.android.package-archive";}
 public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{check(uri);if(!"r".equals(mode))throw new SecurityException("Read only");return ParcelFileDescriptor.open(file(getContext()),ParcelFileDescriptor.MODE_READ_ONLY);}
 public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){check(uri);MatrixCursor c=new MatrixCursor(new String[]{"_display_name","_size"});c.addRow(new Object[]{"Notepad98-update.apk",file(getContext()).length()});return c;}
 public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException();}
 public int delete(Uri uri,String selection,String[] args){throw new UnsupportedOperationException();}
 public int update(Uri uri,ContentValues values,String selection,String[] args){throw new UnsupportedOperationException();}
}
