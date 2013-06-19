package com.rf.gaofeng;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.google.analytics.tracking.android.EasyTracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AMapActivity extends FragmentActivity {
	private AMap aMap;
	private UiSettings uiSettings;
	private TextView textView;
	private Button button;

	private ALocation aLocation = null;
	private long exitTime = 0;
	private Handler handler = null;
	private String provider = "lbs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.amap);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String text = msg.obj.toString();
				alert(text);
				textView.setText(text);
			}
		};
		textView = (TextView) findViewById(R.id.textView);
		aLocation = new ALocation(getBaseContext(), handler);
		button = (Button) findViewById(R.id.button);

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				Intent intent = new Intent(getApplicationContext(),
						ASettingActivity.class);
				intent.putExtra("network",
						aLocation.isProviderEnabled("network"));
				intent.putExtra("passive",
						aLocation.isProviderEnabled("passive"));
				intent.putExtra("gps", aLocation.isProviderEnabled("gps"));
				intent.putExtra("lbs", aLocation.isProviderEnabled("lbs"));
				intent.putExtra("provider", provider);
				aLocation.removeLocation();
				startActivityForResult(intent, 0);
			}
		});

		aMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		aMap.setLocationSource(aLocation);
		aMap.setMyLocationEnabled(true);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.90403,
				116.407525), 15));

		uiSettings = aMap.getUiSettings();
		uiSettings.setCompassEnabled(true);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		provider = data.getStringExtra("provider");
		aLocation.setProvider(provider);
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
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		aLocation.deactivate();
		aLocation = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
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
