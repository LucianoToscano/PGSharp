package com.pgs.personal;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;

/**
 * Painel de configuração local para uso particular.
 * Não altera nem lê os favoritos em execução, o login ou as licenças do PGSharp.
 * Trabalha apenas com um DAT selecionado manualmente e exporta outra cópia.
 */
@SuppressWarnings("deprecation")
public final class FavoritesOverlay {
    private static final String TAG = "pgs_personal_picker";
    private static final String PREF = "pgs_personal_favorites";
    private FavoritesOverlay() {}

    public static void attach(final Activity a) {
        // Executa apos o onCreate, evitando que Unity substitua a area de conteudo e remova o botao.
        a.getWindow().getDecorView().post(() -> {
            try {
                FrameLayout root = a.findViewById(android.R.id.content);
                if(root == null || root.findViewWithTag("pgs_personal_button") != null) return;
                Button star = new Button(a);
                star.setTag("pgs_personal_button");
                star.setText("★");
                star.setTextColor(Color.WHITE);
                star.setTextSize(20);
                star.setAllCaps(false);
                star.setPadding(0,0,0,0);
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(0xd9223231);
                bg.setCornerRadius(dp(a,18));
                bg.setStroke(dp(a,1),0x99c0d9ce);
                star.setBackground(bg);
                star.setElevation(dp(a,6));
                final int side = dp(a,46);
                root.addView(star,new FrameLayout.LayoutParams(side,side));
                SharedPreferences pref=a.getSharedPreferences(PREF,Activity.MODE_PRIVATE);
                star.post(() -> {
                    float defX=Math.max(0,root.getWidth()-side-dp(a,12));
                    star.setX(clamp(pref.getFloat("x",defX),0,Math.max(0,root.getWidth()-side)));
                    star.setY(clamp(pref.getFloat("y",dp(a,108)),0,Math.max(0,root.getHeight()-side)));
                });
                star.setOnClickListener(v -> show(a));
                star.setOnTouchListener(new View.OnTouchListener() {
                    float dx,dy,sx,sy;
                    boolean moved;
                    @Override public boolean onTouch(View v,MotionEvent e) {
                        int action=e.getActionMasked();
                        if(action==MotionEvent.ACTION_DOWN) {
                            dx=e.getRawX()-v.getX();dy=e.getRawY()-v.getY();
                            sx=e.getRawX();sy=e.getRawY();moved=false;return true;
                        }
                        if(action==MotionEvent.ACTION_MOVE) {
                            if(Math.abs(e.getRawX()-sx)+Math.abs(e.getRawY()-sy)>dp(a,8))moved=true;
                            if(moved) {
                                v.setX(clamp(e.getRawX()-dx,0,Math.max(0,root.getWidth()-v.getWidth())));
                                v.setY(clamp(e.getRawY()-dy,0,Math.max(0,root.getHeight()-v.getHeight())));
                            }
                            return true;
                        }
                        if(action==MotionEvent.ACTION_UP) {
                            if(moved)pref.edit().putFloat("x",v.getX()).putFloat("y",v.getY()).apply();
                            else v.performClick();
                            return true;
                        }
                        return action==MotionEvent.ACTION_CANCEL;
                    }
                });
            }catch(Throwable error) {
                android.util.Log.w("PGS-Pessoal","Painel indisponível",error);
            }
        });
    }

    private static float clamp(float x,float a,float b) {return Math.max(a,Math.min(x,b));}
    private static int dp(Activity a,int n) {return Math.round(n*a.getResources().getDisplayMetrics().density);}

