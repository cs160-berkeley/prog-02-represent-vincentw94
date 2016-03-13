package com.cs160.vincent.represent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.core.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;
import com.twitter.sdk.android.core.models.User;

public class CongressionalView extends AppCompatActivity implements ConnectionCallbacks {
    public static final String SUNLIGHT_API_KEY = "a38e556778854b918c8fa74cb4759734";
    public static final String SUNLIGHT_REP_URL = "http://congress.api.sunlightfoundation.com/legislators/locate?apikey=" + SUNLIGHT_API_KEY;
    public static final String SUNLIGHT_COMM_URL = "http://congress.api.sunlightfoundation.com/committees?apikey=" + SUNLIGHT_API_KEY;
    public static final String SUNLIGHT_BILLS_URL = "http://congress.api.sunlightfoundation.com/bills?apikey=" + SUNLIGHT_API_KEY;

    private BroadcastReceiver receiver;
    private ListView repsList;
    private GoogleApiClient client;
    private Location lastLocation;

    public static ArrayList<CongViewEntry> repsInfo = null;
    public static CongressionalView currContext;
    public static int currZip;
    public static int picsLoaded;

    public Location getLocation() {
        return lastLocation;
    }

    public static void updateListView() {
        currContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListAdapter adapter = currContext.repsList.getAdapter();
                if (adapter != null)
                    ((ArrayAdapter)adapter).notifyDataSetChanged();
            }
        });
    }

    public static String formatZip(int zip) {
        if (zip == -1)
            return "you";

        String s = "" + zip;
        while (s.length() < 5)
            s = "0" + s;
        return s;
    }

    @Override
    protected void onStart() {
        Log.d("CONG", "API Client connecting...");
        client.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        client.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("CONG", "API Client connected!");
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        if (lastLocation == null) {
            Log.e("GEO", "Getting location failed! Setting manually...");
            lastLocation = new Location("");
            lastLocation.setLatitude(37.87);
            lastLocation.setLongitude(-122.26);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional_view);

        currContext = this;
        picsLoaded = 0;

        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

//        View root = findViewById(R.id.cong_root);
//        root.getBackground().setAlpha(MainActivity.BG_IMG_OPACITY);

        // search zip code
        Intent intent = getIntent();
        currZip = intent.getIntExtra(getResources().getString(R.string.zip_intent_key), -1);

        TextView resultsTitle = (TextView) findViewById(R.id.cong_result_title);
        resultsTitle.setText(String.format("Representatives near %s:", formatZip(currZip)));

        repsList = (ListView) findViewById(R.id.cong_repr);

        new GetRepInfoTask().execute(currZip);

        // register callback when watch signals detailed view
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("CONG", "Received broadcast!");

                int ind = intent.getIntExtra("ind", -1);
//                ListView listView = (ListView) CongressionalView.this.findViewById(R.id.cong_repr);
//
//                Log.d("CONG", "ind = " + ind + ", total = " + listView.getChildCount() + ", actual total = " + listView.getAdapter().getCount());
//
//                View entryLayout = listView.getChildAt(ind);
//                Button moreInfoButton = (Button) entryLayout.findViewById(R.id.more_info_button);
//                moreInfoButton.performClick();

                Intent toDetIntent = new Intent(CongressionalView.currContext, DetailedView.class);
                toDetIntent.putExtra("ind", ind);
                startActivity(toDetIntent);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("to_detailed");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private ArrayList<CongViewEntry> searchRepresentatives(int zip) {
        // TODO: use API instead
        ArrayList<CongViewEntry> entries = new ArrayList<CongViewEntry>(3);
//        Bitmap meg = BitmapFactory.decodeResource(getResources(), R.drawable.meg);
//        Bitmap mike = BitmapFactory.decodeResource(getResources(), R.drawable.mike);
//        Bitmap bryan = BitmapFactory.decodeResource(getResources(), R.drawable.bryan);
//        entries.add(new CongViewEntry("Megan Jones", "D-CA", "mjones@senate.gov", "mjones.senate.gov",
//                "We cannot allow ourselves to be divided by the anti-immigrant and xenophobic hysteria the Republican party has concocted.",
//                true, meg));
//        entries.add(new CongViewEntry("Mike Mitch", "R-CA", "mmitch@senate.gov", "mmitch.senate.gov",
//                "Check out this video of some of my most memorable speeches over the past few years.",
//                true, mike));
//        entries.add(new CongViewEntry("Bryan Hillow", "I-CA", "bhillow@senate.gov", "bhillow.senate.gov",
//                "This week, millionaires stopped paying into Social Security for 2016. That's absurd.",
//                false, bryan));

        /*
        // TODO: Use a more specific parent
        final ViewGroup parentView = (ViewGroup) getWindow().getDecorView().getRootView();
        // TODO: Base this Tweet ID on some data from elsewhere in your app
        long tweetId = 631879971628183552L;
        TweetUtils.loadTweet(tweetId, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                TweetView tweetView = new TweetView(CongressionalView.this, result.data);
                parentView.addView(tweetView);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load Tweet failure", exception);
            }
        });
        */

        return entries;
    }

    public void backToHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /*******************************************************
     *******************************************************
     *******************************************************
     *******************************************************
     *******************************************************
     *******************************************************
     *******************************************************
     *******************************************************
     */
    private class GetRepInfoTask extends AsyncTask<Integer, Integer, ArrayList<CongViewEntry>> {
        @Override
        protected ArrayList<CongViewEntry> doInBackground(Integer... input) {
            Log.d("REP-INFO", "doInBackground()");
            if (input.length != 1)
                throw new IllegalArgumentException("Expected 1 inputs, got " + Arrays.toString(input));

            String urlStr = SUNLIGHT_REP_URL;
            if (input[0] != -1)
                urlStr += String.format("&zip=%d", input[0]);
            else {
                while (lastLocation == null) {
                    try {
                        Log.d("LOC", "location not ready yet!");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}
                }
                urlStr += String.format("&latitude=%f&longitude=%f", lastLocation.getLatitude(), lastLocation.getLongitude());
            }

            InputStream in = ApiUtils.connectToURL(urlStr, "GET");
            JSONObject json = ApiUtils.streamToJson(in);

            return toEntries(json);
        }

        @Override
        protected void onPostExecute(ArrayList<CongViewEntry> entries) {
            repsInfo = entries;
            repsList.setAdapter(new CongViewArrayAdapter(CongressionalView.this, entries));
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
                    String party = String.format("%s-%s", rep.getString("party"), rep.getString("state"));
                    String site = rep.getString("website");
                    String termEnd = rep.getString("term_end");
                    String bioId = rep.getString("bioguide_id");

                    CongViewEntry entry = new CongViewEntry(name, party, email, site,
                                                            "Loading...", termEnd, bioId, isSenator, null);

                    entries.add(entry);

                    String twitterId = rep.getString("twitter_id");
                    updateTweet(twitterId, entry);
                    setCommsAndBills(entry);
                }
            } catch (JSONException e) {
                Log.e("API", "Exception:" + e);
            }

            return entries;
        }

        private void updateTweet(final String twitterId, final CongViewEntry entry) {
            if (twitterId == null)
                entry.setTweet("No twitter account :(");

            TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
                @Override
                public void success(Result<AppSession> loginResult) {
                    Log.d("TWITTER", "Login success!");

                    TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                    StatusesService statusesService = twitterApiClient.getStatusesService();
                    statusesService.userTimeline(null, twitterId, 1, null, null, false, false, false, false,
                            new Callback<List<Tweet>>() {
                                @Override
                                public void success(Result<List<Tweet>> results) {
                                    Tweet tweet = results.data.get(0);
                                    entry.setTweet(tweet.text);
                                    Log.d("Loaded tweet:", tweet.text);

                                    CongressionalView.updateListView();
                                }

                                public void failure(TwitterException exception) {
                                    Log.e("Tweet", "Failed to load tweet!");
                                    entry.setTweet("Failed to load tweet");

                                    CongressionalView.updateListView();
                                }
                            });

                    ExtendedTwitterApiClient picClient = new ExtendedTwitterApiClient(loginResult.data);
                    picClient.getUsersService().show(null, twitterId, new Callback<User>() {
                        @Override
                        public void success(Result<User> result) {
                            String userImgUrl = result.data.profileImageUrl;
                            Log.d("Twit Pic", "Got img for " + entry.getName() + " @ " + userImgUrl);

                            new LoadTwitterPicTask(entry).execute(userImgUrl);
                        }

                        @Override
                        public void failure(TwitterException e) {
                            Log.e("Twit Pic", "Failed to load users/show");
                        }
                    });
                }

                @Override
                public void failure(TwitterException e) {
                    Log.e("TWITTER", "Login failed!");
                }
            });
        }
    }

    private void setCommsAndBills(final CongViewEntry entry) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<String> comms = new ArrayList<String>(), bills = new ArrayList<String>();

                try {
                    String commsUrl = CongressionalView.SUNLIGHT_COMM_URL + "&member_ids=" + entry.getBioId();
                    JSONObject commsJson = ApiUtils.streamToJson(ApiUtils.connectToURL(commsUrl, "GET"));
                    JSONArray commsArr = commsJson.getJSONArray("results");
                    for (int i=0; i<commsArr.length(); i++)
                        comms.add(commsArr.getJSONObject(i).getString("name"));
                    Log.d("COMMS", "Set comms for " + entry.getName());
                    entry.setCommittees(comms);

                } catch (Exception e) {
                    Log.e("COMMS", "JSON", e);
                }

                try {
                    String billsUrl = CongressionalView.SUNLIGHT_BILLS_URL + "&sponsor_id=" + entry.getBioId();
                    JSONObject billsJson = ApiUtils.streamToJson(ApiUtils.connectToURL(billsUrl, "GET"));
                    JSONArray billsArr = billsJson.getJSONArray("results");
                    for (int i=0; i<Math.min(10, billsArr.length()); i++) {
                        JSONObject billObj = billsArr.getJSONObject(i);
                        String billTitle = billObj.getString("short_title");
                        String billDate = billObj.getString("introduced_on");
                        if (!billTitle.equals("null"))
                            bills.add(String.format("%s (%s)", billTitle, billDate));
                    }
                    Log.d("BILLS", "Set bills for " + entry.getName());
                    entry.setBills(bills);

                } catch (Exception e) {
                    Log.e("BILLS", "JSON", e);
                }

                return null;
            }
        }.execute();
    }
}

