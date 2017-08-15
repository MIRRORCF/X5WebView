package mirror.example.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.github.mirror.jsbridge.BridgeHandler;
import com.github.mirror.jsbridge.BridgeWebView;
import com.github.mirror.jsbridge.CallBackFunction;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements OnClickListener {

    private final String TAG = "MainActivity";
    BridgeWebView webView;

    Button button;

    BaseWebModle baseWebModle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        webView = (BridgeWebView) findViewById(R.id.webView);

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(this);

        findViewById(R.id.btn_back).setOnClickListener(this);

        findViewById(R.id.btn_send).setOnClickListener(this);

        findViewById(R.id.btn_call).setOnClickListener(this);

        List<String> list = new ArrayList<>();
        list.add("https://www.baidu.com");
        list.add("file:///android_asset/demo.html");

        Intent intent = getIntent();
        baseWebModle = (BaseWebModle) intent.getSerializableExtra("X5WebViewModel");

        new BridgeWebView.Builder(webView).
                setChromeProgress(progressBar)//设置进度条
                .setChromeTitle(button)//设置标题
				.setTitleIndex(0,8)//截取标题字段
				.setTitleInSertString(8,"...")//像标题插入字段
//                .setWhiteList(list)//设置白名单
                .setLogMode(true)//正式发布时请设置为false
                .setHandler(baseWebModle)//设置JS消息接收器 或者 setHandler(new MyBridgeHandler)
                .setCacheMode(WebSettings.LOAD_NO_CACHE)//设置wenViews缓存模式
//                .setNetWorkErrorUrl("file:///android_asset/error_404.html")//设置网络加载错误页面
//                .setWhiteListErrorUrl("file:///android_asset/error_white.html")//设置白名单错误页面
//                .setWebCookies(this,"file:///android_asset/demo.html","123456")//设置cookie
                .init();//初始化  必须

//        webView.loadUrl("https://www.baidu.com");
//        webView.loadUrl("file:///android_asset/demo.html");
        webView.loadUrl(baseWebModle.url);
//        webView.send(baseWebModle.data);

        //注册消息，JS端需手动获取
        //注：JS端获取该参数时 该方法将被执行
//        webView.registerHandler("submitFromWeb", new BridgeHandler() {
//
//            @Override
//            public void handler(String data, CallBackFunction function) {
//                function.onCallBack(x5WebViewModel.data);
//
//            }
//
//        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                if (webView.canGoBack()){
                    webView.goBack();
                }else {
                    finish();
                }
                break;
            case R.id.btn_send:
                //传递消息，JS端将自动获取
                webView.send("Hello JS");
                break;
            case R.id.btn_call:
                //接受消息，需主动调用
                webView.callHandler("functionInJs", "data from Java", new CallBackFunction() {

                    @Override
                    public void onCallBack(String data) {
                        // TODO Auto-generated method stub
                        Toast.makeText(MainActivity.this, "data :" + data, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "reponse data from js " + data);
                    }

                });

                break;
            default:
                break;
        }
    }

    //用于接收JS端传入的消息，自动调用
    public class MyBridgeHandler implements BridgeHandler {

        @Override
        public void handler(String data, CallBackFunction function) {
            Toast.makeText(MainActivity.this,data, Toast.LENGTH_SHORT).show();
            //JS回调
            if (null != function){
                function.onCallBack("This is my handler.");
            }
        }
    }

}
