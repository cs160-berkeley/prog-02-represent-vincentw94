package com.cs160.vincent.represent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Vincent on 3/2/2016.
 */
public class CongViewEntry {
    private String name, party, email, website, tweet, termEnd, bioId;
    private List<String> committees, bills;
    private boolean isSenator;
    private Bitmap img;

    private static final int ICON_WIDTH = 250, ICON_HEIGHT = 350;
    private static Bitmap defaultFace;

    public CongViewEntry(String name, String party, String email, String website, String tweet,
                         String termEnd, String bioId, boolean isSenator, Bitmap img) {
        this.name = Html.escapeHtml(name);
        this.party = Html.escapeHtml(party);
        this.email = Html.escapeHtml(email);
        this.website = Html.escapeHtml(website);
        this.tweet = Html.escapeHtml(tweet);
        this.isSenator = isSenator;
        this.termEnd = termEnd;
        this.bioId = bioId;

        if (defaultFace == null) {
            defaultFace = BitmapFactory.decodeResource(MainActivity.globalContext().getResources(), R.drawable.pic_placeholder);
            defaultFace = Bitmap.createScaledBitmap(defaultFace, ICON_WIDTH, ICON_HEIGHT, true);
        }

        setImg(img);

        committees = bills = new ArrayList<String>();
    }

    public String getBioId() {
        return bioId;
    }

    public String getTermEnd() {
        return termEnd;
    }

    public String getName() {
        return name;
    }

    public String getParty() {
        return party;
    }

    public boolean isSenator() {
        return isSenator;
    }

    public void setCommittees(List<String> comms) {
        committees = comms;
    }

    public List<String> getCommittees() {
        return committees;
    }

    public void setBills(List<String> bills) {
        this.bills = bills;
    }

    public List<String> getBills() {
        return bills;
    }

    public String getTitle() {
        return String.format("%s %s (%s)", isSenator ? "Sen. " : "Rep. ", name, party);
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        if (img != null)
            this.img = Bitmap.createScaledBitmap(img, ICON_WIDTH, ICON_HEIGHT, true);
        else {
            this.img = defaultFace;
        }
    }

    public String getHtmlText() {
//        return String.format(
//                "%s<br/>"
//                        + "Email:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=blue>%s</font><br/>"
//                        + "Website:&nbsp;&nbsp;<font color=blue>%s</font><br/><br/>"
//                        + "Last tweet:<br/>"
//                        + "%s<br/>",
//                getTitle(), email, website, tweet);
        return String.format(
                "%s<br/>"
                        + "<font color=blue>%s</font><br/>"
                        + "<font color=blue>%s</font><br/><br/>"
                        + "Last tweet:<br/>"
                        + "%s<br/>",
                getTitle(), email, website, tweet);
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }
}


