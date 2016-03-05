package com.cs160.vincent.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainWearActivity extends Activity {
    public static final int IMG_SIZE = 150;

    private List<RepData> repData = new ArrayList<RepData>();
    private int currInd, currZip;

    private GestureDetector gestures;
    private SensorManager sensorManager;
    private ShakeListener shakeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);

        final Intent intent = getIntent();
        final WatchViewStub stub = (WatchViewStub)findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener((new WatchViewStub.OnLayoutInflatedListener() {
            public void onLayoutInflated(WatchViewStub stub) {
                currZip = intent.getIntExtra("zip", -1);

                // query zip
                Log.d("LAYOUT INFLATED", "zip = " + currZip);
                if (currZip != -1) {
                    repData = query(currZip);
                    currInd = 0;
                    update();
                }

                // default welcome screen
                else {
                    ImageView img = (ImageView) findViewById(R.id.rep_img);
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flag);
                    bitmap = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);
                    img.setImageBitmap(bitmap);

                    TextView name = (TextView) findViewById(R.id.rep_name);
                    name.setText(getResources().getString(R.string.main_welcome));
                }
            }
        }));

        // shake sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        shakeListener = new ShakeListener(this);

        // swipe listener
        gestures = new GestureDetector(this, new SwipeListener(this));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return gestures.onTouchEvent(e) || super.onTouchEvent(e);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(shakeListener, accel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeListener);
    }

    private List<RepData> query(int zip) {
        return Arrays.asList(
                new RepData("Sen. Megan Jones\nD-CA", BitmapFactory.decodeResource(getResources(), R.drawable.meg)),
                new RepData("Sen. Mike Mitch\nR-CA", BitmapFactory.decodeResource(getResources(), R.drawable.mike)),
                new RepData("Rep. Bryan Hillow\nI-CA", BitmapFactory.decodeResource(getResources(), R.drawable.bryan))
        );
    }

    public void onSwipe(boolean right) {
        Log.d("PARENT", "parent onSwipe()");

        // swipe to dismiss
        if (repData.isEmpty()) {
            if (right)
                finish();
            return;
        }

        currInd += right ? -1 : 1;
        if (currInd == -1) {
            // dismiss if swipe to before first pic
            currInd = 0;
            finish();
        }
        else if (currInd == repData.size())
            currInd = repData.size() - 1;
        else
            update();
    }

    private void update() {
        ImageView img = (ImageView) findViewById(R.id.rep_img);
        img.setImageBitmap(repData.get(currInd).img);

        TextView name = (TextView) findViewById(R.id.rep_name);
        name.setText(repData.get(currInd).name);
    }

    public int getCurrInd() {
        return currInd;
    }

    public int getZip() {
        return currZip;
    }

    public boolean loadedData() {
        return !repData.isEmpty();
    }
}

// heavily borrows from http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
class SwipeListener extends GestureDetector.SimpleOnGestureListener {
    private static final int SWIPE_THRESHOLD = 80;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private MainWearActivity context;
    private boolean canTrigger;

    public SwipeListener(MainWearActivity context) {
        this.context = context;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d("LISTENER", "onDown()");
        canTrigger = true;
        return true;    // need this for all other events
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float unused1, float unused2) {
        float dy = e2.getY() - e1.getY();
        float dx = e2.getX() - e1.getX();
//        Log.d("LISTENER", "onScroll(): dx = " + dx + " dy = " + dy + " can trigger = " + canTrigger);
        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > SWIPE_THRESHOLD && canTrigger) {
            context.onSwipe(dx > 0);
            Log.d("Triggered swipe:", ""+(dx > 0));
            canTrigger = false;   // only trigger once per ACTION_DOWN
            return true;
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d("LISTENER", "onDoubleTap()");

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d("LISTENER", "onLongPress()");

        Intent intent = new Intent(context, VoteView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("zip", context.getZip());
        context.startActivity(intent);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d("LISTENER", "onSingleTapConfirmed()");

        // notify phone to switch to DetailedView
        if (context.loadedData()) {
            Intent intent = new Intent(context, WatchToPhoneService.class);
            intent.putExtra("ind", context.getCurrInd());
            context.startService(intent);
        }
        return true;
    }
}

class ShakeListener implements SensorEventListener {
    public static final int THRESHOLD = 50;
    private float oldX, oldY, oldZ;
    private boolean first;
    private MainWearActivity context;

    public ShakeListener(MainWearActivity context) {
        this.context = context;
        first = true;
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent e) {
//        Log.d("SHAKE", String.format("onSensorChanged(): value = %.1f %.1f %.1f", e.values[0], e.values[1], e.values[2]));
        float x = e.values[0], y = e.values[1], z = e.values[2];
        float dx = x - oldX, dy = y - oldY, dz = z = oldZ;
        float total = dx*dx + dy*dy + dz*dz;
        oldX = x;
        oldY = y;
        oldZ = z;

        if (!first && Math.sqrt(total) > THRESHOLD) {
            Intent intent = new Intent(context, WatchToPhoneService.class);
            intent.putExtra("", context.getCurrInd());
            context.startService(intent);
            Log.d("SHAKE", "BIG SHAKE");
        }
        first = false;
    }
}

class RepData {
    String name;
    Bitmap img;

    public RepData(String name, Bitmap img) {
        this.name = name;
        this.img = Bitmap.createScaledBitmap(img, MainWearActivity.IMG_SIZE, MainWearActivity.IMG_SIZE, true);
    }
}