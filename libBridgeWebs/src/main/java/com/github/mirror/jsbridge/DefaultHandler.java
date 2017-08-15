package com.github.mirror.jsbridge;

import com.apkfuns.logutils.LogUtils;

public class DefaultHandler implements BridgeHandler{

	String TAG = "DefaultHandler";
	
	@Override
	public void handler(String data, CallBackFunction function) {
		Logs.d(TAG,data);
		if(function != null){
			function.onCallBack("DefaultHandler response data");
		}
	}

}
