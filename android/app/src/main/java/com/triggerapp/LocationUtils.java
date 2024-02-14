package com.triggerapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

public class LocationUtils {

    public static void requestLocationEnable(Context context, int requestCode) {
        Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        ((Activity) context).startActivityForResult(enableLocationIntent, requestCode);
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return false;
    }
}