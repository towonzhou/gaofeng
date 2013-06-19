package com.rf.gaofeng;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKOfflineMap;
import com.baidu.mapapi.map.MKOfflineMapListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;

public class MainActivity extends Activity implements Driver {
	BMapManager mBMapMan = null;
	MapView mMapView = null;
	TextView textView = null;
	Button button = null;
	TelephonyManager telephonyManager = null;
	Socket socket = null;
	MKOfflineMap mOffline = null;
	MapController mapController = null;
	Vibrator vibrator = null;
	AlertView alertView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBMapMan = new BMapManager(getApplication());
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		telephonyManager = (TelephonyManager) getApplication()
				.getSystemService(Context.TELEPHONY_SERVICE);
		mBMapMan.init("BC2D03E62D8E23C4CA1287DCA0A00BE312EBBBE2", null);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.textView);
		mMapView = (MapView) findViewById(R.id.bmapsView);
		button = (Button) findViewById(R.id.button);

		mOffline = new MKOfflineMap();
		mapController = mMapView.getController();
		mOffline.init(mapController, new MKOfflineMapListener() {
			@Override
			public void onGetOfflineMapState(int type, int state) {
				switch (type) {
				case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
					// MKOLUpdateElement update = mOffline.getUpdateInfo(state);
					// logMsg(String.format("%s : %d%%", update.cityName,
					// update.ratio));
				}
					break;
				case MKOfflineMap.TYPE_NEW_OFFLINE:
					// logMsg(String.format("add offlinemap num:%d", state));
					break;
				case MKOfflineMap.TYPE_VER_UPDATE:
					// logMsg(String.format("new offlinemap ver"));
					break;
				}
			}
		});

		final Location location = new Location(this);
		try {
			socket = new Socket(this);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				String event = (String) button.getText();
				if (event.equals(getString(R.string.grap))) {
					socket.grap();
					alertView.show();
				} else {
					location.start(mMapView);
				}
			}
		});

		int num = mOffline.scan();
		if (num != 0)
			alert("已安装" + num + "个离线包");

		MapController mMapController = mMapView.getController();
		mMapController.setZoom(16);

		alertView = new AlertView();
		location.start(mMapView);
		socket.connect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
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
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void setButton(final Boolean flag) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (flag) {
					button.setText(R.string.grap);
				} else {
					button.setText(R.string.button);
				}

			}
		});
	}

	@Override
	public void setTextView(final String string) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				textView.setText(string);
			}
		});
	}

	@Override
	public void setTextView(final int id, final String... args) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				textView.setText(String.format(getString(id), args));
			}
		});
	}

	@Override
	public String getPhoneNumber() {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getLine1Number();
	}

	@Override
	public void alert(final String string) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
			}
		});
		Log.i("MyLog", string);
	}

	@Override
	public void log(String string) {
		// TODO Auto-generated method stub
		Log.i("MyLog", string);
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return getApplicationContext();
	}

	@Override
	public Button getButton() {
		// TODO Auto-generated method stub
		return button;
	}

	@Override
	public TextView getTextView() {
		// TODO Auto-generated method stub
		return textView;
	}

	@Override
	public void beep(long n) {
		// TODO Auto-generated method stub
		vibrator.vibrate(n);
	}

	@Override
	public void showDialog(final JSONObject json) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				alertView.show();
				try {
					alertView.setMessage(String.format(
							getString(R.string.dialogMsg), " ", " ",
							json.getString("pid")));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private class AlertView extends ProgressDialog {

		@SuppressWarnings("deprecation")
		protected AlertView() {
			super(MainActivity.this);

			setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			setTitle(R.string.dialogTitle);
			setMessage(String.format(getString(R.string.dialogMsg), " ", " ",
					" "));
			setMax(10);
			setButton(getString(R.string.grap), grapListener);
			setButton2(getString(R.string.reject), rejectListener);
			setProgress(10);
			setCancelable(false);
		}

		DialogInterface.OnClickListener grapListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				socket.grap();
			}
		};
		DialogInterface.OnClickListener rejectListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				socket.reject();
			}
		};
	}

	@Override
	public void setTimer(int n) {
		// TODO Auto-generated method stub
		alertView.setProgress(n);
	}

	@Override
	public void closeDialog() {
		// TODO Auto-generated method stub
		alertView.dismiss();
	}

}
