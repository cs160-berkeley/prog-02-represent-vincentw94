package com.cs160.vincent.represent;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class VoteView extends Activity {

    private int ind = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_view);

        int zip = getIntent().getIntExtra("zip", -1);
        String county = resolveCountyFromZip(zip);
        int[] votes = get2012Votes(zip);

        ((TextView) findViewById(R.id.vote_loc)).setText(county);
        ((TextView) findViewById(R.id.obama_perc)).setText(String.format("%d %%", votes[0]));
        ((TextView) findViewById(R.id.romney_perc)).setText(String.format("%d %%", votes[1]));
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

    private int[] get2012Votes(int zip) {
        // TODO: use API
        int obama = 40 + (int) (Math.random()*20);
        return new int[] {obama, 100-obama};
    }
}

