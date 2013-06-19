package com.redflag.Passenger;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

//FIXME: replace all urls with really server address
public class LoginActivity extends Activity {
    private boolean _userLoggedIn = false;
    public static final String TAG = "com.redflag.Passenger";
    public static final String LOGIN_PAGE_URL = "http://172.16.82.62:3000/users/signin";

    private class LoginWebClient extends WebViewClient {
        //FIXME: HACK: POST request also calls onPageStarted, and I can not tell from get,
        //so use afterPost to indicate
        private boolean afterPost = false;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            debug("shouldOverrideUrlLoading: " + url);
            if (uri.getHost().startsWith("172.16.82.62")) {
                afterPost = true;
                return false;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Uri uri = Uri.parse(url);
            debug("onPageStarted: " + url + ", path: " + uri.getPath());
            debug("origin: " + view.getOriginalUrl());

            if (afterPost && view.getOriginalUrl().equals(LOGIN_PAGE_URL)) {
                debug("force stop and finish");
                view.stopLoading();
                if (uri.getPath().equals("/")) {
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            }
        }
    }
    private void debug(String msg) {
        if (msg == null) {
            Log.d(TAG, "msg is null");
            return;
        }

        Log.d(TAG, msg);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    @Override
    protected void onStart() {
        super.onStart();
        WebView webView = (WebView)findViewById(R.id.loginView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new LoginWebClient());
        webView.loadUrl(LOGIN_PAGE_URL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        debug("LoginActivity:onPause");
        if (isFinishing()) {
            debug("LoginActivity:isFinishing");
            WebView webView = (WebView)findViewById(R.id.loginView);
            webView.stopLoading();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        debug("LoginActivity:onDestroy");
    }

    public void doLogin() {

    }

    public void doPassiveLogin() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... urls) {
                    try {
                        CookieManager cookieManager = new CookieManager();
                        CookieHandler.setDefault(cookieManager);

                        URL url = new URL(urls[0]);
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setChunkedStreamingMode(0);
                        conn.setConnectTimeout(30000);
                        conn.connect();

                        BufferedOutputStream ostream = new BufferedOutputStream(conn.getOutputStream());
                        //FIXME: pass in username and password
                        ostream.write("_csrf=&device=0&username=sonald&password=abc".getBytes());
                        ostream.flush();
                        debug("response: " + conn.getResponseCode());
                        ostream.close();
                        conn.disconnect();

                        _userLoggedIn = true;
                        if (urls.length == 1) {
                            return null;
                        }

                        //Fetch data
                        url = new URL(urls[1]);
                        conn = (HttpURLConnection)url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(30000);
                        conn.connect();

                        debug("response: " + conn.getResponseCode());

                        BufferedInputStream istream = new BufferedInputStream(conn.getInputStream());
                        InputStreamReader reader = new InputStreamReader(istream, "UTF-8");
                        char[] buf = new char[128];
                        int len = reader.read(buf);
                        debug("read: " + new String(buf, 0, len));
                        istream.close();
                        conn.disconnect();

                    } catch (MalformedURLException e) {
                        debug("MalformedURLException: " + e.getMessage());

                    } catch (IOException e) {
                        debug(e.getMessage());

                    }

                    return null;
                }
            }.execute("http://172.16.82.62:3000/users/signin", "http://172.16.82.62:3000/");


        } else {
            debug("onLogin: offline now");
        }
    }
}