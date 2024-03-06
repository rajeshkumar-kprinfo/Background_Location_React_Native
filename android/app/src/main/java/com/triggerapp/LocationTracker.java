package com.triggerapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationTracker {
    private static final String TAG = "LocationTracker";
    private static final long MIN_TIME_INTERVAL = 1000;
    private static final float MIN_DISTANCE = 0;

    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public LocationTracker(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startTracking(final long interval, final LocationListenerCallback callback) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Log.d(TAG, "Received Location: " + location.getLatitude() + ", " +
                // location.getLongitude());
                callback.onLocationReceived(location);
                // stopTracking();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    interval,
                    MIN_DISTANCE,
                    locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public interface LocationListenerCallback {
        void onLocationReceived(Location location);
    }

    public void stopTracking() {
        try {
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

}
