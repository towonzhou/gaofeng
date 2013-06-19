package com.rf.gaofeng;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKOfflineMap;
import com.baidu.mapapi.map.MKOfflineMapListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;

public class BDMapActivity extends Activity {
	private BMapManager mBMapMan = null;
	private MapView mMapView = null;
	private BLocation location = null;
	private TextView textView = null;
	private Button button = null;
	private MKOfflineMap mOffline = null;
	private MapController mapController = null;
	private Handler handler = null;
	private Intent data = new Intent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());

		mBMapMan.init("BC2D03E62D8E23C4CA1287DCA0A00BE312EBBBE2", null);
		setContentView(R.layout.bdmap);
		textView = (TextView) findViewById(R.id.textView);
		mMapView = (MapView) findViewById(R.id.bmapsView);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String text = msg.obj.toString();
				alert(text);
				textView.setText(text);
			}
		};

		button = (Button) findViewById(R.id.button);

		mOffline = new MKOfflineMap();
		mapController = mMapView.getController();
		mOffline.init(mapController, new MKOfflineMapListener() {
			@Override
			public void onGetOfflineMapState(int type, int state) {
				mOffline.scan();
			}
		});

		location = new BLocation(this, handler);

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				Intent intent = new Intent(getApplicationContext(),
						BSettingActivity.class);
				switch (data.getIntExtra("priority",
						LocationClientOption.GpsFirst)) {
				case LocationClientOption.GpsFirst:
					intent.putExtra("priority", R.id.loc_gpsFirst);
					break;
				case LocationClientOption.NetWorkFirst:
					intent.putExtra("priority", R.id.loc_netWorkFirst);
					break;
				}
				intent.putExtra("gps", data.getBooleanExtra("gps", true));
				intent.putExtra("cache", data.getBooleanExtra("cache", false));
				location.stop();
				startActivityForResult(intent, 0);
			}
		});

		MapController mMapController = mMapView.getController();
		mMapController.setZoom(16);

		location.start(mMapView);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.data = data;
		location.start(data);
	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		location.stop();
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}
		super.onResume();
	}

	private long exitTime = 0;

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

	public void alert(String string) {
		// TODO Auto-generated method stub
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
		Log.i("MyLog", string);
	}

	public void log(String string) {
		// TODO Auto-generated method stub
		Log.i("MyLog", string);
	}
}
