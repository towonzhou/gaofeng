package com.rf.gaofeng;

import android.app.Activity;
import android.content.ContentValues;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;

public class ALocation extends Activity {
	private LocationManagerProxy mAMapLocationManager = null;
	private AMapLocationListener mListener;
	private JYAdapter jyAdapter;
	private static final String DB_TABLE = "amap";
	private static int timer = 0;
	private TextView textView;
	private static final int RESULT_CODE = 1;
	private String provider, type;
	private Handler handler;
	private Runnable runnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		provider = getIntent().getStringExtra("provider");
		type = getIntent().getStringExtra("type");
		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.removeCallbacks(runnable);
				jyAdapter.close();
				setResult(RESULT_CODE);
				if (mAMapLocationManager != null) {
					mAMapLocationManager.removeUpdates(mListener);
					mAMapLocationManager.destory();
				}
				mAMapLocationManager = null;
				finish();
			}
		};

		setContentView(R.layout.location);
		textView = (TextView) findViewById(R.id.textView);
		textView.setText("\t\t\t高德定位" + "\t" + type);
		jyAdapter = new JYAdapter(getApplicationContext());
		jyAdapter.setTabel(DB_TABLE);
		jyAdapter.open();
		ContentValues newValues = new ContentValues();
		newValues.put("longitude", -1);
		newValues.put("latitude", -1);
		newValues.put("accuracy", -1);
		newValues.put("type", type);
		jyAdapter.insertEntry(newValues);

		mListener = new AMapLocationListener() {

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
			public void onLocationChanged(Location arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onLocationChanged(AMapLocation location) {
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
					if (mAMapLocationManager != null) {
						mAMapLocationManager.removeUpdates(mListener);
						mAMapLocationManager.destory();
					}
					mAMapLocationManager = null;
					finish();
				}
			}
		};

		mAMapLocationManager = LocationManagerProxy
				.getInstance(getApplicationContext());
		mAMapLocationManager.requestLocationUpdates(provider, 10, 3000,
				mListener);
		handler.postDelayed(runnable, 90 * 1000);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			Toast.makeText(getBaseContext(), "正在测试，领导说不让退出", Toast.LENGTH_SHORT)
					.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
