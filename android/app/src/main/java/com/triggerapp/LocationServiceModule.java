package com.triggerapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class LocationServiceModule extends ReactContextBaseJavaModule {

    private static final String TAG = "LocationServiceModule";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private final ReactApplicationContext reactContext;

    public LocationServiceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "LocationService";
    }

    // Start the location service
    @ReactMethod
    public void startLocationService(Callback successCallback, Callback errorCallback) {
        if (hasLocationPermission()) {
            Intent serviceIntent = new Intent(reactContext, LocationService.class);
            reactContext.startService(serviceIntent);
            successCallback.invoke("Location service started successfully");
        } else {
            errorCallback.invoke("Location permission not granted");
        }
    }

    // Stop the location service
    @ReactMethod
    public void stopLocationService(Callback successCallback, Callback errorCallback) {
        Intent serviceIntent = new Intent(reactContext, LocationService.class);
        reactContext.stopService(serviceIntent);
        successCallback.invoke("Location service stopped successfully");
    }

    // Check if the app has location permission
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(reactContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(reactContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request location permission
    @ReactMethod
    public void requestLocationPermission(Callback successCallback, Callback errorCallback) {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(getCurrentActivity(), new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_REQUEST_CODE);
            successCallback.invoke("Location permission request initiated");
        } else {
            successCallback.invoke("Location permission already granted");
        }
    }
}
