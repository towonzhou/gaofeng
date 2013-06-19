package com.rf.gaofeng;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

public class Location extends Activity {
	private LocationManager locationManager;
	private JYAdapter jyAdapter;
	private static final String DB_TABLE = "android";
	private static int timer = 0;
	private TextView textView;
	private static final int RESULT_CODE = 0;
	private String provider, type;
	private Handler handler;
	private Runnable runnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		provider = getIntent().getStringExtra("provider");
		if (provider.equals("lbs")) {
			provider = LocationManager.GPS_PROVIDER;
		}
		type = getIntent().getStringExtra("type");

		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.removeCallbacks(runnable);
				jyAdapter.close();
				setResult(RESULT_CODE);
				if (locationManager != null) {
					locationManager.removeUpdates(listener);
				}
				locationManager = null;
				finish();
			}
		};

		setContentView(R.layout.location);
		textView = (TextView) findViewById(R.id.textView);
		textView.setText("\t\t\t安卓定位" + "\t" + type);
		jyAdapter = new JYAdapter(getApplicationContext());
		jyAdapter.setTabel(DB_TABLE);
		jyAdapter.open();
		ContentValues newValues = new ContentValues();
		newValues.put("longitude", -1);
		newValues.put("latitude", -1);
		newValues.put("accuracy", -1);
		newValues.put("type", type);
		jyAdapter.insertEntry(newValues);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(provider, 3000, 10, listener);
		handler.postDelayed(runnable, 90 * 1000);
	}

	LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(android.location.Location location) {
			// TODO Auto-generated method stub
			handler.removeCallbacks(runnable);
			if (location != null && timer < 3) {

				ContentValues newValues = new ContentValues();
				newValues.put("longitude", location.getLongitude());
				newValues.put("latitude", location.getLatitude());
				newValues.put("accuracy", location.getAccuracy());
				newValues.put("type", type);
				jyAdapter.insertEntry(newValues);
				timer++;
				textView.append("\n次数：" + timer + "\n经纬度："
						+ location.getLongitude() + ","
						+ location.getLatitude() + "\n精度范围："
						+ location.getAccuracy());
			} else {
				setResult(RESULT_CODE);
				jyAdapter.close();
				if (locationManager != null) {
					locationManager.removeUpdates(listener);
				}
				locationManager = null;
				finish();
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			Toast.makeText(getBaseContext(), "正在测试，领导说不让退出", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
