package com.triggerapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    public static boolean checkLocationPermission(Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                requestCode);
    }
}
