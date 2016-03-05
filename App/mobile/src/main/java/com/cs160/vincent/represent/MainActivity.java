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

public class MainActivity extends AppCompatActivity {
    public static final int BG_IMG_OPACITY = 51;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main_root);
        root.getBackground().setAlpha(BG_IMG_OPACITY);

        // register callback when watch signals detailed view
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MAIN", "Init random zip!");
                openCongView((int)(Math.random()*100000));
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
        openCongView(resolveZip());
    }

    private int resolveZip() {
        // TODO: current location
        return 94740;
    }

    private void openCongView(int zip) {
        TextView errText = (TextView) findViewById(R.id.zip_err);
        errText.setText("");

        Intent intent = new Intent(this, CongressionalView.class);
        intent.putExtra(getResources().getString(R.string.zip_intent_key), zip);

        startActivity(intent);
    }


}
