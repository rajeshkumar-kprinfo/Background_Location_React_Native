package com.triggerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class SharedPreferencesModule extends ReactContextBaseJavaModule {
    private static final String MODULE_NAME = "SharedPreferencesModule";
    private static final String PREF_NAME = "MyAppPrefs";

    public SharedPreferencesModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void saveData(String key, String value, Promise promise) {
        SharedPreferences sharedPreferences = getReactApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
        promise.resolve("Data stored successfully"); // Resolve the Promise with a success message
    }

    @ReactMethod
    public String saveData(String key, String value) {
        SharedPreferences sharedPreferences = getReactApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();

        return "Data stored successfully";
    }

    @ReactMethod
    public void getData(String key, Promise promise) {
        SharedPreferences sharedPreferences = getReactApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        String defaultValue = "{}";
        String value = sharedPreferences.getString(key, defaultValue);
        promise.resolve(value);
    }

    @ReactMethod
    public String getDataJava(String key) {
        SharedPreferences sharedPreferences = getReactApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        String defaultValue = null;
        String value = sharedPreferences.getString(key, defaultValue);
        return value;
    }

    @ReactMethod
    public void deleteData(String key, Promise promise) {
        SharedPreferences sharedPreferences = getReactApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
        promise.resolve("Data deleted successfully");
    }
}
