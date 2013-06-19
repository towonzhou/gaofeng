package com.rf.gaofeng;

import org.json.JSONObject;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

public interface Driver {
	public void setTextView(String string);

	public void setTextView(int id, String... args);

	public void setButton(Boolean flag);

	public String getPhoneNumber();

	public void alert(String string);

	public void log(String string);

	public Context getContext();

	public Button getButton();

	public TextView getTextView();

	public void beep(long n);

	public void showDialog(JSONObject json);

	public void setTimer(int n);
	
	public void closeDialog();
}
