package mirror.example.com.myapplication;

import android.app.Application;
import android.util.Log;

import com.github.mirror.jsbridge.BridgeApplication;
import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by Administrator on 2017/7/21 0021.
 */

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        BridgeApplication.init(getApplicationContext());
    }
}
