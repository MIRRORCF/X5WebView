package com.github.mirror.jsbridge;

import android.util.Log;

/**
 * Created by Administrator on 2017/7/19 0019.
 */

public class Logs {

    public static boolean isDebug = true;

    public static void d(String tag,String msg){
        if (isDebug){
            Log.d(tag,msg);
        }
    }

    public static void e(String tag,String msg){
        if (isDebug){
            Log.e(tag,msg);
        }
    }
}
