package com.rf.gaofeng;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class Location {

	private LocationClient mLocationClient = null;
	private MapView mMapView = null;
	private Voice voice = null;
	private BDLocationListener myListener = new MyLocationListener();
	private Driver driver = null;

	public Location(Driver driver) {
		// TODO 自动生成的构造函数存根
		this.driver = driver;
		mLocationClient = new LocationClient(driver.getContext());
		mLocationClient.registerLocationListener(myListener);
		voice = new Voice(driver.getContext());

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.disableCache(true);// 禁止启用缓存定位
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

			String address = location.getDistrict()
					+ location.getStreet() + location.getStreetNumber();
			voice.play(address);
			driver.setTextView(R.string.info, address, driver.getPhoneNumber());

			MyLocationOverlay myLocationOverlay = new MyLocationOverlay(
					mMapView);
			LocationData locData = new LocationData();

			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.accuracy = location.getRadius();
			locData.direction = location.getDerect();
			myLocationOverlay.setData(locData);
			mMapView.getOverlays().add(myLocationOverlay);
			myLocationOverlay.enableCompass();
			mMapView.refresh();
			mMapView.getController().animateTo(
					new GeoPoint((int) (locData.latitude * 1e6),
							(int) (locData.longitude * 1e6)));

			stop();
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	public void start(MapView mv) {
		mMapView = mv;

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
}