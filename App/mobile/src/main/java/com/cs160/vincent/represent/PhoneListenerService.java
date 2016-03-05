package com.cs160.vincent.represent;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

public class PhoneListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d("MSG_RECV", "Phone received " + path);
        if(path.equals("/ind")) {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Log.d("MSG_RECV", "Value = " + value);

            Intent intent = new Intent();
            intent.setAction("to_detailed");
            intent.putExtra("ind", Integer.parseInt(value));
            sendBroadcast(intent);
        }
        else if (path.equals("/rand")) {
            Log.d("MSG_RECV", "Random!");

            Intent intent = new Intent();
            intent.setAction("random");
            sendBroadcast(intent);
        }
        else {
            super.onMessageReceived( messageEvent );
        }

    }
}
