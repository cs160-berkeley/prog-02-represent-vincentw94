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

/**
 * Created by Vincent on 3/3/2016.
 */
public class PhoneToWatchService extends Service {

    private GoogleApiClient mApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize the googleAPIClient for message passing
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {}
                })
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int zip = intent.getIntExtra(getResources().getString(R.string.zip_intent_key), -1);
        Log.d("SENDING ZIP", "" + zip);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mApiClient.connect();
                sendMessage("/zip", ""+zip);
            }
        }).start();

        return START_STICKY;
    }

    @Override //remember, all services need to implement an IBiner
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessage(final String path, final byte[] bytes) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                Log.d("SEND", "Num nodes = " + nodes.getNodes().size());
                for(Node node : nodes.getNodes()) {
                    Log.d("SEND", "Actually sending msg to path " + path + " @ node " + node.toString());
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, bytes ).await();
                }
            }
        }).start();
    }

    private void sendMessage(final String path, String msg) {
        sendMessage(path, msg.getBytes());
    }

}

