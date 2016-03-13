package com.cs160.vincent.represent;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class DetailedView extends AppCompatActivity {

    private static final int IMG_WIDTH = 400, IMG_HEIGHT = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        ((TextView)findViewById(R.id.det_info)).setMovementMethod(new ScrollingMovementMethod());

//        View root = findViewById(R.id.det_root);
//        root.getBackground().setAlpha(MainActivity.BG_IMG_OPACITY);

        Intent intent = getIntent();
        Resources res = getResources();
        int ind = intent.getIntExtra("ind", -1);
        CongViewEntry entry = CongressionalView.repsInfo.get(ind);

        String name = entry.getName();
        String party = entry.getParty();
        boolean isSenator = entry.isSenator();
        String term = entry.getTermEnd();

        TextView profInfo = (TextView) findViewById(R.id.det_profile);
        profInfo.setText(profileText(name, party, isSenator, term));

        List<String> committees = getCommittees(ind), bills = getBills(ind);
        String html = detailedHtml(committees, bills);
        TextView comBillInfo = (TextView) findViewById(R.id.det_info);
        comBillInfo.setText(Html.fromHtml(html));

        Bitmap bitmap = CongressionalView.repsInfo.get(ind).getImg();
        bitmap = Bitmap.createScaledBitmap(bitmap, IMG_WIDTH, IMG_HEIGHT, true);
        ImageView imgView = (ImageView) findViewById(R.id.detailed_img);
        imgView.setImageBitmap(bitmap);

        // get filename for picture from intent
//        try {
//            Bitmap bitmap = BitmapFactory.decodeStream(openFileInput(res.getString(R.string.rep_img_fn)));
//            bitmap = Bitmap.createScaledBitmap(bitmap, IMG_WIDTH, IMG_HEIGHT, true);
//            ImageView imgView = (ImageView) findViewById(R.id.detailed_img);
//            imgView.setImageBitmap(bitmap);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private static String profileText(String name, String party, boolean isSenator, String term) {
        return String.format(
                "%s %s\n\n"
                + "Affiliation:\n"
                + "%s\n\n"
                + "End of Current Term:\n"
                + "%s",
                isSenator ? "Sen." : "Rep.", name, party, term
        );
    }

    private List<String> getCommittees(int ind) {
//        //TODO: use API
//        return Arrays.asList(
//                "Ranking Member, Committee on the Budget",
//                "Committee on Energy and Natural Resources",
//                "Committee on Environment and Public Works",
//                "Committee on Veterans' Affairs"
//        );
        return CongressionalView.repsInfo.get(ind).getCommittees();
    }

    private List<String> getBills(int ind) {
        //TODO: use API
//        return Arrays.asList(
//                "Climate Protection Act (October 2015)",
//                "Clean Energy Investment Act (October 2015)",
//                "Clean Energy Worker Act (January 2016)",
//                "Save Oak Flat Act (February 2016)"
//        );
        return CongressionalView.repsInfo.get(ind).getBills();
    }

    private static String detailedHtml(List<String> committees, List<String> bills) {
        return bulletedListHtml("Committees:", committees, "Not a member of any committees.")
                + "<br/>"
                + bulletedListHtml("Recently Sponsored Bills:", bills, "Has not sponsored any bills.");
    }

    private static String bulletedListHtml(String header, List<String> items, String emptyMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<big>%s</big><br/>", Html.escapeHtml(header)));

        if (items.isEmpty())
            sb.append(Html.escapeHtml(emptyMsg) + "<br/>");
        else {
            for (String item : items) {
                sb.append(String.format("%s <br/><br/>", Html.escapeHtml(item)));
            }
        }
        return sb.toString();
    }
}
