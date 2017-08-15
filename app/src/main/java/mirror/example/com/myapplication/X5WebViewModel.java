package mirror.example.com.myapplication;

import com.apkfuns.logutils.LogUtils;
import com.github.mirror.jsbridge.BridgeHandler;
import com.github.mirror.jsbridge.CallBackFunction;
import com.github.mirror.jsbridge.Logs;
import com.tencent.smtt.sdk.WebView;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/28 0028.
 */

public class X5WebViewModel extends BaseWebModle{

    public X5WebViewModel() {
        url = "http://120.76.190.223:8988/html5/YunHaiTongProject/goods_detail.html";
        data = "ssssss";
    }

    @Override
    public void handler(String data, CallBackFunction function) {
        super.handler(data, function);
        Logs.d(TAG,data);
    }
}
