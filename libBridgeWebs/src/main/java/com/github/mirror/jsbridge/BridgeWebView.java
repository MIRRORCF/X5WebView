package com.github.mirror.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {

    private final String TAG = "BridgeWebView";

    public static final String toLoadJs = "WebViewJavascriptBridge.js";
    Map<String, CallBackFunction> responseCallbacks = new HashMap<String, CallBackFunction>();
    Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();
    BridgeHandler defaultHandler = new DefaultHandler();

    private List<Message> startupMessage = new ArrayList<Message>();

    public List<Message> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<Message> startupMessage) {
        this.startupMessage = startupMessage;
    }

    private long uniqueId = 0;

    public BridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BridgeWebView(Context context) {
        super(context);
        init();
    }

    void handlerReturnData(String url) {
        String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
        CallBackFunction f = responseCallbacks.get(functionName);
        String data = BridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            f.onCallBack(data);
            responseCallbacks.remove(functionName);
            return;
        }
    }

    @Override
    public void send(String data) {
        send(data, null);
    }

    @Override
    public void send(String data, CallBackFunction responseCallback) {
        doSend(null, data, responseCallback);
    }

    private void doSend(String handlerName, String data, CallBackFunction responseCallback) {
        Message m = new Message();
        if (!TextUtils.isEmpty(data)) {
            m.setData(data);
        }
        if (responseCallback != null) {
            String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
            responseCallbacks.put(callbackStr, responseCallback);
            m.setCallbackId(callbackStr);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.setHandlerName(handlerName);
        }
        queueMessage(m);
    }

    private void queueMessage(Message m) {
        if (startupMessage != null) {
            startupMessage.add(m);
        } else {
            dispatchMessage(m);
        }
    }

    void dispatchMessage(Message m) {
        String messageJson = m.toJson();
        //escape special characters for json string
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this.loadUrl(javascriptCommand);
        }
    }

    void flushMessageQueue() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new CallBackFunction() {

                @Override
                public void onCallBack(String data) {
                    // deserializeMessage
                    List<Message> list = null;
                    try {
                        list = Message.toArrayList(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (list == null || list.size() == 0) {
                        return;
                    }
                    for (int i = 0; i < list.size(); i++) {
                        Message m = list.get(i);
                        String responseId = m.getResponseId();
                        // 是否是response
                        if (!TextUtils.isEmpty(responseId)) {
                            CallBackFunction function = responseCallbacks.get(responseId);
                            String responseData = m.getResponseData();
                            function.onCallBack(responseData);
                            responseCallbacks.remove(responseId);
                        } else {
                            CallBackFunction responseFunction = null;
                            // if had callbackId
                            final String callbackId = m.getCallbackId();
                            if (!TextUtils.isEmpty(callbackId)) {
                                responseFunction = new CallBackFunction() {
                                    @Override
                                    public void onCallBack(String data) {
                                        Message responseMsg = new Message();
                                        responseMsg.setResponseId(callbackId);
                                        responseMsg.setResponseData(data);
                                        queueMessage(responseMsg);
                                    }
                                };
                            } else {
                                responseFunction = new CallBackFunction() {
                                    @Override
                                    public void onCallBack(String data) {
                                        // do nothing
                                    }
                                };
                            }
                            BridgeHandler handler;
                            if (!TextUtils.isEmpty(m.getHandlerName())) {
                                handler = messageHandlers.get(m.getHandlerName());
                            } else {
                                handler = defaultHandler;
                            }
                            if (handler != null) {
                                handler.handler(m.getData(), responseFunction);
                            }
                        }
                    }
                }
            });
        }
    }

    public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
        this.loadUrl(jsUrl);
        responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl), returnCallback);
    }

    /**
     * register handler,so that javascript can call it
     *
     * @param handlerName
     * @param handler
     */
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            messageHandlers.put(handlerName, handler);
        }
    }

    /**
     * call javascript registered handler
     *
     * @param handlerName
     * @param data
     * @param callBack
     */
    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack);
    }


    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     *                if js message has handler name, it will be handled by named handlers registered by native
     */
    public void setDefaultHandler(BridgeHandler handler) {
        this.defaultHandler = handler;
    }

    /**
     * 设置默认参数
     */
    private void init() {
        BridgeWebSettings.getInstance().init(this);
        this.setWebViewClient(generateBridgeWebViewClient());
        this.setWebChromeClient(generateBridgeWebChromeClient());
    }

    protected BridgeWebViewClient generateBridgeWebViewClient() {
        return new BridgeWebViewClient(this);
    }

    protected BridgeWebChromeClient generateBridgeWebChromeClient() {
        return new BridgeWebChromeClient();
    }

    /**
     * 设置webViewClient
     *
     * @param webViewClient
     */
    public void setBridgeWebViewClient(WebViewClient webViewClient) {
        this.setWebViewClient(webViewClient);
    }

    /**
     * 设置webChromeClient
     *
     * @param webChromeClient
     */
    public void setBridgeWebChromeClient(WebChromeClient webChromeClient) {
        this.setWebChromeClient(webChromeClient);
    }

    /**
     * webView进度条、标题设置
     */
    public static class Builder{

        private ProgressBar progressBar;
        private TextView textView;
        private BridgeWebView webView;
        private int indexStart;
        private int indexEnd;
        private int indexInsert;
        private String insertString;
        private List<String> whiteList;
        private String errorWhiteListUrl;
        private String errorNetWorkUrl;
        private boolean removeAllCookie = true;
        private boolean removeSessionCookie = true;

        public Builder(BridgeWebView webView){
            this.webView = webView;
        }

        /**
         * 设置进度条
         * @param progress
         * @return
         */
        public BridgeWebView.Builder setChromeProgress(ProgressBar progress){
            this.progressBar = progress;
           return this;
        }

        /**
         * 设置标题栏
         * @param title
         * @return
         */
        public BridgeWebView.Builder setChromeTitle(TextView title){
            this.textView = title;
            return this;
        }

        /**
         * 设置标题栏的开始位置
         * @param indexStart
         * @return
         */
        public BridgeWebView.Builder setTitleIndexStart(int indexStart){
            this.indexStart = indexStart;
            return this;
        }

        /**
         * 设置标题栏的结束位置
         * @param indexEnd
         * @return
         */
        public BridgeWebView.Builder setTitleIndexEnd(int indexEnd){
            this.indexEnd = indexEnd;
            return this;
        }

        /**
         * 设置标题栏的开始和结束位置
         * @param indexStart
         * @param indexEnd
         * @return
         */
        public BridgeWebView.Builder setTitleIndex(int indexStart,int indexEnd){
            this.indexStart = indexStart;
            this.indexEnd = indexEnd;
            return this;
        }

        /**
         * 在标题栏中插入字符串
         * @param indexInsert
         * @param insertString
         * @return
         */
        public BridgeWebView.Builder setTitleInSertString(int indexInsert, String insertString){
            this.indexInsert = indexInsert;
            this.insertString = insertString;
            return this;
        }

        /**
         * 设置白名单
         * @param whiteList
         * @return
         */
        public BridgeWebView.Builder setWhiteList(List<String> whiteList){
            this.whiteList = whiteList;
            return this;
        }

        /**
         * 设置白名单错误页面
         * @param errorWhiteListUrl
         * @return
         */
        public BridgeWebView.Builder setWhiteListErrorUrl(String errorWhiteListUrl){
            this.errorWhiteListUrl = errorWhiteListUrl;
            return this;
        }

        public BridgeWebView.Builder setNetWorkErrorUrl(String errorNetWorkUrl){
            this.errorNetWorkUrl = errorNetWorkUrl;
            return this;
        }

        /**
         * 设置日志开启模式
         * @param isDebug  true 开启  false 关闭
         * @return
         */
        public BridgeWebView.Builder setLogMode(boolean isDebug){
            Logs.isDebug = isDebug;
            return this;
        }

        /**
         * 设置缓存模式
         * @return
         */
        public BridgeWebView.Builder setCacheMode(int mode){
            webView.getSettings().setCacheMode(mode);
            return this;
        }

        /**
         * 设置Java消息接收器
         * @param bridgeHandler
         * @return
         */
        public BridgeWebView.Builder setHandler(BridgeHandler bridgeHandler){
            webView.setDefaultHandler(bridgeHandler);
            return this;
        }

        public void removeAllCookie(boolean removeAllCookie){
            this.removeAllCookie = removeAllCookie;
        }

        public void removeSessionCookie(boolean removeSessionCookie){
            this.removeSessionCookie = removeSessionCookie;
        }

        /**
         * 同步webview与原生之间的cookie
         * @param context
         * @param url
         * @param cookies
         */
        @SuppressWarnings("deprecation")
        public BridgeWebView.Builder setWebCookies(Context context,String url,String cookies){
            if ( !TextUtils.isEmpty(url) )
                if (!TextUtils.isEmpty(cookies) ) {

                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                            CookieSyncManager.createInstance( context);
                        }
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.setAcceptCookie( true );
                        if (removeSessionCookie){
                            cookieManager.removeSessionCookie();// 移除
                        }
                        if (removeAllCookie){
                            cookieManager.removeAllCookie();//清除所有cookie
                        }
                        String oldCookie = cookieManager.getCookie(url);
                        Logs.d("移除后旧cookie", oldCookie);
                        int con= countStr(cookies, ";");
                        String str[]=cookies.replace(" ","").split(";");
                        Logs.d("cookie个数", con + "");
                        Logs.d("cookielength", str.length + "");
                        if(con==0){
                            cookieManager.setCookie( url, cookies +";"+"Path=/");
                        }else{
                            for(int i=0;i<str.length;i++){
                                cookieManager.setCookie( url, str[i]+";"+"Path=/"); //设置cookie,如果没有特殊需求，这里只需要将session id以"key=value"形式作为cookie即可
                            }
                        }
                        CookieSyncManager.getInstance().sync();//同步cookie
                        String newCookie = cookieManager.getCookie(url);
                        Logs.d("同步后cookie", newCookie);
                    } catch (Exception e) {
                        Logs.e("Nat: webView syncCookie failed", e.toString());
                    }
                }
                return this;
        }
        /**
         * 判断str1中包含str2的个数
         * @param str1
         * @param str2
         * @return counter
         */
        public static int countStr(String str1, String str2) {
            int counter=0;
            if (str1.indexOf(str2) == -1) {
                return 0;
            }
            while(str1.indexOf(str2)!=-1){
                counter++;
                str1=str1.substring(str1.indexOf(str2)+str2.length());
            }
            return counter;
        }

        /**
         * 开始设置
         * @return
         */
        public BridgeWebView.Builder init(){
            BridgeWebChromeClient bridgeWebChromeClient = new BridgeWebChromeClient(progressBar,textView,
                    indexStart,indexEnd,
             indexInsert,insertString);

            BridgeWebViewClient bridgeWebViewClient = new BridgeWebViewClient(webView,whiteList,
                    errorWhiteListUrl,errorNetWorkUrl);

            webView.setWebChromeClient(bridgeWebChromeClient);
            webView.setWebViewClient(bridgeWebViewClient);
            return this;
        }


    }
}
