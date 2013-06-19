package com.rf.gaofeng;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class BLocation extends Activity {

	private LocationClient mLocationClient = null;
	private BDLocationListener myListener = new MyLocationListener();
	private LocationClientOption option = null;

	private JYAdapter jyAdapter;
	private static final String DB_TABLE = "bdmap";
	private static int timer = 0;
	private TextView textView;
	private static final int RESULT_CODE = 2;
	private String provider, type;
	private Handler handler;
	private Runnable runnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		provider = getIntent().getStringExtra("provider");
		type = getIntent().getStringExtra("type");
		setContentView(R.layout.location);

		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.removeCallbacks(runnable);
				jyAdapter.close();
				setResult(RESULT_CODE);
				stop();
				finish();
			}
		};

		textView = (TextView) findViewById(R.id.textView);
		textView.setText("\t\t\t百度定位" + "\t" + type);
		jyAdapter = new JYAdapter(getApplicationContext());
		jyAdapter.setTabel(DB_TABLE);
		jyAdapter.open();
		ContentValues newValues = new ContentValues();
		newValues.put("longitude", -1);
		newValues.put("latitude", -1);
		newValues.put("accuracy", -1);
		newValues.put("type", type);
		jyAdapter.insertEntry(newValues);

		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.registerLocationListener(myListener);
		option = new LocationClientOption();

		if (provider.equals("network")) {
			option.setOpenGps(false);
			option.setPriority(LocationClientOption.NetWorkFirst);
		} else {
			option.setOpenGps(true);
			option.setPriority(LocationClientOption.GpsFirst);
		}
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(3000);// 设置发起定位请求的间隔时间为5000ms
		option.disableCache(true);// 禁止启用缓存定位
		option.setPoiNumber(5); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
		mLocationClient.setLocOption(option);
		start();
		handler.postDelayed(runnable, 90 * 1000);
	}

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			handler.removeCallbacks(runnable);
			if (location != null && timer < 3) {
				String source = "";
				ContentValues newValues = new ContentValues();
				newValues.put("longitude", location.getLongitude());
				newValues.put("latitude", location.getLatitude());
				newValues.put("accuracy", location.getRadius());
				newValues.put("type", type);
				switch (location.getLocType()) {
				case 61:
					source = "GPS结果";
					break;
				case 65:
					source = "缓存结果";
					break;
				case 68:
					source = "网络链接失败，缓存结果";
					break;
				case 161:
					source = "网络定位结果";
					break;
				default:
					source = "定位失败";
					break;
				}

				newValues.put("source", source);
				jyAdapter.insertEntry(newValues);
				timer++;
				textView.append("\n次数：" + timer + "\n经纬度："
						+ location.getLongitude() + ","
						+ location.getLatitude() + "\n精度范围："
						+ location.getRadius() + "\n数据来源：" + source);
			} else {
				setResult(RESULT_CODE);
				jyAdapter.close();
				stop();
				finish();
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	public void start() {
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}

		mLocationClient.requestLocation();
	}

	public void stop() {
		if (mLocationClient.isStarted()) {
			mLocationClient.stop();
		}
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