package com.rf.gaofeng;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocalService extends Service {

	public LocalService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
