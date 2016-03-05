package com.cs160.vincent.represent;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by Vincent on 3/3/2016.
 */
public class WatchListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d("RECEIVED MSG", path);
        if (path.equals("/zip")) {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);

            Intent intent = new Intent(this, MainWearActivity.class );
            //you need to add this flag since you're starting a new activity from a service
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("zip", Integer.parseInt(value));
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }

    }
}
