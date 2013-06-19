package com.rf.gaofeng;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.maps.LocationSource;

public class ALocation implements LocationSource, AMapLocationListener {
	private LocationManagerProxy mAMapLocationManager = null;
	private OnLocationChangedListener mListener;
	private Context context = null;
	private String provider = "lbs";
	private Handler handler = null;

	public ALocation(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		mAMapLocationManager = LocationManagerProxy.getInstance(context);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		if (mListener != null) {
			mListener.onLocationChanged(location);
		}
		if (location != null) {
			Double geoLat = location.getLatitude();
			Double geoLng = location.getLongitude();
			String desc = "";
			Bundle locBundle = location.getExtras();
			if (locBundle != null) {
				desc = locBundle.getString("desc");
			}
			String str = ("定位成功: (" + geoLng + "," + geoLat + ")"
					+ "\n精        度: " + location.getAccuracy() + "米"
					+ "\n定位策略: " + location.getProvider() + "\n位置: " + desc);
			Message msg = new Message();
			msg.obj = str;
			if (handler != null) {
				handler.sendMessage(msg);
			}
		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(context);
		}
		if (provider == null) {
			provider = "lbs";
		}
		log("定位策略:" + provider);
		mAMapLocationManager.requestLocationUpdates(provider, 10, 15000, this);
	}

	@Override
	public void deactivate() {
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
		log("alocation deactivate");
	}
	
	public boolean isProviderEnabled(String provider) {
		return mAMapLocationManager.isProviderEnabled(provider);
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public void removeLocation() {
		mAMapLocationManager.removeUpdates(this);
	}

	public void log(String list) {
		// TODO Auto-generated method stub
		Log.i("MyLog", list);
	}
}
