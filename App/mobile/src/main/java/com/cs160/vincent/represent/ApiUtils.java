package com.cs160.vincent.represent;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Vincent on 3/10/2016.
 */
public class ApiUtils {
    public static String streamToString(InputStream stream) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = in.readLine()) != null)
                sb.append(line + "\n");
            Log.d("streamToString()", "raw = " + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            Log.e("streamToString()", e.getMessage());
            return null;
        }
    }

    public static JSONObject streamToJson(InputStream stream) {
        try {
            return new JSONObject(streamToString(stream));
        } catch (Exception e) {
            Log.e("streamToJson()", e.getMessage());
            return null;
        }
    }

    public static InputStream connectToURL(String urlStr, String reqMethod) {
        try {
            Log.d("connectToURL()", "connect: " + urlStr);
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (reqMethod != null)
                conn.setRequestMethod(reqMethod);
            conn.setDoInput(true);
            conn.connect();

            return conn.getInputStream();
        } catch (Exception e) {
            Log.e("connectToURL()", "Exception", e);
            return null;
        }
    }
}
