package com.github.mirror.jsbridge;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {

    private final String TAG = "BridgeWebViewClient";

    private BridgeWebView webView;
    private List<String> whiteList;
    private String errorWhiteListUrl = "file:///android_asset/error_not_white_list.html";
    private String errorNetWorkUrl = "file:///android_asset/error_not_network.html";

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

    public BridgeWebViewClient(BridgeWebView webView, List<String> whiteList){
        this.webView = webView;
        this.whiteList = whiteList;
    }

    public BridgeWebViewClient(BridgeWebView webView,
                               List<String> whiteList,String errorWhiteListUrl
                                ,String errorNetWorkUrl){
        this.webView = webView;
        this.whiteList = whiteList;
        if (null != errorWhiteListUrl){
            this.errorWhiteListUrl = errorWhiteListUrl;
        }
        if (null != errorNetWorkUrl){
            this.errorNetWorkUrl = errorNetWorkUrl;
        }
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue();
            return true;
        } else {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (TextUtils.equals(url,errorWhiteListUrl)){
            Logs.d(TAG,"加载白名单错误提示页面！！！");
            return;
        }else if (TextUtils.equals(url,errorNetWorkUrl)){
            Logs.d(TAG,"加载网络连接错误提示页面！！！");
            return;
        }
        if (null != whiteList && whiteList.size() > 0){
            String urls = url;
            if (url.endsWith("/")){
                 urls = url.substring(0,url.length() - 1);
            }
            if (!whiteList.contains(urls)){
                webView.stopLoading();
                if (null != errorWhiteListUrl){
                    webView.loadUrl(errorWhiteListUrl);
                }
            }
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (BridgeWebView.toLoadJs != null) {
            BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);
        }

        //
        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, final String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        webView.stopLoading();
        if (null != errorNetWorkUrl){
            webView.loadUrl(errorNetWorkUrl);
        }
    }

    @Override
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        // 忽略证书的错误继续Load页面内容
        //		handler.proceed();
        // 证书错误不继续Load页面内容
        sslErrorHandler.cancel();
    }
}