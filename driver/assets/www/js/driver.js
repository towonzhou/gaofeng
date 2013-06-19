var driver = {

    connect: function (url) {
        var socket = new io.connect(url);
        driver.socket = socket;
        socket.on('connect', function() {
            var telephoneNumber = cordova.require("cordova/plugin/telephonenumber");
            var phoneNumber = '000000000';
            telephoneNumber.get(function(result) {
                phoneNumber = result;
            }, function() {
                alert("error");
            });
            alert("connect");
            driverMap.myinfo.name = randomString(5);
            socket.emit('id', { did: driverMap.myinfo.name, dphone: phoneNumber });
            driverMap.myinfo.setContent('用户名： ' + driverMap.myinfo.name + '<br/> 手机号：' + phoneNumber);
            socket.on('want taxi', function (data) {
                alert("one passenger");
                var point = new BMap.Point(116.404, 39.915);
                var marker = driverMap.newMarker(point, data.pid);
                navigator.notification.beep(1);
                driverMap.addGrap();
            });
        })
    },

    grap: function (){
        var infoWindow = map.getInfoWindow();
        if (infoWindow === null && button.textContent === "定位"){
            position();
        } else {
            var socket = driver.socket;
            window.clearTimeout(infoWindow.timer);
            socket.emit("provide taxi", {pid: infoWindow.pid});
            socket.once('confirm taxi', function (data) {
                infoWindow.setContent("抢单成功!");
                navigator.notification.beep(1);
                infoWindow.visible = "hide";
                driverMap.grap.hide();
            });
        }
    },

    reject: function(pid){
        var socket = driver.socket;
        alert("send reject");
        socket.emit("reject taxi", {pid: pid});
    }
};
