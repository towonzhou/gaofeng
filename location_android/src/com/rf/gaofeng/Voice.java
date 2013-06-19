package com.rf.gaofeng;

import android.content.Context;

import com.iflytek.speech.SynthesizerPlayer;

public class Voice {
    SynthesizerPlayer player = null;

    public Voice(Context context) {
	// TODO 自动生成的构造函数存根

	player = SynthesizerPlayer.createSynthesizerPlayer(context,
		"appid=51527f39");
	player.setVoiceName("xiaoyan");

    }

    public void play(String message) {
	player.playText(message, null, null);
    }
}
