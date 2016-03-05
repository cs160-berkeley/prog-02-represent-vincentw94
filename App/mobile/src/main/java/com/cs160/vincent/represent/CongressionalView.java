package com.cs160.vincent.represent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CongressionalView extends AppCompatActivity {

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional_view);

//        View root = findViewById(R.id.cong_root);
//        root.getBackground().setAlpha(MainActivity.BG_IMG_OPACITY);

        // search zip code
        Intent intent = getIntent();
        int zip = intent.getIntExtra(getResources().getString(R.string.zip_intent_key), 0);
        ArrayList<CongViewEntry> results = searchRepresentatives(zip);

        TextView resultsTitle = (TextView) findViewById(R.id.cong_result_title);
        resultsTitle.setText(String.format("Representatives near %d:", zip));

        ListView resultsView = (ListView) findViewById(R.id.cong_repr);
        resultsView.setAdapter(new CongViewArrayAdapter(this, results));

        // update watch
        Intent msgIntent = new Intent(this, PhoneToWatchService.class);
        msgIntent.putExtra(getResources().getString(R.string.zip_intent_key), zip);
        startService(msgIntent);

        // register callback when watch signals detailed view
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("CONG", "Received broadcast!");
                ListView listView = (ListView) CongressionalView.this.findViewById(R.id.cong_repr);
                View entryLayout = listView.getChildAt(intent.getIntExtra("ind", -1));
                Button moreInfoButton = (Button) entryLayout.findViewById(R.id.more_info_button);
                moreInfoButton.performClick();
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
        Bitmap meg = BitmapFactory.decodeResource(getResources(), R.drawable.meg);
        Bitmap mike = BitmapFactory.decodeResource(getResources(), R.drawable.mike);
        Bitmap bryan = BitmapFactory.decodeResource(getResources(), R.drawable.bryan);
        entries.add(new CongViewEntry("Megan Jones", "D-CA", "mjones@senate.gov", "mjones.senate.gov",
                "We cannot allow ourselves to be divided by the anti-immigrant and xenophobic hysteria the Republican party has concocted.",
                true, meg));
        entries.add(new CongViewEntry("Mike Mitch", "R-CA", "mmitch@senate.gov", "mmitch.senate.gov",
                "Check out this video of some of my most memorable speeches over the past few years.",
                true, mike));
        entries.add(new CongViewEntry("Bryan Hillow", "I-CA", "bhillow@senate.gov", "bhillow.senate.gov",
                "This week, millionaires stopped paying into Social Security for 2016. That's absurd.",
                false, bryan));

        return entries;
    }

    public void backToHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
