package com.cs160.vincent.represent;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Vincent on 3/3/2016.
 */
public class PhoneToWatchService extends Service {

    private GoogleApiClient mApiClient;

    public static byte[] bitmapToBytes(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize the googleAPIClient for message passing
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {}
                })
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.d("SENDING REPS", "count = " + CongressionalView.repsInfo.size());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mApiClient.connect();

                    int zip = CongressionalView.currZip;
                    String[] loc;
                    if (zip == -1) {
                        Location currLoc = CongressionalView.currContext.getLocation();
                        loc = GoogleGeocodingUtils.lookupLatlng(currLoc.getLatitude(), currLoc.getLongitude());
                    }
                    else {
                        loc = GoogleGeocodingUtils.lookupZip(CongressionalView.formatZip(zip));
                    }
                    Log.d("GEO", "county = " + loc[0] + " state = " + loc[1]);
                    String county = loc[0], state = loc[1];

                    ArrayList<CongViewEntry> entries = CongressionalView.repsInfo;
                    sendMessage("/update", ""+entries.size());
                    for (int i=0; i<entries.size(); i++) {
                        CongViewEntry entry = entries.get(i);
                        String fullTitle = String.format("%s %s\n%s", entry.isSenator() ? "Sen." : "Rep.", entry.getName(), entry.getParty());
                        sendMessage("/name" + i, fullTitle);
                        Log.d("PHONE", "sending name = " + fullTitle);
                        sendMessage("/img" + i, bitmapToBytes(entry.getImg()));
                    }
                    sendMessage("/location", county + ", " + state);

                    // get vote numbers
                    double obama = 0.0, romney = 0.0;
                    try {
                        InputStream in = getResources().openRawResource(R.raw.election_hash);
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder sb = new StringBuilder(in.available());
                        String line;
                        while ((line = r.readLine()) != null)
                            sb.append(line);

                        String countyCleaned = county;
                        if (county.endsWith(" County"))
                            countyCleaned = county.substring(0, county.length() - " County".length());
                        Log.d("VOTE", "Looking up county " + countyCleaned);

                        JSONObject electionHash = new JSONObject(sb.toString());
                        JSONObject countyRes = electionHash.getJSONObject(countyCleaned);
                        obama = countyRes.getDouble("obama-percentage");
                        romney = countyRes.getDouble("romney-percentage");
                        Log.d("VOTE", "Results = " + obama + " - " + romney);
                    } catch (Exception e) {
                        Log.e("VOTE", "Error reading from raw!", e);
                    }

                    sendMessage("/obama", ""+obama);
                    sendMessage("/romney", ""+romney);
                    sendMessage("/finish","");
                }
            }).start();
        }
        else
            Log.d("Phone2Watch", "Null intent!");

        return START_STICKY;
    }

    @Override //remember, all services need to implement an IBiner
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessage(final String path, final byte[] bytes) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                Log.d("SEND", "Num nodes = " + nodes.getNodes().size());
                Log.d("SEND", "Actually sending msg to path " + path);
                for(Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, bytes ).await();
                }
            }
        }).start();

        // sleep on the (non-UI) thread to try to enforce message order
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
    }

    private void sendMessage(final String path, String msg) {
        sendMessage(path, msg.getBytes());
    }

}

class GoogleGeocodingUtils {
    private static final String SERVER_KEY = "AIzaSyD8jQgIepkhksNgIlob5e0vYnhcpFAXivM";

    public static final String LAT_LNG_FILTER = "&result_type=administrative_area_level_1|administrative_area_level_2";
    public static final String ZIP_LOOKUP_URL = "https://maps.googleapis.com/maps/api/geocode/json?components=postal_code:%s&key=%s";
    public static final String LATLNG_LOOKUP_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f%s&key=%s";

    public static final String[] lookupZip(String zip) {
        JSONObject json = ApiUtils.streamToJson(ApiUtils.connectToURL(String.format(ZIP_LOOKUP_URL, zip, SERVER_KEY), null));
        return parseResults(json);
    }

    public static final String[] lookupLatlng(double lat, double lng) {
        JSONObject json = ApiUtils.streamToJson(ApiUtils.connectToURL(String.format(LATLNG_LOOKUP_URL, lat, lng, LAT_LNG_FILTER, SERVER_KEY), null));
        return parseResults(json);
    }

    private static final String[] parseResults(JSONObject json) {

        try {
            JSONArray components = json.getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

            String county = "county", state = "state", locality = "locality";
//            Log.d("GEO", "comp = " + components.toString());

            for (int i=0; i<components.length(); i++) {
                JSONObject comp = components.getJSONObject(i);
                JSONArray compTypes = comp.getJSONArray("types");
                for (int j=0; j<compTypes.length(); j++) {
                    String type = compTypes.getString(j);
                    if (type.equals("administrative_area_level_1"))
                        state = comp.getString("short_name");
                    else if (type.equals("administrative_area_level_2"))
                        county = comp.getString("long_name");
                    else if (type.equals("locality"))
                        locality = comp.getString("long_name");
                }
            }

            // if no county, perform search recursively based on city
            if (county.equals("county") && !locality.equals("locality")) {
                String localitySan = locality.replaceAll(" ", "+");
                String url = "http://maps.googleapis.com/maps/api/geocode/json?address=" + localitySan;
                JSONObject json2 = ApiUtils.streamToJson(ApiUtils.connectToURL(url, null));
                return parseResults(json2);
            }

            return new String[] {county, state};
        } catch (JSONException e) {
            Log.e("GEO", "Json parse failed!", e);
            return null;
        }
    }
}
