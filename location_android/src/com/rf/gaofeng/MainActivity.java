package com.rf.gaofeng;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private long exitTime = 0;
	private Button gps = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Button aButton = (Button) findViewById(R.id.amap_button);
		Button bButton = (Button) findViewById(R.id.baidu_button);
		gps = (Button) findViewById(R.id.gps);
		EasyTracker.getInstance().setContext(getApplicationContext());

		aButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(),
						AMapActivity.class);
				startActivity(intent);
			}
		});

		bButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(),
						BDMapActivity.class);
				startActivity(intent);
			}
		});

		gps.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				if (locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

					Location location = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					gps.setText("定位中……");
					if (location != null) {
						gps.setText("GPS坐标： " + location.getLongitude() + "," + location.getLatitude());
					}else {
						gps.setText("GPS失败，点击重新定位   ");
					}
					
				} else {
					gps.setText("没有GPS模块或者未开启GPS");
				}

			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				alert("再按一次退出程序");
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onRestart() {
		super.onRestart();
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void alert(final String string) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(), string, Toast.LENGTH_SHORT)
						.show();
			}
		});
		Log.i("MyLog", string);
	}

	public void log(String string) {
		// TODO Auto-generated method stub
		Log.i("MyLog", string);
	}
}
