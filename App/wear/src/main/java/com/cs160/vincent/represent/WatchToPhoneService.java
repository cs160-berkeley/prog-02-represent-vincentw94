package com.cs160.vincent.represent;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.ResultCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent on 3/3/2016.
 */
public class WatchToPhoneService extends Service implements GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mWatchApiClient;
    private List<Node> nodes = new ArrayList<>();
    private boolean ready = false;

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize the googleAPIClient for message passing
        mWatchApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();
        //and actually connect it
        mWatchApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWatchApiClient.disconnect();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override //alternate method to connecting: no longer create this in a new thread, but as a callback
    public void onConnected(Bundle bundle) {
        Log.d("WATCH", "searching nodes");
        Wearable.NodeApi.getConnectedNodes(mWatchApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        nodes = getConnectedNodesResult.getNodes();
                        Log.d("WATCH", "nodes = " + nodes.size());
                        ready = true;
                    }
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // ensure that callback in onConnected() has finished
        // so the available nodes have been found
        final int ind = intent.getIntExtra("ind", -1);
        final String path, value;
        if (ind != -1) {
            path = "/ind";
            value = ""+ind;
        }
        else {
            path = "/rand";
            value = "rand";
        }

        new Thread(new Runnable() {
            @Override public void run() {
                while (!ready) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {}
                }
                sendMessage(path, value);
            }
        }).start();

        return START_STICKY;
    }

    @Override //we need this to implement GoogleApiClient.ConnectionsCallback
    public void onConnectionSuspended(int i) {}

    private void sendMessage(final String path, final String text ) {
        Log.d("PHONE_TO_WATCH", "Sending " + path + " " + text + " nodes = " + nodes.size());
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(
                    mWatchApiClient, node.getId(), path, text.getBytes());
        }
    }

}