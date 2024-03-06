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

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(callback);
    }
}
