package com.cs160.vincent.represent;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by Vincent on 3/3/2016.
 */
public class WatchListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d("RECEIVED MSG @", path);
        String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);

        if (path.equals("/update")) {
            MainWearActivity.repDataUpdate = new RepData[Integer.parseInt(value)];
            for (int i=0; i<MainWearActivity.repDataUpdate.length; i++)
                MainWearActivity.repDataUpdate[i] = new RepData("Foo", null);
        }
        else if (path.startsWith("/name")) {
            Log.d("WATCH", "name = " + value);
            int ind = Integer.parseInt(path.substring("/name".length()));
            MainWearActivity.repDataUpdate[ind].name = value;
        }
        else if (path.startsWith("/img")) {
            int ind = Integer.parseInt(path.substring("/img".length()));
            byte[] bytes = messageEvent.getData();
            MainWearActivity.repDataUpdate[ind].setImg( BitmapFactory.decodeByteArray(bytes, 0, bytes.length) );
        }
        else if (path.equals("/location")) {
            VoteView.location = value;
        }
        else if (path.equals("/obama")) {
            VoteView.obama = Double.parseDouble(value);
        }
        else if (path.equals("/romney")) {
            VoteView.romney = Double.parseDouble(value);
        }
        else if (path.equals("/finish")) {
            MainWearActivity.repData = MainWearActivity.repDataUpdate;

            Intent intent = new Intent(this, MainWearActivity.class );
            //you need to add this flag since you're starting a new activity from a service
            intent.putExtra("loaded", 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Log.d("WATCH", "Unhandled msg @ path " + path + ": " + value);
            super.onMessageReceived( messageEvent );
        }

    }
}