class ExtendedTwitterApiClient extends TwitterApiClient {
    public ExtendedTwitterApiClient(Session session) {
        super(session);
    }

    /**
     * Provide FriendsService with ids
     */
    public UsersService getUsersService() {
        return getService(UsersService.class);
    }

    public interface UsersService {
        @GET("/1.1/users/show.json")
        void show(@Query("user_id") Long id, @Query("screen_name") String screenName, Callback<User> cb);
    }
}

class LoadTwitterPicTask extends AsyncTask<String, Void, Void> {

    private CongViewEntry entry;

    public LoadTwitterPicTask(CongViewEntry entry) {
        this.entry = entry;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length != 1)
            throw new IllegalArgumentException("Expected one URL string, got" + Arrays.toString(params));

        String url = params[0];
        url = url.replace("_normal", "");

        Log.d("Twit Pic", "Starting to load " + url);
        InputStream input = ApiUtils.connectToURL(url, null);
        Bitmap img = BitmapFactory.decodeStream(input);
        entry.setImg(img);
        Log.d("Twit Pic", "Finished loading " + entry.getName() + " @ " + url);
        CongressionalView.updateListView();

        synchronized (CongressionalView.class) {
            CongressionalView.picsLoaded++;
            Log.d("PIC SYNC", "total = " + CongressionalView.picsLoaded);

            if (CongressionalView.picsLoaded == CongressionalView.repsInfo.size()) {
                // update watch
                Intent msgIntent = new Intent(CongressionalView.currContext, PhoneToWatchService.class);
                Log.d("CONG", "Starting phone2watch");
                CongressionalView.currContext.startService(msgIntent);
            }
        }

        return null;
    }
}