package com.cs160.vincent.represent;

import android.os.AsyncTask;
import android.util.Log;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Vincent on 3/10/2016.
 */
public class SunlightAPI extends AsyncTask<Double, Integer, ArrayList<CongViewEntry>> {
    private static final String SUNLIGHT_API_KEY = "a38e556778854b918c8fa74cb4759734";
    private static final String SUNLIGHT_URL = "http://congress.api.sunlightfoundation.com/locate?apikey=" + SUNLIGHT_API_KEY;

    @Override
    protected ArrayList<CongViewEntry> doInBackground(Double... input) {
        if (input.length != 3)
            throw new IllegalArgumentException("Expected 3 inputs, got " + Arrays.toString(input));

        JSONObject json = null;
        try {
            String urlStr = SUNLIGHT_URL;
            if (input[2] != -1)
                urlStr += String.format("&zip=%d", input[2].intValue());
            else
                urlStr += String.format("&latitude=%f&longitude=%f", input[0], input[1]);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            int code = conn.getResponseCode();
            Log.d("Response code", ""+code);

            json = ApiUtils.streamToJson(conn.getInputStream());
        } catch (Exception e) {
            Log.e("API", "Exception:" + e);
        }

        return toEntries(json);
    }

    @Override
    protected void onPostExecute(ArrayList<CongViewEntry> entries) {

    }

    private ArrayList<CongViewEntry> toEntries(JSONObject json) {
        if (json == null)
            return null;

        ArrayList<CongViewEntry> entries = new ArrayList<CongViewEntry>();
        try {
            JSONArray resultsArr = json.getJSONArray("results");
            for (int i=0; i<resultsArr.length(); i++) {
                JSONObject rep = resultsArr.getJSONObject(i);
                String name = String.format("%s %s", rep.getString("first_name"), rep.getString("last_name"));
                boolean isSenator = rep.getString("chamber").equals("senate");
                String email = rep.getString("oc_email");
                String party = rep.getString("party");
                String site = rep.getString("website");
                String termEnd = rep.getString("term_end");
                String bioId = rep.getString("bioguide_id");

                CongViewEntry entry = new CongViewEntry(name, party, email, site, "Loading...", termEnd, bioId, isSenator, null);
                String twitterId = rep.getString("twitter_id");
                updateTweet(twitterId, entry);

                entries.add(entry);
            }


        } catch (JSONException e) {
            Log.e("API", "Exception:" + e);
        }

        return entries;
    }

    private void updateTweet(String twitterId, final CongViewEntry entry) {
        if (twitterId == null)
            entry.setTweet("No twitter account :(");

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        statusesService.userTimeline(null, twitterId, 1, null, null, null, null, null, null, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> results) {
                Tweet tweet = results.data.get(0);
                entry.setTweet(tweet.text);
            }

            public void failure(TwitterException exception) {
                entry.setTweet("Failed to load tweet");
            }
        });
    }
}