    private static void show(final Activity a) {
        Dialog dialog=new Dialog(a);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xff12191b);
        LinearLayout header=new LinearLayout(a);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(a,12),dp(a,7),dp(a,12),dp(a,7));
        TextView label=new TextView(a);
        label.setText("★ Favoritos • Pessoal");
        label.setTextColor(Color.WHITE);
        label.setTextSize(17);
        header.addView(label,new LinearLayout.LayoutParams(0,dp(a,44),1f));
        Button close=new Button(a);
        close.setText("Fechar");
        close.setAllCaps(false);
        header.addView(close,new LinearLayout.LayoutParams(dp(a,96),dp(a,44)));
        root.addView(header);
        WebView web=new WebView(a);
        web.setBackgroundColor(0xff11191b);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setAllowFileAccessFromFileURLs(false);
        web.getSettings().setAllowUniversalAccessFromFileURLs(false);
        web.addJavascriptInterface(new LocalBridge(a),"PgsAndroid");
        web.setWebViewClient(new WebViewClient() {
            private boolean permitted(String url) {
                return url != null && url.startsWith("file:///android_asset/pgs_private/");
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,String url) {
                return !permitted(url);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request) {
                return !permitted(request.getUrl().toString());
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view,ValueCallback<Uri[]> callback,FileChooserParams params) {
                PickerFragment f=(PickerFragment)a.getFragmentManager().findFragmentByTag(TAG);
                if(f==null) {
                    f=new PickerFragment();
                    a.getFragmentManager().beginTransaction().add(f,TAG).commitAllowingStateLoss();
                    a.getFragmentManager().executePendingTransactions();
                }
                if(f.callback!=null)f.callback.onReceiveValue(null);
                f.callback=callback;
                Intent choose=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                choose.addCategory(Intent.CATEGORY_OPENABLE);
                choose.setType("*/*");
                try {
                    f.startActivityForResult(choose,PickerFragment.REQ);
                    return true;
                }catch(Exception e) {
                    f.callback=null;
                    callback.onReceiveValue(null);
                    Toast.makeText(a,"Não foi possível abrir o seletor DAT",Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
        root.addView(web,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f));
        dialog.setContentView(root);
        dialog.setOnDismissListener(d -> {
            web.removeJavascriptInterface("PgsAndroid");
            web.destroy();
        });
        close.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        Window w=dialog.getWindow();
        if(w!=null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            w.setBackgroundDrawableResource(android.R.color.transparent);
        }
        web.loadUrl("file:///android_asset/pgs_private/editor.html");
    }

    public static final class PickerFragment extends Fragment {
        static final int REQ=7910;
        ValueCallback<Uri[]> callback;
        @Override public void onActivityResult(int request,int result,Intent data) {
            super.onActivityResult(request,result,data);
            if(request!=REQ)return;
            ValueCallback<Uri[]> cb=callback;
            callback=null;
            if(cb!=null)cb.onReceiveValue(result==Activity.RESULT_OK && data!=null && data.getData()!=null
                ? new Uri[] {data.getData()}:null);
        }
    }

    public static final class LocalBridge {
        private final Activity activity;
        public LocalBridge(Activity a){activity=a;}
        @JavascriptInterface public void saveBase64(String encoded,String filename) {
            Uri target=null;
            try {
                if(Build.VERSION.SDK_INT<29)throw new IllegalStateException("Exportação integrada requer Android 10+");
                if(encoded.length()>15000000)throw new IllegalArgumentException("DAT muito grande");
                byte[] dat=Base64.decode(encoded,Base64.DEFAULT);
                if(dat.length<4 || (dat[0]&255)!=0xac || (dat[1]&255)!=0xed || (dat[2]&255)!=0 || (dat[3]&255)!=5)
                    throw new IllegalArgumentException("DAT inválido");
                String safe=filename.replaceAll("[^a-zA-Z0-9_.-]","_");
                if(!safe.endsWith(".dat"))safe+=".dat";
                ContentValues cv=new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME,safe);
                cv.put(MediaStore.Downloads.MIME_TYPE,"application/octet-stream");
                cv.put(MediaStore.Downloads.RELATIVE_PATH,"Download/PGSharp_Pessoal");
                target=activity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,cv);
                if(target==null)throw new Exception("Pasta de destino indisponível");
                try(OutputStream os=activity.getContentResolver().openOutputStream(target)) {
                    if(os==null)throw new Exception("Não foi possível abrir arquivo");
                    os.write(dat);
                }
                notifyUser("DAT exportado em Downloads/PGSharp_Pessoal");
            }catch(Throwable error) {
                if(target!=null) {
                    try {activity.getContentResolver().delete(target,null,null);}catch(Exception ignored){}
                }
                notifyUser("Falha ao salvar DAT: "+error.getMessage());
            }
        }
        private void notifyUser(String message) {
            activity.runOnUiThread(() -> Toast.makeText(activity,message,Toast.LENGTH_LONG).show());
        }
    }
}
