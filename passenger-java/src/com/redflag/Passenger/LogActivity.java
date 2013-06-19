package com.redflag.Passenger;

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class LogActivity extends Activity implements GestureDetector.OnGestureListener, View.OnTouchListener {
    private ListView _listView = null;

    private static final float MIN_DISTANCE = 250.0F;
    private static final float MIN_VELOCITY = 400.0F;

    private GestureDetector _gestureDetector;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _gestureDetector = new GestureDetector(this, this);

        _listView = new ListView(getApplicationContext());
        _listView.setOnTouchListener(this);

        Intent caller = getIntent();
        if (caller != null) {
            ArrayList<String> extra = caller.getStringArrayListExtra(MainActivity.EXTRA_LOGS);
            loadLogs(extra);
        }
        setContentView(_listView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _listView = null;

    }

    public void loadLogs(ArrayList<String> logs) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1);
        adapter.addAll(logs);
        _listView.setAdapter(adapter);
    }

    public void debug(String msg) {
        Log.d("com.redflag.Passenger", msg);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        debug("onDown: " + motionEvent.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        debug("onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        debug("onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
//        debug("onScroll: " + motionEvent.toString());
//        debug("onScroll: " + motionEvent2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        debug("onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float vX, float vY) {
        debug("onFling: " + motionEvent.toString());
        debug("onFling: " + motionEvent2.toString());
        debug("onFling: " + vX + ", " + vY);

        // implement swipe L-t-R
        float dist = motionEvent2.getX() - motionEvent.getX();
        if (dist > 0 && dist > MIN_DISTANCE && vX > MIN_VELOCITY) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        debug("onTouch");
        _gestureDetector.onTouchEvent(motionEvent);
        return false;
    }
}