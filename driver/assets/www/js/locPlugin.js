window.LocPlugin = function(callback) {
    var exec = cordova.require('cordova/exec');
    exec(function(data){
        var type = data.locType;
        if(type == 61 || type == 65 || type == 161){
            callback(data);
        }else{
            alert("location error code : " + type );
        }
    },function(err){
        alert("location error: " + err);
    },"locPlugin", "get" , []);
};

var position = function() {
    window.LocPlugin(function(pos) {
        driverMap.myinfo.setTitle("<font color=blue><h3><b>我的信息：</b></h3></font>" + pos.addrStr);
        driverMap.myinfo.point = new BMap.Point(pos.longitude, pos.latitude);
        map.openInfoWindow(driverMap.myinfo, driverMap.myinfo.point);
        map.panTo(driverMap.myinfo.point);
        driverMap.grap.hide();
        window.VoicePlugin(pos.addrStr);
    });
}
