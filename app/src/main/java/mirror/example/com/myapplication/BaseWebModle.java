package mirror.example.com.myapplication;

import com.apkfuns.logutils.LogUtils;
import com.github.mirror.jsbridge.BridgeHandler;
import com.github.mirror.jsbridge.CallBackFunction;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/28 0028.
 */

public class BaseWebModle implements BridgeHandler,Serializable {

    public final String TAG = this.getClass().getSimpleName();

    public String url;

    public String data;

    public BaseWebModle() {

    }

    @Override
    public void handler(String data, CallBackFunction function) {

    }
}
