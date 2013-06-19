package com.redflag.taximate;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class RestaurantDetailsActivity extends Activity {
    private class executeCallback implements Runnable {
        private JSONObject _data;

        public executeCallback(JSONObject json) {
            _data = json;
        }

        @Override
        public void run() {
            RestaurantDetailsActivity.this.debug("onPostExecute: " + _data.toString());
            WebView webView = (WebView)findViewById(R.id.webView);
            webView.loadUrl("javascript: doHostCallback(" + _data.toString() + ");");
        }
    }

    private class WebInterface {
        private Activity _activity;

        public WebInterface(Activity act) {
            _activity = act;
        }

        public void debug(String msg) {
            RestaurantDetailsActivity.this.debug(msg);
        }

        public void showAlert(String msg) {
            Toast.makeText(_activity, msg, Toast.LENGTH_SHORT).show();
        }

        public void loadRestaurant() {
            JSONObject json = new JSONObject();
            try {
                json.putOpt("name", "成都小吃");

            } catch (JSONException e) {
                RestaurantDetailsActivity.this.debug(e.getMessage());
            }

            runOnUiThread(new executeCallback(json));
        }
    }

    private void debug(String msg) {
        MainActivity.debug(msg);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_details);

        debug("RestaurantDetailsActivity created");
        WebView webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new WebInterface(this), "host");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                debug(consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ": " + consoleMessage.message());
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/restaurant.html");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * load restaurant data according to restaurant id
     *
     * @param id restaurant id
     * @return false if load failed
     */
    private boolean loadRestaurant(String id) {

        return true;
    }
}