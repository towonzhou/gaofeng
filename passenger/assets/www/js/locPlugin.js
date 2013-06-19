window.locPlugin = function(onSuccess, onError) {
    var exec = cordova.require('cordova/exec');
    exec(function(data){
        var type = data.locType;
        if (type == 61 || type == 65 || type == 161) {
            onSuccess(data);
        } else {
            onError("location error code : " + type );
        }

    }, onError, "locPlugin", "get" , []);
};
