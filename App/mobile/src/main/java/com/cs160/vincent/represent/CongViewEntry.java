package com.cs160.vincent.represent;

import android.graphics.Bitmap;
import android.text.Html;
/**
 * Created by Vincent on 3/2/2016.
 */
public class CongViewEntry {
    private String name, party, email, website, tweet;
    private boolean isSenator;
    private Bitmap img;

    private static final int ICON_WIDTH = 250, ICON_HEIGHT = 350;

    public CongViewEntry(String name, String party, String email,
                         String website, String tweet, boolean isSenator, Bitmap img) {
        this.name = Html.escapeHtml(name);
        this.party = Html.escapeHtml(party);
        this.email = Html.escapeHtml(email);
        this.website = Html.escapeHtml(website);
        this.tweet = Html.escapeHtml(tweet);
        this.isSenator = isSenator;
        this.img = Bitmap.createScaledBitmap(img, ICON_WIDTH, ICON_HEIGHT, true);
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

    public String getTitle() {
        return String.format("%s %s (%s)", isSenator ? "Sen. " : "Rep. ", name, party);
    }

    public Bitmap getImg() {
        return img;
    }

    public String getHtmlText() {
        return String.format(
            "%s<br/>"
            + "Email:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=blue>%s</font><br/>"
            + "Website:&nbsp;&nbsp;<font color=blue>%s</font><br/><br/>"
            + "Last tweet:<br/>"
            + "%s<br/>",
            getTitle(), email, website, tweet);
    }
}
