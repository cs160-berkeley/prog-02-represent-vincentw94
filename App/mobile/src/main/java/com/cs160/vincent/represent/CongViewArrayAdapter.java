package com.cs160.vincent.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by Vincent on 3/2/2016.
 */
public class CongViewArrayAdapter extends ArrayAdapter<CongViewEntry> {
    public CongViewArrayAdapter(Context context, ArrayList<CongViewEntry> data) {
        super(context, R.layout.cong_view_entry, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            convertView = inflater.inflate(R.layout.cong_view_entry, parent, false);
        }

        final CongViewEntry entry = getItem(position);

        ImageView congPic = (ImageView) convertView.findViewById(R.id.cong_entry_pic);
        if (entry.getImg() != null)
            congPic.setImageBitmap(entry.getImg());

        TextView congInfo = (TextView) convertView.findViewById(R.id.cong_entry_info);
        congInfo.setText(Html.fromHtml(entry.getHtmlText()));

        // add listener here, when access to the CongViewEntry is convenient
        final int positionCpy = position;
        Button moreInfo = (Button) convertView.findViewById(R.id.more_info_button);
        moreInfo.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Resources res = getContext().getResources();
                Intent intent = new Intent(getContext(), DetailedView.class);
                intent.putExtra("ind", positionCpy);
//                intent.putExtra(res.getString(R.string.rep_name_key), entry.getName());
//                intent.putExtra(res.getString(R.string.rep_party_key), entry.getParty());
//                intent.putExtra(res.getString(R.string.rep_type_key), entry.isSenator());
//                intent.putExtra(res.getString(R.string.rep_term_key), 2016);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                // save image locally
//                String fn = res.getString(R.string.rep_img_fn);
//                try {
//                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                    entry.getImg().compress(Bitmap.CompressFormat.PNG, 100, bytes);
//                    FileOutputStream out = getContext().openFileOutput(fn, Context.MODE_PRIVATE);
//                    out.write(bytes.toByteArray());
//                    out.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }
}
