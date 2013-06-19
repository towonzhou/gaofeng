package com.rf.gaofeng;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioGroup;

public class ASettingActivity extends Activity {
	private RadioGroup setting = null;
	private boolean network = true;
	private boolean passive = true;
	private boolean gps = true;
	private boolean lbs = true;
	private String provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent data = getIntent();
		setContentView(R.layout.asetting);
		setting = (RadioGroup) findViewById(R.id.loc_setting);
		network = data.getBooleanExtra("network", true);
		passive = data.getBooleanExtra("passive", true);
		gps = data.getBooleanExtra("gps", true);
		lbs = data.getBooleanExtra("lbs", true);
		provider = data.getStringExtra("provider");
		setup();
	}

	private void setup() {
		setting.getChildAt(0).setEnabled(network);
		setting.getChildAt(1).setEnabled(passive);
		setting.getChildAt(2).setEnabled(gps);
		setting.getChildAt(3).setEnabled(lbs);
		if (provider.equals("network")) {
			setting.check(R.id.loc_network);
		} else if (provider.equals("passive")) {
			setting.check(R.id.loc_passive);
		} else if (provider.equals("gps")) {
			setting.check(R.id.loc_gps);
		} else if (provider.equals("lbs")) {
			setting.check(R.id.loc_lbs);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();

			switch (setting.getCheckedRadioButtonId()) {
			case R.id.loc_network:
				intent.putExtra("provider", "network");
				break;
			case R.id.loc_passive:
				intent.putExtra("provider", "passive");
				break;
			case R.id.loc_gps:
				intent.putExtra("provider", "gps");
				break;
			case R.id.loc_lbs:
				intent.putExtra("provider", "lbs");
				break;
			}
			setResult(RESULT_OK, intent);
		}
		return super.onKeyDown(keyCode, event);
	}

	public void log(String string) {
		// TODO Auto-generated method stub
		Log.i("MyLog", string);
	}
}
