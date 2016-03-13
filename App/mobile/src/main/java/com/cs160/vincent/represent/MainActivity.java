package com.cs160.vincent.represent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "a0wR6Z1rPsPWWUKDJqp86aKny";
    private static final String TWITTER_SECRET = "geWnpjqdeZ1H8rxRb4lKQ5viKCb8in9gIImq2bT0LtA6Uy3HVF";

    public static final int BG_IMG_OPACITY = 51;

    private BroadcastReceiver receiver;

    // so resources are accessible from anywhere
    private static Context globalContext;

    public static Context globalContext() {
        return globalContext;
    }


    public static final int[] VALID_ZIP = {29445, 26508, 11373, 11364, 2720, 54115, 27292, 38654, 44663, 20854, 48167, 3054, 23834, 6106, 2860, 33428, 32174, 53045, 60047, 46037, 30043, 30084, 92806, 6473, 55406, 18052, 21014, 7047, 1821, 11561, 22701, 28655, 23223, 37122, 30281, 14094, 14580, 76522, 60062, 22801, 24401, 11354, 11757, 48236, 44004, 30542, 19401, 40356, 31404, 34786};
    public static int randZip() {
//        return 48236;       // TODO: a random zip from above that works for purposes of video
        return VALID_ZIP[ (int)(Math.random()*VALID_ZIP.length) ];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        globalContext = this;

        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        setContentView(R.layout.activity_main);

//        View root = findViewById(R.id.main_root);
//        root.getBackground().setAlpha(BG_IMG_OPACITY);

        // register callback when watch signals detailed view
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MAIN", "Init random zip!");
                openCongView(randZip());
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("random");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void searchZip(View view) {
        EditText input = (EditText) findViewById(R.id.zip_input);
        String zip = input.getText().toString();
        if (!zip.matches("\\d\\d\\d\\d\\d")) {
            TextView errText = (TextView) findViewById(R.id.zip_err);
            errText.setText(R.string.zip_err);
            return;
        }

        openCongView(Integer.parseInt(zip));
    }

    public void searchCurrLoc(View view) {
        openCongView(-1);
    }

    private void openCongView(int zip) {
        TextView errText = (TextView) findViewById(R.id.zip_err);
        errText.setText("");

        Intent intent = new Intent(this, CongressionalView.class);
        intent.putExtra(getResources().getString(R.string.zip_intent_key), zip);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }


}
