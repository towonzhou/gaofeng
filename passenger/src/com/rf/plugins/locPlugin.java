package com.rf.plugins;

import android.os.Bundle;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类

public class locPlugin extends CordovaPlugin {
    public LocationClient mLocationClient = null;
    public MyLocationListener myListener = new MyLocationListener();
    private  JSONObject jsonObj = new JSONObject();
    private  CallbackContext callback = null;

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (callback == null)
                return ;

            if (location == null)
                return ;
            try {
                jsonObj.put("locType", location.getLocType());
                jsonObj.put("latitude", location.getLatitude());
                jsonObj.put("longitude", location.getLongitude());
                jsonObj.put("hasRadius", location.hasRadius());
                jsonObj.put("radius", location.getRadius());
                jsonObj.put("addrStr", location.getAddrStr());
            } catch (JSONException e) {
                return ;
            }

            callback.success(jsonObj);
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    public void onDestroy(){
        if (mLocationClient != null && mLocationClient.isStarted()){
            mLocationClient.stop();
            mLocationClient = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("get".equals(action)) {

            callback = callbackContext;

            mLocationClient = new LocationClient(cordova.getActivity());

            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);
            option.setAddrType("all");//返回的定位结果包含地址信息
            option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
            option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
//            option.disableCache(true);//禁止启用缓存定位
            option.setProdName("BaiduLoc");
            option.setPoiNumber(5);//最多返回POI个数
            option.setPoiDistance(1000); //poi查询距离
            option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息
            mLocationClient.setLocOption(option);

            mLocationClient.registerLocationListener( myListener );
            mLocationClient.start();
            if (mLocationClient != null && mLocationClient.isStarted()){
                mLocationClient.requestLocation();
            }

            return true;
        }
        return false;  // Returning false results in a "MethodNotFound" error.
    }
}
