package com.redflag.Passenger;

import android.util.Log;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAdaptor implements IOCallback {
    public static final String TAG = "com.redflag.Passenger";
    private SocketIO _socket = null;
    private PassengerResponse _passgener;
//    private static final String SERVER_ADDR = "http://172.16.82.31:9999/passengers";
    private static final String SERVER_ADDR = "http://gaofeng-server.nodejitsu.com/passengers";

    public ServerAdaptor(PassengerResponse passenger) throws Exception {
        _socket = new SocketIO();
        _socket.connect(SERVER_ADDR, this);
        _passgener = passenger;
    }

    public SocketIO getSocket() {
        return _socket;
    }

    public void disconnect() {
        if (_socket != null) {
            _socket.disconnect();
            _socket = null;
        }
    }

    private void debug(String msg) {
        Log.d(TAG, msg);
    }

    @Override
    public void onMessage(JSONObject json, IOAcknowledge ack) {
        try {
            debug("Server said:" + json.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String data, IOAcknowledge ack) {
        debug("Server said: " + data);
    }

    @Override
    public void onError(SocketIOException socketIOException) {
        debug(socketIOException.getCause().getMessage());
        _passgener.onError(socketIOException);
    }

    @Override
    public void onDisconnect() {
        debug("Connection terminated.");
    }

    @Override
    public void onConnect() {
        debug("Connection established");
        _passgener.sendPID();

    }

    @Override
    public void on(String event, IOAcknowledge ack, Object... args) {
        Log.d(TAG, "Server triggered event '" + event + "'");

        if (event.equals("provided taxi")) {
            if (args.length == 0) {
                _passgener.onError(new Exception("taxi info is not provided"));
                return;
            }

            _passgener.onGotACar((JSONObject)args[0]);

        } else if (event.equals("rejected taxi")) {
            _passgener.onBeingRejectACar();
        }
    }

}
