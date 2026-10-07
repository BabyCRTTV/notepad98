package shop.eightbittrade.fleet98;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.net.Uri;
import android.view.View;
import java.io.*;
public class MainActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff008080);getWindow().setNavigationBarColor(0xff008080);web=new WebView(this);setContentView(web);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);web.setWebViewClient(new WebViewClient(){
 @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return !"app.fleet98.local".equals(r.getUrl().getHost());}
 @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){Uri u=r.getUrl();if(!"app.fleet98.local".equals(u.getHost()))return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));String p=u.getPath();if(p==null||p.equals("/"))p="/index.html";if(p.contains(".."))return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));String type=p.endsWith(".js")?"application/javascript":p.endsWith(".css")?"text/css":p.endsWith(".svg")?"image/svg+xml":p.endsWith(".webmanifest")?"application/manifest+json":"text/html";try{return new WebResourceResponse(type,"UTF-8",getAssets().open("web"+p));}catch(IOException e){return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));}}
 });web.loadUrl("https://app.fleet98.local/index.html");}
 @Override protected void onPause(){super.onPause();web.onPause();}
 @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}
