package com.redflag.Passenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.*;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import io.socket.SocketIO;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements PassengerResponse {
    private static final String mapKey = "DBF7401BD57A70BC16FC4E003D1285A1FC0F32CF";
    public static final String TAG = "com.redflag.Passenger";
    public static final String EXTRA_LOGS = "com.redflag.Passenger.LOGS";
    public static final int REQ_LOGIN = 100;

    private MapView _map = null;
    private MKOfflineMap _Offline = null;
    private BMapManager _mapMan = null;
    private LocationClient _locationClient = null;

    private ArrayList<String> _logs = new ArrayList<String>();

    private ServerAdaptor serverAdaptor = null;
    private String _dphone = null;

    private GoogleAnalytics _googleAnalytics;
    private Tracker _tracker;

    public class LocationOverlay extends MyLocationOverlay {
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

    private class MyWorker implements Runnable {
        private int resid;
        private boolean state;

        public MyWorker(boolean state, int resid) {
            this.resid = resid;
            this.state = state;
        }

        @Override
        public void run() {
            Button btn = (Button)findViewById(R.id.btnCallTaxi);
            btn.setText(resid);

            TextView textView = (TextView)findViewById(R.id.textView);
            textView.setText(resid);
            btn.setEnabled(state);
        }
    }

    private class WaitTaxiCountDown extends CountDownTimer {
        private long _remaining;
        private CountDownDialog _countDownDialog;

        public WaitTaxiCountDown(long millisInFuture) {
            super(millisInFuture, 1000);
            _remaining = millisInFuture;
//            showDialog(0);
            _countDownDialog = new CountDownDialog();
            _countDownDialog.show(getFragmentManager(), "countdown");
        }

        @Override
        public void onTick(long remaining) {
            Button btn = (Button) findViewById(R.id.btnCallTaxi);
            btn.setText(String.format(" %d ", remaining / 1000));
            _remaining = remaining - 1000; // next round remaining

            Dialog dlg = _countDownDialog.getDialog();
            if (dlg != null) {

//                ProgressBar progressBar = (ProgressBar)dlg.findViewById(R.id.progressBar);
//                progressBar.setProgress((int)remaining/1000);
                TextView textView = (TextView)dlg.findViewById(R.id.textView);
                textView.setText("timeout in " + remaining/1000 + " seconds");
            }

            debug("WaitTaxiCountDown remaining " + remaining/1000);
        }

        @Override
        public void onFinish() {
            debug("WaitTaxiCountDown finished, _remaining " + _remaining);
            if (_remaining < 1000) {
                //meaning timeout
                onBeingRejectACar();
            }
            _countDownDialog.dismiss();
        }
    }

    WaitTaxiCountDown _waitTaxiCountDown = null;

    private class CountDownDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.countdown, null));
            return builder.create();
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        debug("create mainactivity");

        _googleAnalytics = GoogleAnalytics.getInstance(this);
        _googleAnalytics.setDebug(true);
        _tracker = _googleAnalytics.getTracker(getString(R.string.ga_trackingId));

        _mapMan = new BMapManager(getApplication());
        _mapMan.init(mapKey, null);
        setContentView(R.layout.main);

        try {
            serverAdaptor = new ServerAdaptor(this);
        } catch(Exception e) {
            debug(e.getMessage());
        }

        setupMap();
    }

    private void debug(String msg) {
        if (msg == null) {
            Log.d(TAG, "msg is null");
            return;
        }

        Log.d(TAG, msg);
        _logs.add(msg);
    }

    private void setupMap() {
        _map = (MapView) findViewById(R.id.bmapView);
        debug("touch mode: " + _map.isInTouchMode());

        _map.setBuiltInZoomControls(true);
        MapController mMapController = _map.getController();
        mMapController.setZoom(15);//设置地图zoom级别
        mMapController.enableClick(true);


        _Offline = new MKOfflineMap();
        _Offline.init(mMapController, new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int type, int state) {
                switch (type) {
                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                        MKOLUpdateElement update = _Offline.getUpdateInfo(state);
                        debug(String.format("%s : %d%%", update.cityName, update.ratio));
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
            debug(String.format("update info: %s:%d", update.cityName, update.ratio));
        }

        int num = _Offline.scan();
        if (num > 0) {
            debug("scanned offline _map " + num);
        }
        GeoPoint point = new GeoPoint((int)(39.915* 1E6),(int)(116.404* 1E6));
        _map.getController().setCenter(point);//设置地图中心点

        _locationClient = new LocationClient(getApplicationContext());
        _locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                debug(String.format("onReceiveLocation: locType %d", bdLocation.getLocType()));

                switch (bdLocation.getLocType()) {
                    case 61: // GPS
                    case 161: // network
                        debug("locate correctly");

                        LocationOverlay myLocationOverlay = new LocationOverlay(_map, bdLocation.getAddrStr());
                        LocationData data = new LocationData();
                        data.latitude = bdLocation.getLatitude();
                        data.longitude = bdLocation.getLongitude();
                        myLocationOverlay.setData(data);

                        _map.getOverlays().clear();
                        _map.getOverlays().add(myLocationOverlay);
                        _map.refresh();

                        GeoPoint p = new GeoPoint((int) (bdLocation.getLatitude() * 1E6),
                                (int) (bdLocation.getLongitude() * 1E6));
                        _map.getController().animateTo(p);
                        Toast.makeText(getApplicationContext(), bdLocation.getAddrStr(), Toast.LENGTH_LONG).show();

                        _locationClient.stop();
                        break;

                    default:
                        debug("locate failed");
                }

            }

            @Override
            public void onReceivePoi(BDLocation bdLocation) {
                debug("onReceivePoi");
            }
        });

        LocationClientOption locOpts = new LocationClientOption();
        locOpts.setOpenGps(true);

        locOpts.setAddrType("all");//返回的定位结果包含地址信息
        locOpts.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        locOpts.setScanSpan(3000);
        locOpts.disableCache(true);//禁止启用缓存定位
        locOpts.setPoiNumber(5);	//最多返回POI个数
        locOpts.setPoiDistance(1000); //poi查询距离
        locOpts.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息

        _locationClient.setLocOption(locOpts);
    }

    public void onLogin(View view) {
        debug("onLogin");
        Intent login = new Intent(this, LoginActivity.class);
        startActivityForResult(login, REQ_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQ_LOGIN) {
            return;
        }

        if (resultCode != RESULT_OK) {
            debug("login failed");
            return;
        }

        debug("login success");
    }

    public void onShowLogs(View view) {
        _tracker.sendEvent("ui_action", "button_press", "show_logs", 0L);

        Intent logIntent = new Intent(this, LogActivity.class);
        logIntent.putExtra(EXTRA_LOGS, _logs);
        startActivity(logIntent);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onLocate(View view) {
        _tracker.sendEvent("ui_action", "button_press", "locate", 0L);

        debug("onLocate");

        if (_locationClient != null) {

            if (!_locationClient.isStarted()) {
                _locationClient.start();

            }
            _locationClient.requestLocation();
        } else {
            debug("_locationClient is null or not started");
        }
    }

    public void onCallTaxi(View view) {
        _tracker.sendEvent("ui_action", "button_press", "call_taxi", 0L);

        debug("onCallTaxi");

        notifyState();
        Button btn = (Button)findViewById(R.id.btnCallTaxi);
        btn.setText(R.string.btnCallTaxi_title_wait);
        btn.setEnabled(false);

        if (isSocketHealthy()) {
            SocketIO sock = serverAdaptor.getSocket();
            sock.emit("want taxi");
        }

        if (_waitTaxiCountDown != null) {
            _waitTaxiCountDown.cancel();
            _waitTaxiCountDown = null;
        }
        _waitTaxiCountDown = new WaitTaxiCountDown(10000);
        _waitTaxiCountDown.start();
    }

    public void onCallDriver(View view) {
        _tracker.sendEvent("ui_action", "button_press", "call_driver", 0L);

        debug("onCallDriver");
        //TODO: check  && state == STATE.NORMAL
        if (_dphone != null) {
            Uri uri = Uri.parse("tel:" + _dphone);
            Intent callPhone = new Intent(Intent.ACTION_CALL, uri);
            PackageManager pm = getPackageManager();
            List<ResolveInfo> infos = pm.queryIntentActivities(callPhone, 0);
            if (infos.size() > 0) {
                startActivity(callPhone);
            } else {
                debug("can not call phone");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        debug("onStop");

        if (serverAdaptor != null ) {
            serverAdaptor.disconnect();
        }

        if (_waitTaxiCountDown != null) {
            _waitTaxiCountDown.cancel();
            _waitTaxiCountDown = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        debug("onDestroy");

        _tracker.close();
        _map.destroy();
        if(_mapMan != null){
            _mapMan.destroy();
            _mapMan = null;
        }

        System.exit(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        debug("onPause");

        _map.onPause();
        if(_mapMan != null) {
            _mapMan.stop();
        }

        if (isFinishing()) {
            debug("isFinishing");
            if (_locationClient != null && _locationClient.isStarted()) {
                _locationClient.stop();
            }

            if (isSocketHealthy()) {
                serverAdaptor.disconnect();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        debug("onResume");

        _map.onResume();
        if(_mapMan != null ) {
            _mapMan.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        debug("onStart");

        _tracker.sendView("Main");

    }

    // PassengerResponse implementations
    public void sendPID() {
        if (!isSocketHealthy()) {
            debug("sendPID: socket dies");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("pid", randomPid());
            debug("sendPID: " + json.toString());
            serverAdaptor.getSocket().emit("id", json);

        } catch (JSONException e) {
            debug(e.getMessage());
        }
    }

    public void onGotACar(JSONObject json) {
        debug("onGotACar: dphone " + json.optString("dphone"));
        _dphone = json.optString("dphone");

         Handler handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message inputMessage) {
                TextView textView = (TextView)findViewById(R.id.textView);
                textView.setText("driver telephone: " + _dphone);
            }
        };

        Message completeMessage =
                handler.obtainMessage(0, null);
        completeMessage.sendToTarget();

        onConfirmTaxi();
    }

    public void onBeingRejectACar() {
        debug("onBeingRejectACar");
        if (_waitTaxiCountDown != null) {
            _waitTaxiCountDown.cancel();
            _waitTaxiCountDown = null;
        }

        runOnUiThread(new MyWorker(true, R.string.btnCallTaxi_title_reject));
    }

    public void onConfirmTaxi() {
        if (!isSocketHealthy()) {
            debug("onConfirmTaxi: socket dies");
            return;
        }

        debug("confirm taxi");
        serverAdaptor.getSocket().emit("confirm taxi");

        if (_waitTaxiCountDown != null) {
            _waitTaxiCountDown.cancel();
            _waitTaxiCountDown = null;
        }

        runOnUiThread(new MyWorker(true, R.string.btnCallTaxi_title_done));
        debug("confirm done");
    }

    public void onError(Exception e) {
        debug("onError: " + e.getMessage());
        //TODO: terminate transaction nicely
        if (_waitTaxiCountDown != null) {
            _waitTaxiCountDown.cancel();
            _waitTaxiCountDown = null;
        }

        runOnUiThread(new MyWorker(true, R.string.btnCallTaxi_title_reject));
    }

    public STATE state = STATE.NORMAL;
    public void startTransaction() {
        state = STATE.WANTING_TAXI;
    }

    public void endTransaction() {
        state = STATE.NORMAL;
    }

    public void rollbackTransaction() {

    }

    private String randomPid() {
        String alpha = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(5);
        for(int i = 0; i < 5; ++i) {
            int id = (int)Math.floor(Math.random() * alpha.length());
            sb.append(alpha.charAt(id));
        }
        return sb.toString();
    }

    private boolean isSocketHealthy() {
        return serverAdaptor != null && serverAdaptor.getSocket() != null && serverAdaptor.getSocket().isConnected();
    }

    private void notifyState() {
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final Ringtone ringtone = RingtoneManager.getRingtone(this, defaultUri);
        if (ringtone == null) {
            debug("can not find ringtone");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ringtone.play();
                debug("after play ringtone");
            }
        }).start();
    }
}
