package com.rf.gaofeng;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import android.util.Log;

public class Socket extends SocketIO {
	private String url = null;
	private Queue<JSONObject> plist = new LinkedList<JSONObject>();
	private Driver driver = null;
	private Timer timer = null;
	private TimerTask task = null;

	public Socket(Driver driver, String url) throws MalformedURLException {
		super(url);
		this.driver = driver;
		timer = new Timer();
	}

	public Socket(Driver driver) throws MalformedURLException {
		this(driver, "http://gaofeng-server.nodejitsu.com/drivers");
		// this(driver, "http://172.16.82.23:9999");
	}

	private IOCallback callback = new IOCallback() {

		@Override
		public void onMessage(JSONObject jsonObject, IOAcknowledge arg1) {
			// TODO 自动生成的方法存根
			driver.log("JSON Message: " + jsonObject);
		}

		@Override
		public void onMessage(String data, IOAcknowledge arg1) {
			// TODO 自动生成的方法存根
			driver.log("String Message: " + data);
		}

		@Override
		public void onError(SocketIOException socketIOException) {
			// TODO 自动生成的方法存根
			socketIOException.printStackTrace();
		}

		@Override
		public void onDisconnect() {
			// TODO 自动生成的方法存根

		}

		@Override
		public void onConnect() {
			// TODO 自动生成的方法存根
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("did", "123456");
				jsonObject.put("dphone", driver.getPhoneNumber());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			emit("id", jsonObject);
			driver.alert("connected..........");
		}

		@Override
		public void on(String event, IOAcknowledge ack, Object... args) {
			// TODO 自动生成的方法存根

			if (event.equals("want taxi")) {
				startTimer();
				driver.beep(1000);
				JSONObject data = (JSONObject) args[0];
				driver.alert("one passenger....");
				if (plist.isEmpty()) {
					show(data);
				}
				plist.offer(data);
			} else if (event.equals("confirm taxi")) {
				stopTimer(task);
				driver.beep(1000);
				plist.poll();
				driver.setButton(false);
				driver.alert("success....");
			}
		}
	};

	private class Task extends TimerTask {
		int n = 0;

		public Task(int n) {
			// TODO Auto-generated constructor stub
			super();
			this.n = n;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (n > 0) {
				driver.log(String.format("还剩%1$d秒", n));
				driver.setTimer(n--);
			} else {
				stopTimer(this);
				driver.closeDialog();
				reject();
				cancel();
			}
		}
	}

	private void stopTimer(TimerTask task) {
		if (task != null) {
			task.cancel();
			timer.purge();
		}
	}

	private void startTimer() {
		stopTimer(task);
		task = new Task(10);
		timer.scheduleAtFixedRate(task, 0, 1000);
	}

	public void show(JSONObject data) {
		String name = null;

		try {
			name = data.getString("pid");
			driver.showDialog(data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.setButton(true);
		driver.setTextView(R.string.pinfo, "", name);
		startTimer();
	}

	public void show() {
		show(plist.peek());
	}

	public void setURL(String url) {
		this.url = url;
	}

	public String getURL() {
		return this.url;
	}

	public Boolean connect() {
		super.connect(callback);
		return true;
	}

	public boolean grap() {
		emit("provide taxi", plist.peek());
		return true;
	}

	public boolean reject() {
		emit("reject taxi", plist.poll());
		stopTimer(task);
		driver.setButton(false);
		driver.setTextView("");
		driver.alert("reject taxi");
		if (!plist.isEmpty()) {
			show();
		}
		return true;
	}

	public void log(String msg) {
		Log.i("MyLog", msg);
	}
}
