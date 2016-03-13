package com.cs160.vincent.represent;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class VoteView extends Activity {

    private int ind = 0;

    public static double obama, romney;
    public static String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_view);

//        int zip = getIntent().getIntExtra("zip", -1);
//        String county = resolveCountyFromZip(zip);

        ((TextView) findViewById(R.id.vote_loc)).setText(location);
        ((TextView) findViewById(R.id.obama_perc)).setText(String.format("%.1f %%", obama));
        ((TextView) findViewById(R.id.romney_perc)).setText(String.format("%.1f %%", romney));
    }

    private String resolveCountyFromZip(int zip) {
        // TODO: use API
        int rand = (int)(Math.random()*5);
        switch(rand) {
            case 0: return "Alameda County, CA";
            case 1: return "St Louis County, MO";
            case 2: return "Nashville County, TN";
            case 3: return "Albany County, NY";
            default: return "Boulder County, CO";
        }
    }
}

