package com.cs160.vincent.represent;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class DetailedView extends AppCompatActivity {

    private static final int IMG_WIDTH = 400, IMG_HEIGHT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

//        View root = findViewById(R.id.det_root);
//        root.getBackground().setAlpha(MainActivity.BG_IMG_OPACITY);

        Intent intent = getIntent();
        Resources res = getResources();
        String name = intent.getStringExtra(res.getString(R.string.rep_name_key));
        String party = intent.getStringExtra(res.getString(R.string.rep_party_key));
        boolean isSenator = intent.getBooleanExtra(res.getString(R.string.rep_type_key), false);
        int term = intent.getIntExtra(res.getString(R.string.rep_term_key), -1);

        TextView profInfo = (TextView) findViewById(R.id.det_profile);
        profInfo.setText(profileText(name, party, isSenator, term));

        List<String> committees = getCommittees(name), bills = getBills(name);
        String html = detailedHtml(committees, bills);
        TextView comBillInfo = (TextView) findViewById(R.id.det_info);
        comBillInfo.setText(Html.fromHtml(html));

        // get filename for picture from intent
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(openFileInput(res.getString(R.string.rep_img_fn)));
            bitmap = Bitmap.createScaledBitmap(bitmap, IMG_WIDTH, IMG_HEIGHT, true);
            ImageView imgView = (ImageView) findViewById(R.id.detailed_img);
            imgView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String profileText(String name, String party, boolean isSenator, int term) {
        return String.format(
                "%s %s\n\n"
                + "Affiliation:\n"
                + "%s\n\n"
                + "Current Term:\n"
                + "%d",
                isSenator ? "Sen." : "Rep.", name, party, term
        );
    }

    private List<String> getCommittees(String name) {
        //TODO: use API
        return Arrays.asList(
                "Ranking Member, Committee on the Budget",
                "Committee on Energy and Natural Resources",
                "Committee on Environment and Public Works",
                "Committee on Veterans' Affairs"
        );
    }

    private List<String> getBills(String name) {
        //TODO: use API
        return Arrays.asList(
                "Climate Protection Act (October 2015)",
                "Clean Energy Investment Act (October 2015)",
                "Clean Energy Worker Act (January 2016)",
                "Save Oak Flat Act (February 2016)"
        );
    }

    private static String detailedHtml(List<String> committees, List<String> bills) {
        return bulletedListHrml("Committees:", committees, "Not a member of any committees.")
                + "<br/>"
                + bulletedListHrml("Recently Sponsored Bills:", bills, "Has not sponsored any bills.");
    }

    private static String bulletedListHrml(String header, List<String> items, String emptyMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<big>%s</big><br/>", Html.escapeHtml(header)));

        if (items.isEmpty())
            sb.append(Html.escapeHtml(emptyMsg));
        else {
            for (String item : items) {
                sb.append(String.format("- %s <br/>", Html.escapeHtml(item)));
            }
        }
        return sb.toString();
    }
}
