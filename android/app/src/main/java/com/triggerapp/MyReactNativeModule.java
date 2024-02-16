
package com.triggerapp;

import android.util.Log;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public class MyReactNativeModule {

    private static final String TAG = "MyReactNativeModule";

    private ReactInstanceManager mReactInstanceManager;

    public MyReactNativeModule(ReactInstanceManager reactInstanceManager) {
        mReactInstanceManager = reactInstanceManager;
    }

    public void callJavaScriptFunction(String functionName, String args) {
        Log.d(TAG, "callJavaScriptFunction: " + functionName + " " + args);
        // ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
        // if (reactContext != null) {
        // CatalystInstance catalystInstance = reactContext.getCatalystInstance();
        // if (catalystInstance != null) {
        // WritableMap params = Arguments.createMap();
        // params.putString("functionName", functionName);
        // params.putString("args", args.toString()); // You may need to serialize this
        // better depending on your
        // // use case
        // // catalystInstance.callFunction(
        // // "handleLocationUpdate", // Assuming the JavaScript function is named
        // // handleLocationUpdate
        // // args);
        // } else {
        // // CatalystInstance is not available
        // }
        // } else {
        // // ReactContext is not available
        // }
    }
}
