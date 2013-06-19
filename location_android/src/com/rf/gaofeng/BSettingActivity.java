package com.rf.gaofeng;

import com.baidu.location.LocationClientOption;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class BSettingActivity extends Activity {
	private RadioGroup priority = null;
	private ToggleButton cache = null;
	private ToggleButton gps = null;
	private Intent data = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bsetting);
		data = getIntent();
		priority = (RadioGroup) findViewById(R.id.loc_priority);
		cache = (ToggleButton) findViewById(R.id.loc_cache);
		gps = (ToggleButton) findViewById(R.id.loc_gps);

		gps.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					priority.getChildAt(0).setEnabled(true);
					priority.getChildAt(1).setEnabled(true);
				} else {
					priority.getChildAt(0).setEnabled(false);
					priority.getChildAt(1).setEnabled(false);
					priority.check(R.id.loc_netWorkFirst);
				}
			}
		});
		setup();
	}

	private void setup() {
		int id = data.getIntExtra("priority", R.id.loc_netWorkFirst);
		cache.setChecked(data.getBooleanExtra("cache", true));
		gps.setChecked(data.getBooleanExtra("gps", true));
		priority.getChildAt(0).setEnabled(gps.isChecked());
		priority.getChildAt(1).setEnabled(gps.isChecked());
		priority.check(id);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(getApplicationContext(),
					BDMapActivity.class);

			switch (priority.getCheckedRadioButtonId()) {
			case R.id.loc_gpsFirst:
				intent.putExtra("priority", LocationClientOption.GpsFirst);
				break;
			case R.id.loc_netWorkFirst:
				intent.putExtra("priority", LocationClientOption.NetWorkFirst);
				break;
			}

			intent.putExtra("cache", cache.isChecked());
			intent.putExtra("gps", gps.isChecked());

			setResult(RESULT_OK, intent);
		}
		return super.onKeyDown(keyCode, event);
	}

	public void log(String string) {
		// TODO Auto-generated method stub
		Log.i("MyLog", string);
	}
}
