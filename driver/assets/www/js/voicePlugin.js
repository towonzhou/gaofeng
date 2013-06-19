window.VoicePlugin = function(message) {
    if (typeof message !== "string") {
        message = "不确定，可能在中国";
    }
    var exec = cordova.require('cordova/exec');
    exec(function(data){
        console.log(data);
    },function(err){
        alert("Tel error: " + err);
    },"voicePlugin", "play" , [message]);
};
