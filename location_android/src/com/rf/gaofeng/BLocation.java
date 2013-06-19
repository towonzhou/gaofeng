package com.rf.gaofeng;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class BLocation {

	private LocationClient mLocationClient = null;
	private MapView mMapView = null;
	// private Voice voice = null;
	private Handler handler = null;
	private BDLocationListener myListener = new MyLocationListener();
	private LocationClientOption option = null;

	public BLocation(Context context, Handler handler) {
		// TODO 自动生成的构造函数存根
		mLocationClient = new LocationClient(context);
		mLocationClient.registerLocationListener(myListener);
		// voice = new Voice(context);
		this.handler = handler;

		option = new LocationClientOption();
		setOption(new Intent());
	}

	public void setOption(Intent data) {
		option.setOpenGps(data.getBooleanExtra("gps", true));
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(15000);// 设置发起定位请求的间隔时间为5000ms
		option.disableCache(!data.getBooleanExtra("cache", false));// 禁止启用缓存定位
		option.setPriority(data.getIntExtra("priority",
				LocationClientOption.GpsFirst));
		option.setPoiNumber(5); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
		mLocationClient.setLocOption(option);
	}

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mMapView == null)
				return;
			boolean flag = false;
			String string = "定位成功： ";

			MyLocationOverlay myLocationOverlay = new MyLocationOverlay(
					mMapView);
			LocationData locData = new LocationData();
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.accuracy = location.getRadius();
			locData.direction = location.getDerect();

			string = string + locData.longitude + "," + locData.latitude
					+ "\n精        度: " + location.getRadius() + "米"
					+ "\n定位类型: ";

			switch (location.getLocType()) {
			case 61:
				string = string + "GPS结果";
				flag = true;
				break;
			case 65:
				string = string + "缓存结果";
				flag = true;
				break;
			case 68:
				string = string + "网络链接失败，缓存结果";
				flag = true;
				break;
			case 161:
				string = string + "网络定位结果";
				flag = true;
				break;
			default:
				string = "定位失败";
				flag = false;
				break;
			}
			if (flag) {
				myLocationOverlay.setData(locData);
				mMapView.getOverlays().add(myLocationOverlay);
				myLocationOverlay.enableCompass();
				mMapView.refresh();
				mMapView.getController().animateTo(
						new GeoPoint((int) (locData.latitude * 1e6),
								(int) (locData.longitude * 1e6)));
				string = string + "\n位置: " + location.getAddrStr();

			}

			Message msg = new Message();
			msg.obj = string;
			if (handler != null) {
				handler.sendMessage(msg);
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

	public void start(MapView mv) {
		mMapView = mv;
		start();
	}

	public void start(Intent data) {
		setOption(data);
		start();
	}

	public void stop() {
		if (mLocationClient.isStarted()) {
			mLocationClient.stop();
		}
	}
}