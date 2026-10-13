package com.eightbittrade.notepad98;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.provider.OpenableColumns;
import android.provider.DocumentsContract;
import android.graphics.drawable.GradientDrawable;
import android.database.Cursor;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

public class MainActivity extends Activity {
    UpdateManager updater;
    static final String[] THEMES={"Windows 98","Windows XP","Windows Classic","Windows Vista","Windows 3.1","Windows XP Olive","Windows XP Silver","Macintosh Classic","XP Start Green","Macintosh Rainbow","Windows XP Zune","Windows Classic Desert","Amber Terminal","Green Terminal","White Terminal","Windows 11","Apple Liquid Glass","Macintosh Classic Dark"};
    final int GRAY=0xffc0c0c0, NAVY=0xff000080;
    EditText editor; TextView title,status; LinearLayout root, paper, titlebar, menubar; TextView closeButton; int theme=0;
    Uri file; String name="Untitled", saved=""; boolean changing=false,wrap=true;
    ArrayDeque<String> undo=new ArrayDeque<>(); Handler handler=new Handler(); Runnable recovery;
    int fontChoice=1;
    static final String[] FONT_NAMES={"Monospace 14","Monospace 16","Monospace 20","Sans serif 16","Serif 18","Serif 14","Serif 16","Serif 20","Serif 24"};
    int size=16; String font="monospace"; Runnable afterSave;
    int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    class Edge extends Drawable {
        boolean sunken; Paint p=new Paint(); Edge(boolean s){sunken=s;}
        public void draw(Canvas c){android.graphics.Rect r=getBounds();p.setColor(GRAY);c.drawRect(r,p);int a=sunken?0xff808080:0xffffffff,b=sunken?0xffffffff:0xff404040;p.setStrokeWidth(dp(1));p.setColor(a);c.drawLine(r.left,r.top,r.right,r.top,p);c.drawLine(r.left,r.top,r.left,r.bottom,p);p.setColor(b);c.drawLine(r.left,r.bottom-1,r.right,r.bottom-1,p);c.drawLine(r.right-1,r.top,r.right-1,r.bottom,p);}
        public void setAlpha(int a){} public void setColorFilter(android.graphics.ColorFilter f){} public int getOpacity(){return android.graphics.PixelFormat.OPAQUE;}
    }
    TextView label(String s,int color,int bg){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(14);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(8),dp(7),dp(8),dp(7));t.setBackgroundColor(bg);return t;}
    public void onCreate(Bundle b){super.onCreate(b);updater=new UpdateManager(this);getWindow().setStatusBarColor(NAVY);getWindow().setNavigationBarColor(GRAY);
        root=new LinearLayout(this);root.setOrientation(1);root.setBackground(new Edge(false));root.setPadding(dp(3),dp(3),dp(3),dp(3));setContentView(root);
        root.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(dp(3)+i.getSystemWindowInsetLeft(),dp(3)+i.getSystemWindowInsetTop(),dp(3)+i.getSystemWindowInsetRight(),dp(3)+i.getSystemWindowInsetBottom());return i;});
        LinearLayout bar=new LinearLayout(this);titlebar=bar;bar.setGravity(Gravity.CENTER_VERTICAL);bar.setBaselineAligned(false);bar.setBackgroundColor(NAVY);title=label("",0xffffffff,NAVY);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setIncludeFontPadding(true);title.setMinHeight(dp(40));bar.addView(title,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));TextView close=label("×",0xff000000,GRAY);closeButton=close;close.setGravity(Gravity.CENTER);close.setBackground(new Edge(false));bar.addView(close,new LinearLayout.LayoutParams(dp(34),dp(32)));close.setOnClickListener(v->confirm(()->finish()));root.addView(bar);
        LinearLayout menus=new LinearLayout(this);menubar=menus;for(String menu:new String[]{"File","Edit","Format","View","Help"}){TextView t=label(menu,0xff000000,GRAY);menus.addView(t);t.setOnClickListener(v->menu(t,menu));}root.addView(menus);
        paper=new LinearLayout(this);paper.setPadding(dp(2),dp(2),dp(2),dp(2));paper.setBackground(new Edge(true));root.addView(paper,new LinearLayout.LayoutParams(-1,0,1));
        editor=new EditText(this){protected void onSelectionChanged(int a,int b){super.onSelectionChanged(a,b);if(status!=null)update();}};editor.setGravity(Gravity.TOP|Gravity.LEFT);editor.setTextColor(0xff000000);editor.setBackgroundColor(0xffffffff);editor.setPadding(dp(8),dp(8),dp(8),dp(8));editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);editor.setTypeface(Typeface.MONOSPACE);editor.setTextSize(size);paper.addView(editor,new LinearLayout.LayoutParams(-1,-1));
        status=label("",0xff000000,GRAY);status.setBackground(new Edge(true));root.addView(status);
        recovery=()->getSharedPreferences("draft",0).edit().putString("text",editor.getText().toString()).putString("saved",saved).putString("name",name).putString("uri",file==null?"":file.toString()).apply();
        android.content.SharedPreferences prefs=getSharedPreferences("draft",0);applyFont(prefs.getInt("fontChoice",1));saved=prefs.getString("saved","");name=prefs.getString("name","Untitled");String uri=prefs.getString("uri","");if(!uri.isEmpty())file=Uri.parse(uri);editor.setText(prefs.getString("text",""));
        editor.addTextChangedListener(new TextWatcher(){String before;public void beforeTextChanged(CharSequence s,int a,int c,int n){before=s.toString();}public void onTextChanged(CharSequence s,int a,int b,int c){}public void afterTextChanged(Editable e){if(!changing){if(undo.size()>=100)undo.removeFirst();undo.addLast(before);}update();handler.removeCallbacks(recovery);handler.postDelayed(recovery,300);}});applyTheme(prefs.getInt("theme",0));update();
    }
    GradientDrawable gradient(int[] colors, boolean vertical, int radius, int stroke){
        GradientDrawable d=new GradientDrawable(vertical?GradientDrawable.Orientation.TOP_BOTTOM:GradientDrawable.Orientation.LEFT_RIGHT,colors);
        d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;
    }
    boolean isTerminal(int n){return n>=12&&n<=14;}
    int terminalColor(int n){return n==13?0xff55ff66:n==14?0xffeeeeee:0xffffb000;}
    void applyTheme(int n){
        theme=n>=0&&n<THEMES.length?n:0;n=theme;
        title.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);title.setSingleLine(true);title.setEllipsize(TextUtils.TruncateAt.END);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);closeButton.setText("×");
        titlebar.removeView(title);titlebar.removeView(closeButton);
        // Let font metrics and system font scaling determine the caption height.
        titlebar.addView(title,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
        titlebar.addView(closeButton,new LinearLayout.LayoutParams(dp(34),dp(32)));
        // Always reset editor and menu colors when leaving a dark theme.
        editor.setTextColor(Color.BLACK);editor.setBackgroundColor(Color.WHITE);editor.setHighlightColor(0xffb5d5ff);
        paper.setBackground(new Edge(true));status.setTextColor(Color.BLACK);status.setTypeface(Typeface.DEFAULT);
        for(int i=0;i<menubar.getChildCount();i++){TextView item=(TextView)menubar.getChildAt(i);item.setTextColor(Color.BLACK);item.setTypeface(Typeface.DEFAULT);}
        boolean vista=n==3;
        int chrome=vista?0xffe8edf1:n==1?0xffece9d8:GRAY;
        int blue=vista?0xff243e50:n==1?0xff245edc:n==2?0xff0a246a:NAVY;
        getWindow().setStatusBarColor(blue);getWindow().setNavigationBarColor(chrome);
        title.setBackgroundColor(Color.TRANSPARENT);
        title.setTextColor(vista?0xff102737:Color.WHITE);
        title.setTypeface(Typeface.DEFAULT,vista?Typeface.NORMAL:Typeface.BOLD);
        title.setShadowLayer(vista?dp(3):0,0,0,Color.WHITE);
        if(vista){
            root.setBackground(gradient(new int[]{0xffb8d9e8,0xff628a9f,0xffb3cbd6},true,5,0xff375466));
            titlebar.setBackground(gradient(new int[]{0xffe4f2f8,0xffb4d2e1,0xff7ca3ba,0xffaecfdf},true,4,0xffd8edf7));
            menubar.setBackground(gradient(new int[]{0xfffafcfd,0xffe8edf1},true,0,0));
            status.setBackground(gradient(new int[]{0xfff4f8fa,0xffdce5eb},true,0,0xffa6b7c2));
        }else{
            root.setBackground(new Edge(false));status.setBackground(new Edge(true));
            if(n==2)titlebar.setBackgroundColor(blue);else titlebar.setBackground(gradient(new int[]{blue,n==1?0xff5b9afa:0xff1084d0},false,0,0));
            menubar.setBackgroundColor(chrome);
        }
        for(int i=0;i<menubar.getChildCount();i++)menubar.getChildAt(i).setBackgroundColor(vista?Color.TRANSPARENT:chrome);
        if(n==1||vista){
            closeButton.setBackground(gradient(vista?new int[]{0xfff3b9af,0xffd77564,0xffa52c1b,0xffd3563f}:new int[]{0xfff59880,0xffc53b21},true,3,0xffffffff));closeButton.setTextColor(Color.WHITE);
        }else{closeButton.setBackground(new Edge(false));closeButton.setTextColor(Color.BLACK);}
        if(n==4){
            root.setBackground(gradient(new int[]{GRAY,GRAY},true,0,Color.BLACK));
            titlebar.setBackgroundColor(0xff0000aa);title.setGravity(Gravity.CENTER);
            title.setTypeface(Typeface.create("serif",Typeface.BOLD));
            closeButton.setBackground(new Edge(false));closeButton.setTextColor(Color.BLACK);
            getWindow().setStatusBarColor(0xff0000aa);
        }else if(n==5||n==6){
            boolean olive=n==5;
            int base=olive?0xffecebdc:0xffe8e8ed;
            root.setBackground(gradient(olive?new int[]{0xffb8c48f,0xff7c8d4f}:new int[]{0xffd6d8e5,0xff9498b3},true,4,olive?0xff65743d:0xff73798f));
            titlebar.setBackground(gradient(olive?new int[]{0xffd1daa9,0xffa4b579,0xff7c924c,0xffa6b87d}:new int[]{0xfff4f5fa,0xffd4d7e5,0xffa4abc4,0xffc4c9db},true,4,olive?0xffe2e9c7:Color.WHITE));
            title.setTextColor(olive?Color.WHITE:0xff20213a);
            title.setShadowLayer(dp(1),0,dp(1),olive?0xff5d7035:Color.WHITE);
            menubar.setBackgroundColor(base);for(int i=0;i<menubar.getChildCount();i++)menubar.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            status.setBackground(gradient(new int[]{base,base},true,0,olive?0xffa9b38a:0xffa8acbd));
            closeButton.setBackground(gradient(new int[]{0xfff6c5b7,0xffd97b63,0xffb6442c},true,3,Color.WHITE));closeButton.setTextColor(Color.WHITE);
            getWindow().setStatusBarColor(olive?0xff7c924c:0xff858ca7);getWindow().setNavigationBarColor(base);
        }else if(n==7){
            root.setBackground(gradient(new int[]{0xffdddddd,0xffdddddd},true,3,Color.BLACK));
            titlebar.setBackground(gradient(new int[]{Color.WHITE,Color.WHITE},true,0,Color.BLACK));
            title.setBackground(new MacTitle());title.setTextColor(Color.BLACK);title.setShadowLayer(0,0,0,0);title.setGravity(Gravity.CENTER);title.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
            titlebar.removeView(closeButton);titlebar.addView(closeButton,0,new LinearLayout.LayoutParams(dp(34),dp(32)));
            closeButton.setText("□");closeButton.setTextColor(Color.BLACK);closeButton.setBackgroundColor(Color.WHITE);
            menubar.setBackground(gradient(new int[]{Color.WHITE,Color.WHITE},true,0,Color.BLACK));for(int i=0;i<menubar.getChildCount();i++)menubar.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            status.setBackground(gradient(new int[]{0xffeeeeee,0xffeeeeee},true,0,Color.BLACK));
            getWindow().setStatusBarColor(0xff555555);getWindow().setNavigationBarColor(0xffdddddd);
        }else if(n==8){
            root.setBackground(gradient(new int[]{0xff68b653,0xff226f24,0xff399333},true,4,0xff174f1a));
            titlebar.setBackground(gradient(new int[]{0xff91ce75,0xff4aab36,0xff248526,0xff3a9d2e},true,5,0xffb8e4a2));
            title.setTextColor(Color.WHITE);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD_ITALIC);title.setShadowLayer(dp(1),0,dp(1),0xff164d18);
            menubar.setBackground(gradient(new int[]{0xffeef5e6,0xffcfe3bf},true,0,0));for(int i=0;i<menubar.getChildCount();i++)menubar.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            status.setBackground(gradient(new int[]{0xffe7f0dc,0xffc4dbb1},true,0,0xff659551));
            closeButton.setBackground(gradient(new int[]{0xffa1d882,0xff4ba53a,0xff237724},true,3,0xffc9eab8));closeButton.setTextColor(Color.WHITE);
            getWindow().setStatusBarColor(0xff248526);getWindow().setNavigationBarColor(0xffcfe3bf);
        }else if(n==9){
            int warm=0xffe8e5db;
            root.setBackground(gradient(new int[]{0xfff5f2e9,0xffd0cdc3},true,3,0xff55534e));
            titlebar.setBackground(gradient(new int[]{warm,warm},true,0,0xff55534e));
            title.setBackground(new RainbowTitle());title.setTextColor(0xff24231f);title.setShadowLayer(0,0,0,0);title.setGravity(Gravity.CENTER);title.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
            titlebar.removeView(closeButton);titlebar.addView(closeButton,0,new LinearLayout.LayoutParams(dp(34),dp(32)));
            closeButton.setText("□");closeButton.setTextColor(0xff24231f);closeButton.setBackground(gradient(new int[]{0xfffaf8ee,0xffdad6cb},true,0,0xff55534e));
            menubar.setBackground(gradient(new int[]{0xfffaf8ef,warm},true,0,0xff88857c));for(int i=0;i<menubar.getChildCount();i++)menubar.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            status.setBackground(gradient(new int[]{0xfff5f2e8,0xffdedacf},true,0,0xff88857c));
            getWindow().setStatusBarColor(0xff5a584f);getWindow().setNavigationBarColor(warm);
        }else if(n==10){
            root.setBackground(gradient(new int[]{0xff4a4a4a,0xff202020},true,4,0xff111111));
            titlebar.setBackground(gradient(new int[]{0xff606060,0xff3a3a3a,0xff202020,0xff333333},true,4,0xff777777));
            menubar.setBackground(gradient(new int[]{0xff4c4c4c,0xff303030},true,0,0));
            status.setBackground(gradient(new int[]{0xff424242,0xff292929},true,0,0xff5b5b5b));status.setTextColor(0xffeeeeee);
            for(int i=0;i<menubar.getChildCount();i++){TextView item=(TextView)menubar.getChildAt(i);item.setBackgroundColor(Color.TRANSPARENT);item.setTextColor(0xfff5f5f5);}
            closeButton.setBackground(gradient(new int[]{0xffffbb63,0xfff58b25,0xffce5913},true,3,0xffffd19a));closeButton.setTextColor(Color.WHITE);
            getWindow().setStatusBarColor(0xff252525);getWindow().setNavigationBarColor(0xff303030);
        }else if(n==11){
            root.setBackground(gradient(new int[]{0xffd9cbb0,0xffb7a689},true,0,0xff77664b));
            titlebar.setBackground(gradient(new int[]{0xff806548,0xffb19b78},false,0,0));
            menubar.setBackgroundColor(0xffd9cbb0);status.setBackground(gradient(new int[]{0xffe5d9c1,0xffd3c2a3},true,0,0xff9a8767));status.setTextColor(0xff32291e);
            for(int i=0;i<menubar.getChildCount();i++){TextView item=(TextView)menubar.getChildAt(i);item.setBackgroundColor(Color.TRANSPARENT);item.setTextColor(0xff32291e);}
            closeButton.setBackground(gradient(new int[]{0xfff0e5ce,0xffbfac8b},true,0,0xff77664b));closeButton.setTextColor(0xff30271c);
            getWindow().setStatusBarColor(0xff806548);getWindow().setNavigationBarColor(0xffd9cbb0);
        }else if(isTerminal(n)){
            int ink=terminalColor(n),dim=n==13?0xff287832:n==14?0xff777777:0xff805900;
            int top=n==13?0xff08270d:n==14?0xff242424:0xff271b00;
            int bottom=n==13?0xff031006:n==14?0xff101010:0xff100b00;
            int selection=n==13?0xff184d20:n==14?0xff505050:0xff664600;
            root.setBackground(gradient(new int[]{Color.BLACK,Color.BLACK},true,0,dim));
            titlebar.setBackground(gradient(new int[]{top,bottom},true,0,dim));title.setTextColor(ink);title.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
            menubar.setBackgroundColor(Color.BLACK);
            for(int i=0;i<menubar.getChildCount();i++){TextView item=(TextView)menubar.getChildAt(i);item.setBackgroundColor(Color.TRANSPARENT);item.setTextColor(ink);item.setTypeface(Typeface.MONOSPACE);}
            paper.setBackground(gradient(new int[]{Color.BLACK,Color.BLACK},true,0,dim));
            editor.setBackgroundColor(Color.BLACK);editor.setTextColor(ink);editor.setHighlightColor(selection);
            status.setBackground(gradient(new int[]{Color.BLACK,Color.BLACK},true,0,dim));status.setTextColor(ink);status.setTypeface(Typeface.MONOSPACE);
            closeButton.setBackground(gradient(new int[]{top,Color.BLACK},true,0,dim));closeButton.setTextColor(ink);
            getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);
        }
        if(n==15){
            root.setBackground(gradient(new int[]{0xffeaf0f8,0xfff3f3f3},true,8,0xffcbd3df));
            titlebar.setBackground(gradient(new int[]{0xffe8eef8,0xffedf1f7},false,8,0));
            title.setTextColor(0xff202020);title.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
            title.setShadowLayer(0,0,0,0);
            menubar.setBackground(gradient(new int[]{0xfff4f6fa,0xfff4f6fa},true,6,0));
            for(int i=0;i<menubar.getChildCount();i++){TextView item=(TextView)menubar.getChildAt(i);item.setBackgroundColor(Color.TRANSPARENT);item.setTextColor(0xff30343b);}
            paper.setBackground(gradient(new int[]{Color.WHITE,Color.WHITE},true,7,0xffd8dce3));
            editor.setBackground(gradient(new int[]{Color.WHITE,Color.WHITE},true,6,0));editor.setTextColor(0xff202020);editor.setHighlightColor(0xffcce4ff);
            status.setBackground(gradient(new int[]{0xfff4f6f9,0xfff4f6f9},true,6,0xffe3e7ed));status.setTextColor(0xff525965);
            closeButton.setBackground(gradient(new int[]{0xffedf1f7,0xffedf1f7},true,5,0));closeButton.setTextColor(0xff313640);
            getWindow().setStatusBarColor(0xffe8eef8);getWindow().setNavigationBarColor(0xfff3f3f3);
        }else if(n==16){
            root.setBackground(gradient(new int[]{0xffc9eaff,0xffddd5f3,0xfff2daeb,0xffd2edf0},true,12,0xffeff8ff));
            titlebar.setBackground(new GlassPanel(14));title.setTextColor(0xff26334d);title.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));title.setGravity(Gravity.CENTER);title.setShadowLayer(0,0,0,0);
            titlebar.removeView(closeButton);titlebar.addView(closeButton,0,new LinearLayout.LayoutParams(dp(34),dp(32)));
            closeButton.setBackground(new GlassPanel(16));closeButton.setTextColor(0xffa84255);
            menubar.setBackground(new GlassPanel(16));for(int i=0;i<menubar.getChildCount();i++){TextView item=(TextView)menubar.getChildAt(i);item.setBackgroundColor(Color.TRANSPARENT);item.setTextColor(0xff293c58);}
            paper.setBackground(gradient(new int[]{0xfff6fbff,0xfff8f6fc},true,10,0xdfffffff));
            editor.setBackground(gradient(new int[]{0xfffbfdff,0xfffbfaff},true,9,0));editor.setTextColor(0xff243247);editor.setHighlightColor(0xffd1e6fc);
            status.setBackground(new GlassPanel(12));status.setTextColor(0xff354660);
            getWindow().setStatusBarColor(0xffc9eaff);getWindow().setNavigationBarColor(0xffd2edf0);
        }
        if(n==17){
            int chromeDark=0xff3c3c3c,inkDark=0xffeeeeee,edgeDark=0xff878787;
            root.setBackground(gradient(new int[]{chromeDark,chromeDark},true,0,edgeDark));
            titlebar.setBackground(gradient(new int[]{chromeDark,chromeDark},true,0,edgeDark));
            title.setBackground(new MacTitle(true));title.setTextColor(inkDark);title.setShadowLayer(0,0,0,0);title.setGravity(Gravity.CENTER);title.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
            titlebar.removeView(closeButton);titlebar.addView(closeButton,0,new LinearLayout.LayoutParams(dp(34),dp(32)));
            closeButton.setText("□");closeButton.setTextColor(inkDark);closeButton.setBackground(gradient(new int[]{chromeDark,chromeDark},true,0,edgeDark));
            menubar.setBackground(gradient(new int[]{chromeDark,chromeDark},true,0,edgeDark));
            for(int i=0;i<menubar.getChildCount();i++){TextView item=(TextView)menubar.getChildAt(i);item.setBackgroundColor(Color.TRANSPARENT);item.setTextColor(inkDark);}
            paper.setBackground(gradient(new int[]{0xff191919,0xff191919},true,0,edgeDark));
            editor.setBackgroundColor(0xff191919);editor.setTextColor(inkDark);editor.setHighlightColor(0xff686868);
            status.setBackground(gradient(new int[]{chromeDark,chromeDark},true,0,edgeDark));status.setTextColor(inkDark);
            getWindow().setStatusBarColor(chromeDark);getWindow().setNavigationBarColor(chromeDark);
        }
        int systemUi=getWindow().getDecorView().getSystemUiVisibility();
        int lightBars=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility((n==15||n==16)?systemUi|lightBars:systemUi&~lightBars);
        if(Build.VERSION.SDK_INT>=29){
            int accent=isTerminal(n)?terminalColor(n):n==17?0xffeeeeee:0xff006da5;
            GradientDrawable cursor=gradient(new int[]{accent,accent},true,0,0);cursor.setSize(dp(2),dp(size+5));editor.setTextCursorDrawable(cursor);
            Drawable[] handles={editor.getTextSelectHandle(),editor.getTextSelectHandleLeft(),editor.getTextSelectHandleRight()};
            for(Drawable handle:handles)if(handle!=null)handle.mutate().setTint(accent);
        }
    }
    class GlassPanel extends Drawable {
        final int radius;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        GlassPanel(int radius){this.radius=radius;}
        public void draw(Canvas canvas){
            Rect bounds=getBounds();RectF panel=new RectF(bounds.left+.5f,bounds.top+.5f,bounds.right-.5f,bounds.bottom-.5f);float round=dp(radius);
            p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(panel.left,panel.top,panel.right,panel.bottom,new int[]{0xdfffffff,0x88eef8ff,0x70e5ddfa,0xbbeafff9},null,Shader.TileMode.CLAMP));
            canvas.drawRoundRect(panel,round,round,p);p.setShader(null);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(0xdfffffff);canvas.drawRoundRect(panel,round,round,p);
            // Fine inner highlight gives a refractive edge without obscuring text.
            panel.inset(dp(1),dp(1));p.setColor(0x507b95b6);canvas.drawRoundRect(panel,round,round,p);p.setStyle(Paint.Style.FILL);
        }
        public void setAlpha(int alpha){}public void setColorFilter(android.graphics.ColorFilter f){}public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
    }
    class MacTitle extends Drawable {
        final boolean dark;MacTitle(){this(false);}MacTitle(boolean dark){this.dark=dark;}
        Paint p=new Paint();
        public void draw(Canvas c){
            android.graphics.Rect r=getBounds();p.setColor(dark?0xff3c3c3c:Color.WHITE);c.drawRect(r,p);
            p.setColor(dark?0xff878787:Color.BLACK);p.setStrokeWidth(dp(1));
            for(int y=r.top+dp(5);y<r.bottom-dp(4);y+=dp(4))c.drawLine(r.left+dp(3),y,r.right-dp(3),y,p);
            float width=Math.min(r.width()-dp(10),title.getPaint().measureText(title.getText().toString())+dp(16));
            p.setColor(dark?0xff3c3c3c:Color.WHITE);c.drawRect(r.exactCenterX()-width/2,r.top,r.exactCenterX()+width/2,r.bottom,p);
        }
        public void setAlpha(int a){}public void setColorFilter(android.graphics.ColorFilter f){}public int getOpacity(){return android.graphics.PixelFormat.OPAQUE;}
    }
    class RainbowTitle extends Drawable {
        final int[] colors={0xff61bb46,0xfffdb827,0xfff5821f,0xffe03a3e,0xff963d97,0xff009ddc};
        Paint p=new Paint();
        public void draw(Canvas c){
            android.graphics.Rect r=getBounds();
            for(int i=0;i<colors.length;i++){p.setColor(colors[i]);c.drawRect(r.left,r.top+r.height()*i/6f,r.right,r.top+r.height()*(i+1)/6f,p);}
            float width=Math.min(r.width()-dp(28),title.getPaint().measureText(title.getText().toString())+dp(16));
            p.setColor(0xfff5f2e8);c.drawRect(r.exactCenterX()-width/2,r.top+dp(2),r.exactCenterX()+width/2,r.bottom-dp(2),p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(0xff55534e);c.drawRect(r.exactCenterX()-width/2,r.top+dp(2),r.exactCenterX()+width/2,r.bottom-dp(2),p);p.setStyle(Paint.Style.FILL);
        }
        public void setAlpha(int a){}public void setColorFilter(android.graphics.ColorFilter f){}public int getOpacity(){return android.graphics.PixelFormat.OPAQUE;}
    }
    void update(){String s=editor.getText().toString();title.setText("▤  "+name+(s.equals(saved)?"":" *")+" - Notepad");int pos=Math.max(0,editor.getSelectionStart()),line=1,col=1;for(int i=0;i<Math.min(pos,s.length());i++){if(s.charAt(i)=='\n'){line++;col=1;}else col++;}if((theme==7||theme==9||theme==17)&&title.getBackground()!=null)title.getBackground().invalidateSelf();status.setText("Ln "+line+", Col "+col+"    |    "+s.length()+" characters");}
    void menu(View anchor,String which){PopupMenu m=new PopupMenu((theme==10||theme==17||isTerminal(theme))?new ContextThemeWrapper(this,android.R.style.Theme_Material):this,anchor);String[] items;
        switch(which){case "File":items=new String[]{"New","Open…","Save","Save As…","Rename…"};break;case "Edit":items=new String[]{"Undo","Cut","Copy","Paste","Select All","Find…","Replace…"};break;case "Format":items=new String[]{(wrap?"✓ ":"")+"Word Wrap","Font…","Select Text","Select All"};break;case "View":items=new String[]{"Status Bar","Theme…"};break;default:items=new String[]{"Check for updates…","Restore Defaults","Changelog","About Notepad 98"};}
        for(String s:items){if(isTerminal(theme)){SpannableString amberLabel=new SpannableString(s);amberLabel.setSpan(new android.text.style.ForegroundColorSpan(terminalColor(theme)),0,s.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);m.getMenu().add(amberLabel);}else m.getMenu().add(s);}m.setOnMenuItemClickListener(i->{action(i.getTitle().toString());return true;});m.show();}
    void action(String s){switch(s){case "New":confirm(()->{file=null;name="Untitled";saved="";setText("");undo.clear();recovery.run();});break;
        case "Open…":confirm(()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("text/*").addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,1);});break;
        case "Rename…":rename();break;
        case "Save":save(false);break;case "Save As…":save(true);break;
        case "Undo":if(!undo.isEmpty())setText(undo.removeLast());break;
        case "Cut":editor.onTextContextMenuItem(android.R.id.cut);break;case "Copy":editor.onTextContextMenuItem(android.R.id.copy);break;case "Paste":editor.onTextContextMenuItem(android.R.id.paste);break;case "Select All":editor.requestFocus();editor.onTextContextMenuItem(android.R.id.selectAll);break;
        case "Find…":find(false);break;case "Replace…":find(true);break;
        case "Word Wrap":case "✓ Word Wrap":wrap=!wrap;editor.setHorizontallyScrolling(!wrap);break;
        case "Font…":showFonts();break;
        case "Select Text":selectText();break;
        case "Theme…":new AlertDialog.Builder(this).setTitle("Theme").setSingleChoiceItems(THEMES,theme,(d,n)->{applyTheme(n);getSharedPreferences("draft",0).edit().putInt("theme",n).apply();d.dismiss();}).setNegativeButton("Cancel",null).show();break;
        case "Check for updates…":updater.check();break;
        case "Changelog":showChangelog();break;
        case "Restore Defaults":applyTheme(0);applyFont(1);getSharedPreferences("draft",0).edit().putInt("theme",0).putInt("fontChoice",1).apply();Toast.makeText(this,"Restored Windows 98 and Monospace 16",Toast.LENGTH_SHORT).show();break;
        case "Status Bar":status.setVisibility(status.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);break;
        default:new AlertDialog.Builder(this).setTitle("Notepad 98").setMessage("Version 1.7.5\n8BitTrade\n\nA little plain-text editor with a classic Windows look.\n\nNo ads. No analytics. No accounts. Internet is used only when you check for updates.\n\nDrafts recover automatically on this device. Save creates a portable UTF-8 .txt file.\n\nAn independent app, unaffiliated with Microsoft or Apple.").setPositiveButton("OK",null).show();}}
    void showChangelog(){
        String[][] history={
            {"1.7.5", "Added Macintosh Classic Dark and Serif font sizes 14, 16, 20, and 24."},
            {"1.7.4", "Added Check for updates under Help. New permanent signing key: uninstall the older app once after saving your notes."},
            {"1.7.3", "Slightly increased the title bar height so letters like g display fully in every theme."},
            {"1.7.2", "Basic revision."},
            {"1.7.1", "Added this simplified changelog under Help."},
            {"1.7", "Added Windows 11 and Apple Liquid Glass-inspired themes."},
            {"1.6", "Added Green Terminal and White Terminal, both with black backgrounds."},
            {"1.5", "Added Zune, Desert, and Amber Terminal themes."},
            {"1.4.1", "Added Restore Defaults to reset the theme and font."},
            {"1.4", "Restored the original icon. Added a selected-font indicator, remembered font choice, and selection options in Format."},
            {"1.3.1", "Introduced an alternative spiral-notepad icon with 98 on the cover."},
            {"1.3", "Added XP Start Green and Macintosh Rainbow themes."},
            {"1.2", "Added Windows 3.1, XP Olive, XP Silver, and Macintosh Classic themes."},
            {"1.1", "Added file renaming and the Windows Vista theme."},
            {"1.0", "First release: plain-text editing, Open and Save, undo, find/replace, fonts, word wrap, draft recovery, and three classic Windows themes. No ads or accounts."}
        };
        LinearLayout entries=new LinearLayout(this);entries.setOrientation(LinearLayout.VERTICAL);entries.setPadding(dp(20),dp(4),dp(20),dp(12));
        for(String[] entry:history){
            TextView heading=new TextView(this);heading.setText("Version "+entry[0]);heading.setTextSize(16);heading.setTextColor(0xff202020);heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);heading.setPadding(0,dp(14),0,dp(4));
            if(Build.VERSION.SDK_INT>=28)heading.setAccessibilityHeading(true);entries.addView(heading);
            TextView description=new TextView(this);description.setText(entry[1]);description.setTextSize(14);description.setTextColor(0xff444444);description.setTextIsSelectable(true);description.setLineSpacing(dp(2),1);entries.addView(description);
        }
        ScrollView scroll=new ScrollView(this);scroll.addView(entries);scroll.setVerticalScrollBarEnabled(true);
        LinearLayout container=new LinearLayout(this);container.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(dp(420),getResources().getDisplayMetrics().heightPixels/2)));
        new AlertDialog.Builder(this).setTitle("Changelog").setView(container).setPositiveButton("Close",null).show();
    }
    void showFonts(){
        final int[] order={0,1,2,3,5,6,4,7,8};String[] labels=new String[order.length];int checked=0;
        for(int i=0;i<order.length;i++){labels[i]=FONT_NAMES[order[i]];if(order[i]==fontChoice)checked=i;}
        new AlertDialog.Builder(this).setTitle("Font").setSingleChoiceItems(labels,checked,(d,n)->{applyFont(order[n]);getSharedPreferences("draft",0).edit().putInt("fontChoice",fontChoice).apply();d.dismiss();}).setNegativeButton("Cancel",null).show();
    }
    void applyFont(int choice){
        fontChoice=choice>=0&&choice<FONT_NAMES.length?choice:1;
        size=new int[]{14,16,20,16,18,14,16,20,24}[fontChoice];font=new String[]{"monospace","monospace","monospace","sans-serif","serif","serif","serif","serif","serif"}[fontChoice];
        editor.setTypeface(Typeface.create(font,Typeface.NORMAL));editor.setTextSize(size);
    }
    void selectText(){
        editor.requestFocus();
        if(editor.length()==0){Toast.makeText(this,"There is no text to select yet",Toast.LENGTH_SHORT).show();return;}
        // Use the editor's native selection handles and floating editing toolbar.
        editor.performLongClick();
    }
    void setText(String s){changing=true;editor.setText(s);editor.setSelection(s.length());changing=false;update();}
    void confirm(Runnable next){if(editor.getText().toString().equals(saved)){next.run();return;}new AlertDialog.Builder(this).setTitle("Notepad").setMessage("Save changes to "+name+"?").setPositiveButton("Save",(d,w)->{afterSave=next;save(false);}).setNegativeButton("Discard",(d,w)->next.run()).setNeutralButton("Cancel",null).show();}
    void save(boolean as){if(file==null||as){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/plain").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,name.equals("Untitled")?"Untitled.txt":name);startActivityForResult(i,2);}else write(file);}
    void rename(){
        if(file==null){
            new AlertDialog.Builder(this).setTitle("Name this note").setMessage("Save this note first to give it a filename.").setPositiveButton("Save As…",(d,w)->save(true)).setNegativeButton("Cancel",null).show();return;
        }
        boolean supported=false;
        try(Cursor c=getContentResolver().query(file,new String[]{DocumentsContract.Document.COLUMN_FLAGS},null,null,null)){
            supported=c!=null&&c.moveToFirst()&&(c.getInt(0)&DocumentsContract.Document.FLAG_SUPPORTS_RENAME)!=0;
        }catch(Exception ignored){}
        if(!supported){renameUnavailable();return;}
        EditText input=new EditText(this);input.setSingleLine(true);input.setText(name);
        int dot=name.lastIndexOf('.');input.setSelection(0,dot>0?dot:name.length());
        LinearLayout box=new LinearLayout(this);box.setPadding(dp(16),dp(8),dp(16),0);box.addView(input,new LinearLayout.LayoutParams(-1,-2));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Rename file").setMessage("Rename the current file. Unsaved edits stay in the editor.").setView(box).setPositiveButton("Rename",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w->{
            String requested=input.getText().toString().trim();
            if(requested.isEmpty()||requested.equals(".")||requested.equals("..")||requested.indexOf('/')>=0||requested.indexOf('\\')>=0||requested.indexOf('\0')>=0){input.setError("Enter a filename without slashes.");return;}
            if(requested.equals(name)){dialog.dismiss();return;}
            try{
                Uri renamed=DocumentsContract.renameDocument(getContentResolver(),file,requested);
                if(renamed==null){dialog.dismiss();renameUnavailable();return;}
                // A provider can return a different URI after renaming; use it for all later saves.
                file=renamed;
                try{getContentResolver().takePersistableUriPermission(file,Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);}catch(SecurityException ignored){}
                name=displayName(file);update();recovery.run();dialog.dismiss();
                Toast.makeText(this,"Renamed to "+name,Toast.LENGTH_SHORT).show();
            }catch(Exception e){dialog.dismiss();renameUnavailable();}
        }));dialog.show();
    }
    void renameUnavailable(){
        new AlertDialog.Builder(this).setTitle("Cannot rename here").setMessage("This storage location could not rename the file. Save As can create a copy with a new name; the original file will remain.").setPositiveButton("Save As…",(d,w)->save(true)).setNegativeButton("Cancel",null).show();
    }
    void write(Uri u){try(OutputStream out=getContentResolver().openOutputStream(u,"wt")){if(out==null)throw new IOException("Cannot open file");out.write(editor.getText().toString().getBytes(StandardCharsets.UTF_8));out.flush();file=u;name=displayName(u);saved=editor.getText().toString();update();recovery.run();Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();if(afterSave!=null){Runnable r=afterSave;afterSave=null;r.run();}}catch(Exception e){afterSave=null;error(e);}}
    String displayName(Uri u){try(Cursor c=getContentResolver().query(u,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst())return c.getString(0);}catch(Exception ignored){}return "Note.txt";}
    protected void onActivityResult(int r,int result,Intent data){super.onActivityResult(r,result,data);if(r==904){updater.settingsReturned();return;}if(result!=RESULT_OK||data==null||data.getData()==null){afterSave=null;return;}Uri u=data.getData();try{getContentResolver().takePersistableUriPermission(u,data.getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION));}catch(Exception ignored){}if(r==2){write(u);return;}try(InputStream in=getContentResolver().openInputStream(u)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){out.write(buf,0,n);if(out.size()>8*1024*1024)throw new IOException("Please open a text file smaller than 8 MB.");}String text=new String(out.toByteArray(),StandardCharsets.UTF_8);if(text.startsWith("\ufeff"))text=text.substring(1);saved=text;file=u;name=displayName(u);setText(text);undo.clear();recovery.run();}catch(Exception e){error(e);}}
    void find(boolean replace){LinearLayout fields=new LinearLayout(this);fields.setOrientation(1);fields.setPadding(dp(16),0,dp(16),0);EditText query=new EditText(this);query.setHint("Find text (case sensitive)");fields.addView(query);EditText replacement=new EditText(this);if(replace){replacement.setHint("Replace with");fields.addView(replacement);}AlertDialog d=new AlertDialog.Builder(this).setTitle(replace?"Replace":"Find").setView(fields).setPositiveButton(replace?"Replace All":"Find Next",null).setNegativeButton("Close",null).create();d.setOnShowListener(v->d.getButton(-1).setOnClickListener(w->{String q=query.getText().toString(),text=editor.getText().toString();if(q.isEmpty())return;if(replace){editor.setText(text.replace(q,replacement.getText().toString()));Toast.makeText(this,"Replacement complete",0).show();}else{int n=text.indexOf(q,Math.max(0,editor.getSelectionEnd()));if(n<0)n=text.indexOf(q);if(n<0)Toast.makeText(this,"Text not found",0).show();else{editor.requestFocus();editor.setSelection(n,n+q.length());d.dismiss();}}}));d.show();}
    void error(Exception e){new AlertDialog.Builder(this).setTitle("Unable to complete action").setMessage(e.getMessage()).setPositiveButton("OK",null).show();}
    protected void onPause(){super.onPause();handler.removeCallbacks(recovery);recovery.run();}
    public void onBackPressed(){confirm(()->finish());}
}
