package com.rf.gaofeng;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKOfflineMap;
import com.baidu.mapapi.map.MKOfflineMapListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.rf.gaofeng.R;

import android.support.v4.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;

public class Map extends FragmentActivity implements LocationSource,
		AMapLocationListener, BDLocationListener {
	private AMap aMap;
	private UiSettings uiSettings;
	private BMapManager mBMapMan = null;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private MapView mMapView = null;
	private MKOfflineMap mOffline = null;
	private MapController mapController = null;
	private LocationClient mLocationClient = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());

		mBMapMan.init("BC2D03E62D8E23C4CA1287DCA0A00BE312EBBBE2", null);
		setContentView(R.layout.map);
		aMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.amap)).getMap();
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.90403,
				116.407525), 15));

		uiSettings = aMap.getUiSettings();
		aMap.setLocationSource(this);
		aMap.setMyLocationEnabled(true);
		uiSettings.setCompassEnabled(true);

		mMapView = (MapView) findViewById(R.id.bmapsView);
		mOffline = new MKOfflineMap();
		mapController = mMapView.getController();
		mOffline.init(mapController, new MKOfflineMapListener() {
			@Override
			public void onGetOfflineMapState(int type, int state) {
				mOffline.scan();
			}
		});

		MapController mMapController = mMapView.getController();
		mMapController.setZoom(16);

		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.disableCache(false);// 禁止启用缓存定位
		option.setPriority(LocationClientOption.GpsFirst);
		option.setPoiNumber(5); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
		mLocationClient.setLocOption(option);

		mLocationClient.start();
		mLocationClient.requestLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		deactivate();
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
	public void onLocationChanged(AMapLocation aLocation) {
		// TODO Auto-generated method stub
		if (mListener != null) {
			mListener.onLocationChanged(aLocation);
		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
		}
		mAMapLocationManager.requestLocationUpdates(
				LocationProviderProxy.AMapNetwork, 5000, 10, this);
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		// TODO Auto-generated method stub
		MyLocationOverlay myLocationOverlay = new MyLocationOverlay(mMapView);
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
	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		mLocationClient.stop();
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onDestroy();
	}
}
