package com.redflag.taximate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * display a map with all restaurants nearby
 */
public class MainActivity extends Activity {
	public static final String mapKey = "DBF7401BD57A70BC16FC4E003D1285A1FC0F32CF";
	public static final String TAG = "com.redflag.taximate";

	private BMapManager _mapMan;
	private MKOfflineMap _Offline;
	private MapView _map;
	private LocationClient _locationClient;

	private class LocationOverlay extends MyLocationOverlay {
		private String _info;
		private Context _context;

		public LocationOverlay(MapView mapView, String info) {
			super(mapView);
			this._info = info;
			this._context = mapView.getContext();
		}

		@Override
		protected boolean dispatchTap() {
			debug("dispatchTap");
			Toast.makeText(this._context, this._info, Toast.LENGTH_LONG).show();
			return true;
		}
	}

	private class RestaurantPopupItem extends OverlayItem {
		private MKPoiInfo _poi;

		public RestaurantPopupItem(MKPoiInfo poi) {
			super(poi.pt, poi.name, poi.address);
			_poi = poi;
		}

		public MKPoiInfo getPoi() {
			return _poi;
		}
	}

	private class RestaurantIconOverlay extends
			ItemizedOverlay<RestaurantPopupItem> {
		private PopupOverlay _popup;

		public RestaurantIconOverlay(Drawable marker, MapView map) {
			super(marker, map);
		}

		@Override
		protected boolean onTap(int i) {
			debug("onTap: " + i);
			RestaurantPopupItem item = (RestaurantPopupItem) getItem(i);
			MKPoiInfo info = item.getPoi();
			Toast.makeText(getApplicationContext(), info.name,
					Toast.LENGTH_LONG).show();
			_map.getController().animateTo(info.pt);

			_popup = new PopupOverlay(_map, new PopupClickListener() {
				@Override
				public void onClickedPopup(int i) {
					debug("onClickedPopup " + i);
					_popup.hidePop();

					// TODO: setup swipe right to close it
					Intent details = new Intent(MainActivity.this,
							RestaurantDetailsActivity.class);
					startActivity(details);
				}
			});

			Bitmap bm = null;
			try {
				bm = BitmapFactory.decodeStream(MainActivity.this.getAssets()
						.open("imgs/restaurant.png"));
			} catch (IOException e) {
				debug("read bitmap failed: " + e.getMessage());
			}

			_popup.showPopup(bm, info.pt, 32);

			return true;
		}
	}

	public static void debug(String msg) {
		if (msg == null) {
			Log.d(TAG, "msg is null");
			return;
		}

		Log.d(TAG, msg);
	}

	private void setupMap() {
		_map = (MapView) findViewById(R.id.mapView);
		debug("touch mode: " + _map.isInTouchMode());

		_map.setBuiltInZoomControls(true);
		MapController mMapController = _map.getController();
		mMapController.setZoom(15);// 设置地图zoom级别
		mMapController.enableClick(true);

		_Offline = new MKOfflineMap();
		_Offline.init(mMapController, new MKOfflineMapListener() {
			@Override
			public void onGetOfflineMapState(int type, int state) {
				switch (type) {
				case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
					MKOLUpdateElement update = _Offline.getUpdateInfo(state);
					debug(String.format("%s : %d%%", update.cityName,
							update.ratio));
					if (update.ratio >= 100) {
						_Offline.scan();
					}
				}
					break;

				case MKOfflineMap.TYPE_NEW_OFFLINE:
					debug(String.format("add offlinemap num: %d", state));
					break;

				case MKOfflineMap.TYPE_VER_UPDATE:
					debug(String.format("new offlinemap ver"));

					break;

				default:
					debug("MKOfflineMapListener unkown type");
				}
			}
		});

		ArrayList<MKOLUpdateElement> updates = _Offline.getAllUpdateInfo();
		for (MKOLUpdateElement update : updates) {
			debug(String.format("update info: %s:%d", update.cityName,
					update.ratio));
		}

		int num = _Offline.scan();
		if (num > 0) {
			debug("scanned offline _map " + num);
		}
		GeoPoint point = new GeoPoint((int) (39.915 * 1E6),
				(int) (116.404 * 1E6));
		_map.getController().setCenter(point);// 设置地图中心点

