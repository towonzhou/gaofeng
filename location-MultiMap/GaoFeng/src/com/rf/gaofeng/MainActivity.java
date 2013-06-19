package com.rf.gaofeng;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private long exitTime = 0;
	private TextView textView;
	private JYAdapter jyAdapter;
	private ConnectivityManager gprsManager;
	private WifiManager wifiManager;
	private static final int GPS_ONLY = 0;
	private static final int WIFI_ONLY = 1;
	private static final int NETWORK_ONLY = 2;
	private static final int WIFI_NETWORK = 3;
	private static final int WIFI_GPS = 4;
	private static final int NETWORK_GPS = 5;
	private static final int ALL = 6;
	private static final int ANDROID = 0;
	private static final int AMAP = 1;
	private static final int BDMAP = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		textView = (TextView) findViewById(R.id.textView);
		jyAdapter = new JYAdapter(getApplicationContext());
		jyAdapter.open();
		gprsManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		
	}

	public void start(View v) {
		gpsOnly();
		Intent intent = new Intent(getApplicationContext(), Location.class);
		intent.putExtra("provider", "gps");
		intent.putExtra("type", "GPS_ONLY");
		startActivityForResult(intent, GPS_ONLY);
	}

	public void queryAndroid(View v) {
		show("android");
	}

	public void queryAmap(View v) {
		show("amap");
	}

	public void queryBDmap(View v) {
		show("bdmap");
	}
	
	public void map(View v) {
		Intent intent = new Intent(getApplicationContext(), Map.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Intent intent;

		switch (requestCode) {
		case GPS_ONLY:
			switch (resultCode) {
			case ANDROID:
				gpsOnly();
				intent = new Intent(getApplicationContext(), ALocation.class);
				intent.putExtra("provider", "gps");
				intent.putExtra("type", "GPS_ONLY");
				startActivityForResult(intent, GPS_ONLY);
				break;
			case AMAP:
				gpsOnly();
				intent = new Intent(getApplicationContext(), BLocation.class);
				intent.putExtra("provider", "gps");
				intent.putExtra("type", "GPS_ONLY");
				startActivityForResult(intent, GPS_ONLY);
				break;
			case BDMAP:
				wifiOnly();
				intent = new Intent(getApplicationContext(), Location.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "WIFI_ONLY");
				startActivityForResult(intent, WIFI_ONLY);
				break;
			}
			break;

		case WIFI_ONLY:
			switch (resultCode) {
			case ANDROID:
				wifiOnly();
				intent = new Intent(getApplicationContext(), ALocation.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "WIFI_ONLY");
				startActivityForResult(intent, WIFI_ONLY);
				break;
			case AMAP:
				wifiOnly();
				intent = new Intent(getApplicationContext(), BLocation.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "WIFI_ONLY");
				startActivityForResult(intent, WIFI_ONLY);
				break;
			case BDMAP:
				networkOnly();
				intent = new Intent(getApplicationContext(), Location.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "NETWORK_ONLY");
				startActivityForResult(intent, NETWORK_ONLY);
				break;
			}
			break;

		case NETWORK_ONLY:
			switch (resultCode) {
			case ANDROID:
				networkOnly();
				intent = new Intent(getApplicationContext(), ALocation.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "NETWORK_ONLY");
				startActivityForResult(intent, NETWORK_ONLY);
				break;
			case AMAP:
				networkOnly();
				intent = new Intent(getApplicationContext(), BLocation.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "NETWORK_ONLY");
				startActivityForResult(intent, NETWORK_ONLY);
				break;
			case BDMAP:
				wifiNetwork();
				intent = new Intent(getApplicationContext(), Location.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "WIFI_NETWORK");
				startActivityForResult(intent, WIFI_NETWORK);
				break;
			}
			break;

		case WIFI_NETWORK:
			switch (resultCode) {
			case ANDROID:
				wifiNetwork();
				intent = new Intent(getApplicationContext(), ALocation.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "WIFI_NETWORK");
				startActivityForResult(intent, WIFI_NETWORK);
				break;
			case AMAP:
				wifiNetwork();
				intent = new Intent(getApplicationContext(), BLocation.class);
				intent.putExtra("provider", "network");
				intent.putExtra("type", "WIFI_NETWORK");
				startActivityForResult(intent, WIFI_NETWORK);
				break;
			case BDMAP:
				wifiOnly();
				intent = new Intent(getApplicationContext(), Location.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "WIFI_GPS");
				startActivityForResult(intent, WIFI_GPS);
				break;
			}
			break;

		case WIFI_GPS:
			switch (resultCode) {
			case ANDROID:
				wifiOnly();
				intent = new Intent(getApplicationContext(), ALocation.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "WIFI_GPS");
				startActivityForResult(intent, WIFI_GPS);
				break;
			case AMAP:
				wifiOnly();
				intent = new Intent(getApplicationContext(), BLocation.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "WIFI_GPS");
				startActivityForResult(intent, WIFI_GPS);
				break;
			case BDMAP:
				networkOnly();
				intent = new Intent(getApplicationContext(), Location.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "NETWORK_GPS");
				startActivityForResult(intent, NETWORK_GPS);
				break;
			}
			break;

		case NETWORK_GPS:
			switch (resultCode) {
			case ANDROID:
				networkOnly();
				intent = new Intent(getApplicationContext(), ALocation.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "NETWORK_GPS");
				startActivityForResult(intent, NETWORK_GPS);
				break;
			case AMAP:
				networkOnly();
				intent = new Intent(getApplicationContext(), BLocation.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "NETWORK_GPS");
				startActivityForResult(intent, NETWORK_GPS);
				break;
			case BDMAP:
				wifiNetwork();
				intent = new Intent(getApplicationContext(), Location.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "ALL");
				startActivityForResult(intent, ALL);
				break;
			}
			break;

		case ALL:
			switch (resultCode) {
			case ANDROID:
				wifiNetwork();
				intent = new Intent(getApplicationContext(), ALocation.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "ALL");
				startActivityForResult(intent, ALL);
				break;
			case AMAP:
				wifiNetwork();
				intent = new Intent(getApplicationContext(), BLocation.class);
				intent.putExtra("provider", "lbs");
				intent.putExtra("type", "ALL");
				startActivityForResult(intent, ALL);
				break;
			case BDMAP:
				break;
			}
			break;
		}
	}

	private void show(String table) {
		jyAdapter.setTabel(table);
		Cursor cursor = jyAdapter.getAllEntries();
		String string = "<b>" + table + "<b>";
		double acc = -1;
		if (cursor.moveToFirst()) {
			do {
				acc = cursor.getDouble(cursor.getColumnIndex("accuracy"));
				if (acc == -1) {
					string = string + "<br/><br/><br/><font color='red'><b>"
							+ cursor.getString(cursor.getColumnIndex("type"))
							+ " 测试开始时间："
							+ cursor.getString(cursor.getColumnIndex("date"))
							+ "</b></font>";
				} else {
					string = string
							+ "<br/><br/>时间: "
							+ cursor.getString(cursor.getColumnIndex("date"))
							+ "<br/>经纬度: "
							+ cursor.getDouble(cursor
									.getColumnIndex("longitude"))
							+ "， "
							+ cursor.getDouble(cursor
									.getColumnIndex("latitude")) + "<br/>精度范围: "
							+ acc + "<br/>定位数据来源："  + cursor.getString(cursor.getColumnIndex("source")) + "<br/>测试方式: "
							+ cursor.getString(cursor.getColumnIndex("type"));
				}

			} while (cursor.moveToNext());
		}
		textView.setText(Html.fromHtml(string));
	}

	private boolean gpsOnly() {
		setGprs(false);
		setWifi(false);
		return true;
	}

	private boolean wifiOnly() {
		setGprs(false);
		setWifi(true);
		return true;
	}

	private boolean networkOnly() {
		setGprs(true);
		setWifi(false);
		return true;
	}

	private boolean wifiNetwork() {
		setGprs(true);
		setWifi(true);
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean setGprs(boolean flag) {
		Class cmClass = gprsManager.getClass();
		Class[] argClasses = new Class[1];
		argClasses[0] = boolean.class;
		try {
			Method method = cmClass.getMethod("setMobileDataEnabled",
					argClasses);
			method.invoke(gprsManager, flag);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean setWifi(boolean flag) {
		return wifiManager.setWifiEnabled(flag);
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
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void alert(String string) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Toast.makeText(getBaseContext(), string, Toast.LENGTH_SHORT).show();
		Log.i("MyLog", string);
	}

	public void log(String string) {
		// TODO Auto-generated method stub
		Log.i("MyLog", string);
	}
}
