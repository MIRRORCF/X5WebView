package com.github.mirror.jsbridge;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebBackForwardList;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

/**
 * Created by Administrator on 2017/7/18 0018.
 */

public class BridgeWebChromeClient extends WebChromeClient {

    private final String TAG = "BridgeWebChromeClient";

    private final String ONRELOAD = "OnReload";

    private ProgressBar progressBar;

    private TextView textView;

    private int indexStart;

    private int indexEnd;

    private int indexInsert;

    private String insertString;

    public BridgeWebChromeClient(){
    }

    /**
     * 设置进度条和标题栏
     * @param progressBar
     * @param textView
     */
    public BridgeWebChromeClient(ProgressBar progressBar,
                                 TextView textView, int indexStart, int indexEnd,
                                 int indexInsert, String insertString){
        this.progressBar = progressBar;
        this.textView = textView;
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
        this.indexInsert = indexInsert;
        this.insertString = insertString;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        StringBuffer indexTitle = new StringBuffer(title);
        if (null != title && title.length() > 0 && null != textView){
            if (checkTitleIndex(title)){
                indexTitle.replace(0,indexTitle.length(),0 == indexEnd?
                        title.substring(indexStart):title.substring(indexStart,indexEnd));
            }

            if (null != insertString && insertString.length() != 0
                    && indexInsert <= indexTitle.length() && indexInsert >= 0){
                indexTitle.insert(indexInsert,insertString);
            }else {
                Logs.e(TAG,"BridgeWebChromeClient标题插入字符串错误！！！");
            }
            textView.setText(indexTitle.toString());
        }
        super.onReceivedTitle(view, title);
    }

    /**
     * j检验Title index参数
     * @return
     */
    private boolean checkTitleIndex(String title){
        int length = title.length();
        if (indexStart < 0
                || indexEnd < 0
                || indexStart > length - 1
                || indexEnd > length
                || (indexEnd != 0 && indexEnd <= indexStart)){
            Logs.e(TAG,"BridgeWebChromeClient标题长度设置错误！！！");
            return false;
        }
        return true;
    }

    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
        super.onGeolocationPermissionsShowPrompt(origin, (GeolocationPermissionsCallback) callback);
    }




    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (null != progressBar){
            if (progressBar.getVisibility() != View.VISIBLE) {
                progressBar.setVisibility(View.VISIBLE);
            }
            progressBar.setProgress(newProgress);
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            }
        }
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public boolean onJsPrompt(WebView webView, String s, String s1, String s2, JsPromptResult jsPromptResult) {
        jsPromptResult.cancel();
        if (TextUtils.equals(s1,ONRELOAD)){
            Logs.d(TAG,"重新加载！");
            webView.goBack();
        }
        return true;
    }


}
