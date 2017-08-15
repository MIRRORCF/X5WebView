package mirror.example.com.myapplication;

import com.github.mirror.jsbridge.BridgeHandler;
import com.github.mirror.jsbridge.CallBackFunction;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/28 0028.
 */

public class CommonWebModle implements BridgeHandler,Serializable{

    public String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        url = url;
    }

    @Override
    public void handler(String data, CallBackFunction function) {

    }
}
