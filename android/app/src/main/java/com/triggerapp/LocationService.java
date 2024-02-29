package com.triggerapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.io.IOException;

import okhttp3.*;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final int LOCATION_UPDATE_INTERVAL = 10 * 1000; // 10 seconds
    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private LocationManager mLocationManager;
    private LocationManager nLocationManager;

    private PowerManager.WakeLock wakeLock;

    private List<Map<String, Object>> pendingLocationUpdates = new ArrayList<>();

    // HTTP POST Request Data
    private String userData;
    private String AuthToken;
    private String URL;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationService onCreate()");
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        nLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "LocationService::WakeLock");
        wakeLock.acquire(60 * 1000);

        startLocationUpdates();
        startForeground(NOTIFICATION_ID, createNotification());

    }

    private Notification createNotification() {
        // Create a notification channel if Android version is Oreo or higher
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        // Create a notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Location updates are being tracked")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return notificationBuilder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            userData = intent.getStringExtra("UserData");
            AuthToken = intent.getStringExtra("AuthToken");
            URL = intent.getStringExtra("URL");
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLocationUpdates() {
        Log.d(TAG, "Location updates started");
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    0,
                    mLocationListener);
            nLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    0,
                    nLocationListener);

        } catch (SecurityException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    private final LocationListener nLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {

            Instant currentTimeUTC = Instant.now();
            ZoneId istZone = ZoneId.of("Asia/Kolkata");
            ZonedDateTime currentTimeIST = currentTimeUTC.atZone(istZone);

            // Create a new user document with location data
            Map<String, Object> userLocation = new HashMap<>();
            userLocation.put("latitude", location.getLatitude());
            userLocation.put("longitude", location.getLongitude());
            userLocation.put("time", currentTimeUTC.toString());
            userLocation.put("Source", "Network");
            userLocation.put("UserId", userData);

            Log.d(TAG, "Network Location updated: " + location.getLatitude() + ", " + location.getLongitude() + ", "
                    + currentTimeUTC);

            pendingLocationUpdates.add(userLocation);

            // Upload pending location updates to Firebase
            uploadPendingLocationUpdates();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "Location status changed: " + status);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.d(TAG, "Provider enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Log.d(TAG, "Provider disabled: " + provider);
        }
    };

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.d(TAG, "GPS Location updated: " + location.getLatitude() + ", " + location.getLongitude());

            Instant currentTimeUTC = Instant.now();
            ZoneId istZone = ZoneId.of("Asia/Kolkata");
            ZonedDateTime currentTimeIST = currentTimeUTC.atZone(istZone);

            // Create a new user document with location data
            Map<String, Object> userLocation = new HashMap<>();
            userLocation.put("latitude", location.getLatitude());
            userLocation.put("longitude", location.getLongitude());
            userLocation.put("time", currentTimeUTC.toString());
            userLocation.put("Source", "GPS");
            userLocation.put("UserId", userData);

            pendingLocationUpdates.add(userLocation);

            // Upload pending location updates to Firebase
            uploadPendingLocationUpdates();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "Location status changed: " + status);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.d(TAG, "Provider enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Log.d(TAG, "Provider disabled: " + provider);
        }
    };

    private void uploadPendingLocationUpdates() {
        Iterator<Map<String, Object>> iterator = pendingLocationUpdates.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> userLocation = iterator.next();
            handleLocationDetails(userLocation);
            iterator.remove();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }

        if (nLocationManager != null) {
            nLocationManager.removeUpdates(nLocationListener);
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void handleLocationDetails(Map<String, Object> locationDetails) {
        Log.d(TAG, "Location details: " + locationDetails + URL + AuthToken);
    }

}