		_locationClient = new LocationClient(getApplicationContext());
		_locationClient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation bdLocation) {
				debug(String.format("onReceiveLocation: locType %d",
						bdLocation.getLocType()));

				switch (bdLocation.getLocType()) {
				case 68: // OfflineLocaiton
					debug("OfflineLocaiton");
					break;
				case 61: // GPS
				case 161: // network
					debug("locate correctly");

					LocationOverlay myLocationOverlay = new LocationOverlay(
							_map, bdLocation.getAddrStr());
					LocationData data = new LocationData();
					data.latitude = bdLocation.getLatitude();
					data.longitude = bdLocation.getLongitude();
					data.accuracy = bdLocation.getRadius();
					data.direction = bdLocation.getDerect();
					myLocationOverlay.setData(data);
					myLocationOverlay.enableCompass();

					_map.getOverlays().clear();
					_map.getOverlays().add(myLocationOverlay);
					_map.refresh();

					GeoPoint p = new GeoPoint(
							(int) (bdLocation.getLatitude() * 1E6),
							(int) (bdLocation.getLongitude() * 1E6));
					_map.getController().animateTo(p);
					Toast.makeText(getApplicationContext(),
							bdLocation.getAddrStr(), Toast.LENGTH_LONG).show();

					// TODO: for the sake of accuracy, we may need amount of
					// iterations
					_locationClient.requestPoi();

					break;

				default:
					_locationClient.stop();
					debug("locate failed");
				}

			}

			@Override
			public void onReceivePoi(BDLocation bdLocation) {
				debug("onReceivePoi: " + bdLocation.hasPoi());
				if (!bdLocation.hasPoi()) {
					_locationClient.stop();
					return;
				}

				try {
					JSONObject json = new JSONObject(bdLocation.getPoi());
					JSONArray pois = json.getJSONArray("p");

					Drawable marker = getResources().getDrawable(
							R.drawable.restaurant);
					RestaurantIconOverlay po = new RestaurantIconOverlay(
							marker, _map);
					List<OverlayItem> items = new ArrayList<OverlayItem>();

					for (int i = 0; i < pois.length(); ++i) {
						JSONObject o = pois.getJSONObject(i);
						debug("POI: " + o.toString());

						MKPoiInfo info = new MKPoiInfo();
						info.address = o.optString("addr");
						info.phoneNum = o.optString("tel");
						info.name = o.optString("name");
						info.pt = new GeoPoint((int) (o.getDouble("y") * 1E6),
								(int) (o.getDouble("x") * 1E6));
						info.uid = String.valueOf(i);
						OverlayItem oi = new RestaurantPopupItem(info);
						items.add(oi);
					}

					_map.getOverlays().add(po);
					po.addItem(items);
					_map.refresh();

				} catch (JSONException e) {
					debug(e.getMessage());

				} finally {
					_locationClient.stop();
				}

			}
		});

		LocationClientOption locOpts = new LocationClientOption();
		locOpts.setOpenGps(true);

		locOpts.setAddrType("all");// 返回的定位结果包含地址信息
		locOpts.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		locOpts.setScanSpan(900); // make it one-shot locating
		locOpts.disableCache(true);// 禁止启用缓存定位
		locOpts.setPoiNumber(5); // 最多返回POI个数
		locOpts.setPoiDistance(1000); // poi查询距离
		locOpts.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
		locOpts.setPriority(LocationClientOption.GpsFirst);

		_locationClient.setLocOption(locOpts);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.locate:
			onLocate();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onLocate() {
		if (!_locationClient.isStarted()) {
			_locationClient.start();
		}
		// TODO: needs to check if a locating is currently undergoing
		_locationClient.requestLocation();
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_mapMan = new BMapManager(getApplication());
		_mapMan.init(mapKey, null);
		setContentView(R.layout.main);

		String locale = getResources().getConfiguration().locale
				.getDisplayName();
		debug("locale: " + locale);
		setupMap();
	}

	@Override
	protected void onStart() {
		super.onStart();
		debug("onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		debug("onResume");

		_map.onResume();
		if (_mapMan != null) {
			_mapMan.start();
		}

		if (!_locationClient.isStarted()) {
			_locationClient.start();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		debug("onPause");

		_map.onPause();
		if (_mapMan != null) {
			_mapMan.stop();
		}

		if (isFinishing()) {
			debug("isFinishing");
			if (_locationClient != null && _locationClient.isStarted()) {
				_locationClient.stop();
			}

		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		debug("onStop");

		if (_locationClient.isStarted()) {
			_locationClient.stop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		debug("onDestroy");

		_Offline.destroy();
		_map.destroy();
		if (_mapMan != null) {
			_mapMan.destroy();
			_mapMan = null;
		}

		// System.exit(0);
	}

}
