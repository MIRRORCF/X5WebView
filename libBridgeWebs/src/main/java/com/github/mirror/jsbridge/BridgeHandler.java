package com.github.mirror.jsbridge;

import com.tencent.smtt.sdk.WebView;

public interface BridgeHandler {
	
	void handler(String data, CallBackFunction function);
}
