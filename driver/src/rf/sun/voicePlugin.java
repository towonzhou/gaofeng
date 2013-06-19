package rf.sun;

import android.os.Bundle;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.iflytek.speech.SpeechError;
import com.iflytek.speech.SynthesizerPlayer;
import com.iflytek.speech.SynthesizerPlayerListener;
import com.iflytek.ui.SynthesizerDialog;

public class voicePlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("play".equals(action)) {
            String message = args.getString(0);
            SynthesizerPlayer player = SynthesizerPlayer.createSynthesizerPlayer(cordova.getActivity(),"appid=51527f39");
            player.setVoiceName("xiaoyan");
            player.playText(message, null, null);
            callbackContext.success("OK");

            return true;
        }
        return false;  // Returning false results in a "MethodNotFound" error.
    }
}
