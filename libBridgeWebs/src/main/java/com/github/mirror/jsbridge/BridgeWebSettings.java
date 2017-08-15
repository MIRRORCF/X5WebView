package com.github.mirror.jsbridge;

import android.os.Build;
import android.view.View;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

/**
 * WebViewSetting设置
 *
 * Created by Administrator on 2017/7/18 0018.
 */

public class BridgeWebSettings {

    private static BridgeWebSettings bridgeWebSettings;


    public static BridgeWebSettings getInstance(){
       if (null == bridgeWebSettings){
           bridgeWebSettings = new BridgeWebSettings();
       }
       return bridgeWebSettings;
    }

    @SuppressWarnings({ "deprecation", "rawtypes" })
    public void init(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled( true );
        webView.removeJavascriptInterface("accessibility");
        webView.removeJavascriptInterface("accessibilityTraversal");
        webView.removeJavascriptInterface("searchBoxJavaBridge_");
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webSettings.setAllowFileAccess( true );
        webSettings.setSupportZoom( false );
        webSettings.setBuiltInZoomControls( false );
        webSettings.setRenderPriority( WebSettings.RenderPriority.HIGH );
        webSettings.setAppCacheMaxSize(Long.MAX_VALUE );
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE );
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled( true );
        webSettings.setDomStorageEnabled( true );
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSavePassword(false);
        //定位
        webSettings.setGeolocationEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        //解决webView 在4.4系统上面onDraw Failed 问题
        webView.setLayerType( View.LAYER_TYPE_SOFTWARE, null );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}
