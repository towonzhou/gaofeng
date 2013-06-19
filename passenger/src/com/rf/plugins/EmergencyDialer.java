package com.rf.plugins;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class EmergencyDialer extends CordovaPlugin {
    private static final int PHONE_CALL = 0;     // 拨打电话
    private CallbackContext callbackContext;

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        String result = "";
        try {
            this.callbackContext = callbackContext;

            if (action.equals("dial")) {
                String number = "tel:" + args.getString(0);
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                Log.e("Passenger", "start dial");
                this.cordova.getActivity().startActivity(callIntent);
                callbackContext.success();
                return true;

            } else {
                Log.e("Passenger", "invalid dial");
                result = "invalid call";
            }

            callbackContext.error(result);
            return false;

        } catch(Exception e) {
            callbackContext.error(e.getMessage());
            return false;
        }
    }
}
