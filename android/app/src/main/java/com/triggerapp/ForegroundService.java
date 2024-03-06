package com.triggerapp;

import android.content.SharedPreferences;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import android.location.Location;

import androidx.core.app.NotificationCompat;

import android.content.SharedPreferences;
import com.facebook.react.bridge.ReactApplicationContext;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.PowerManager;

import android.os.BatteryManager;

import org.json.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient; // May also be needed depending on your usage
import okhttp3.Response;
import java.io.IOException; // Standard Java class

import com.google.gson.Gson;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ForegroundService extends Service {
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static long INTERVAL = 5000;
    private static String TAG = "ForegroundService";

    private Handler handler;
    private Runnable runnable;
    private SharedPreferencesModule sharedPreferencesModule;

    // Location
    private Location locationgps = null;
    private Location locationnetwork = null;

    // Geofencing
    private List<Map<String, String>> geofences_list = new ArrayList<>();

    // Pending location updates
    private List<Map<String, Object>> pendingLocationUpdates = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        ReactApplicationContext reactContext = new ReactApplicationContext(this);
        sharedPreferencesModule = new SharedPreferencesModule(reactContext);
        startRepeatingTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sales 10X")
                .setContentText("Sales 10X is running in the background.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    boolean isDayActive(int currentDay, JSONArray daysArray) {
        try {
            for (int i = 0; i < daysArray.length(); i++) {
                if (daysArray.getInt(i) == currentDay) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
        return false;
    }

    boolean isInTimeInterval(String startTime, String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            Date time1 = sdf.parse(startTime);
            Date time2 = sdf.parse(endTime);
            Date d = sdf.parse(currentTime);
            if (time1.before(d) && time2.after(d)) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
        return false;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000; // in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    public void userInsideGeofence(double userLat, double userLng) {
        try {
            String key = "key";
            String data = sharedPreferencesModule.getDataJava(key);
            if (data != null) {
                JSONObject obj = new JSONObject(data);
                JSONArray geofences = obj.getJSONArray("geofencing");

                for (int i = 0; i < geofences.length(); i++) {
                    JSONObject geofence = geofences.getJSONObject(i);
                    JSONObject locationObj = geofence.getJSONObject("location");

                    double distance = calculateDistance(userLat, userLng, locationObj.getDouble("lat"),
                            locationObj.getDouble("lng"));
                    String customerId = geofence.getString("customer_id");
                    double radius = geofence.getDouble("radius");
                    if (distance <= radius) {
                        // Check if user entered this geofence before
                        boolean enteredBefore = false;
                        for (Map<String, String> map : geofences_list) {
                            if (map.get("customer_id").equals(customerId)) {
                                // User has entered this geofence before, update exit time
                                if (map.get("exit_time") == null) {
                                    enteredBefore = true;
                                    break;
                                }
                            }
                        }
                        // If user is entering this geofence for the first time
                        if (!enteredBefore) {
                            Map<String, String> geofenceEntry = new HashMap<>();
                            geofenceEntry.put("customer_id", customerId);
                            geofenceEntry.put("entry_time", getCurrentTime());
                            geofenceEntry.put("exit_time", null);
                            geofences_list.add(geofenceEntry);
                        }
                    } else {
                        // Check if user entered this geofence before
                        for (Map<String, String> map : geofences_list) {
                            if (map.get("customer_id").equals(customerId)) {
                                // User has entered this geofence before, update exit time
                                if (map.get("exit_time") == null) {
                                    map.put("exit_time", getCurrentTime());
                                    PostGeofencingData();
                                    break;
                                }
                            }
                        }
                    }

                }
            }

            Log.d(TAG, "Geofences: " + geofences_list);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    private String getCurrentTime() {
        // Get current time in desired format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    void GetCurrentLocation(int time_interval, String userId) {
        Log.d(TAG, "Getting current location ");

        LocationTrackerGps locationTrackerGps = new LocationTrackerGps(getApplicationContext());
        locationTrackerGps.startTracking(time_interval, new LocationTrackerGps.LocationListenerCallback() {
            @Override
            public void onLocationReceived(Location location) {
                locationgps = location;
            }
        });

        LocationTracker locationTracker = new LocationTracker(getApplicationContext());
        locationTracker.startTracking(time_interval, new LocationTracker.LocationListenerCallback() {
            @Override
            public void onLocationReceived(Location location) {
                locationnetwork = location;
            }
        });

        if (locationgps != null) {
            PostData(locationgps.getLatitude(), locationgps.getLongitude(), "GPS", userId);
            userInsideGeofence(locationgps.getLatitude(), locationgps.getLongitude());
        } else if (locationnetwork != null) {
            PostData(locationnetwork.getLatitude(), locationnetwork.getLongitude(), "NETWORK", userId);
            userInsideGeofence(locationnetwork.getLatitude(), locationnetwork.getLongitude());
        } else {
            Log.d(TAG, "Location not found");
        }
    }

    void CallFunction() {
        try {
            String key = "key";
            String data = sharedPreferencesModule.getDataJava(key);
            if (data != null) {
                JSONObject obj = new JSONObject(data);

                String userId = obj.getString("user_id");

                // For Geofencing
                JSONArray geofencingArray = obj.getJSONArray("geofencing");

                // For Auto Start
                boolean isAutoStart = obj.getBoolean("is_auto_start");

                // For Time Interval
                int timeInterval = obj.getInt("time_interval");

                // For interval time
                INTERVAL = timeInterval;

                Calendar calendar = Calendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                if (isAutoStart) {

                    // For Auto Start
                    JSONArray daysArray = obj.getJSONArray("days");
                    String startTime = obj.getString("start_time");
                    String endTime = obj.getString("end_time");

                    if (isDayActive(currentDay, daysArray) && isInTimeInterval(startTime, endTime)) {
                        GetCurrentLocation(timeInterval, userId);
                    }
                } else if (!isAutoStart) {

                    // For Normal Attendance
                    boolean is_attendance_out = obj.getBoolean("is_attendance_out");
                    boolean is_attendance_in = obj.getBoolean("is_attendance_in");

                    if (!is_attendance_out && is_attendance_in) {
                        GetCurrentLocation(timeInterval, userId);
                    }
                }

                Log.d(TAG, "Data: " + obj.getString("user_id"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    void PostGeofencingData() {
        for (int i = 0; i < geofences_list.size(); i++) {
            Map<String, String> geo_fence_data = geofences_list.get(i);

            JSONObject jsonObject = new JSONObject(geo_fence_data);

            Callback callback = new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseString = response.body().string();

                        JSONObject jsonResponse = new JSONObject(responseString);
                        boolean status = jsonResponse.getBoolean("status");

                        if (status) {
                            // Remove geo_fence_data from geofences_list
                            geofences_list.remove(geo_fence_data);
                            Log.d(TAG, "Response: " + responseString);
                        } else {
                            Log.d(TAG, "Response: " + responseString);
                            Log.d(TAG, "GeoFencing data was not successfully stored");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error: error while sending geo fencing data : " + e.getMessage());
                }
            };

            try {
                String key = "key";
                String UserData = sharedPreferencesModule.getDataJava(key);

                if (UserData != null) {
                    JSONObject obj = new JSONObject(UserData);
                    String userId = obj.getString("user_id");
                    String authToken = obj.getString("auth_token");
                    String url = obj.getString("geo_fencing_url");

                    jsonObject.put("userId", userId);

                    Log.d(TAG, "jsonObject : " + jsonObject);

                    HttpRequest HttpPostRequest = new HttpRequest();
                    HttpPostRequest.sendPostRequest(url, jsonObject, authToken, callback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void PostData(double latitude, double longitude, String source, String userData) {

        BatteryManager bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        Instant currentTimeUTC = Instant.now();
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime currentTimeIST = currentTimeUTC.atZone(istZone);

        // Create a new user document with location data
        Map<String, Object> userLocation = new HashMap<>();
        double[] coordinates = { latitude, longitude };

        userLocation.put("location", coordinates);
        userLocation.put("time", currentTimeUTC.toString());
        userLocation.put("source", source);
        userLocation.put("userId", userData);
        userLocation.put("checkInDeviceBatteryPercentage", batLevel);

        pendingLocationUpdates.add(userLocation);

        for (int i = 0; i < pendingLocationUpdates.size(); i++) {
            Map<String, Object> location = pendingLocationUpdates.get(i);

            JSONObject jsonObject = new JSONObject(location);

            Callback callback = new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    pendingLocationUpdates.remove(location);
                    Log.d(TAG, "Response: " + responseString);
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error: error while sending location data : " + e.getMessage());
                }
            };

            try {
                String key = "key";
                String UserData = sharedPreferencesModule.getDataJava(key);

                if (UserData != null) {
                    JSONObject obj = new JSONObject(UserData);
                    String authToken = obj.getString("auth_token");
                    String url = obj.getString("url");
                    String organization_id = obj.getString("org_id");

                    Log.d(TAG, "jsonObject : " + jsonObject);
                    jsonObject.put("organization_id", organization_id);

                    HttpRequest HttpPostRequest = new HttpRequest();
                    HttpPostRequest.sendPostRequest(url, jsonObject, authToken, callback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    void startRepeatingTask() {
        runnable = new Runnable() {
            @Override
            public void run() {
                CallFunction();
                handler.postDelayed(this, INTERVAL);
            }
        };
        handler.postDelayed(runnable, INTERVAL);
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(runnable);
    }

}
