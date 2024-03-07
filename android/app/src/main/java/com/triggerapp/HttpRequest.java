package com.triggerapp;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;

import android.util.Log;

public class HttpRequest {

    public static void sendPostRequest(String url, JSONObject jsonData, String authToken, Callback callback)
            throws IOException {

        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON, jsonData.toString());

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);

        // Add authorization header if token is provided
        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.addHeader("Authorization", authToken);
        }

        // Add organization id header if organization id is provided
        if (jsonData.has("organization_id")) {
            try {
                String organizationId = jsonData.getString("organization_id");
                requestBuilder.addHeader("Organizationid", organizationId);
                Log.d("OrganizationId", organizationId);
            } catch (Exception e) {
                Log.d("OrganizationId", e.getMessage());
            }
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(callback);
    }
}
