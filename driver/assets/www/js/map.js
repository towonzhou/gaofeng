var driverMap = {

    init: function (div, opts) {
        
        var url = "http://gaofeng-server.nodejitsu.com/drivers";
        var pos;

        if ( opts === undefined ) {
            opts = {
                enableHighResolution: false
            };
        }
        driverMap.myinfo = new BMap.InfoWindow("", {
            enableCloseOnClick: false,
            title: "我的信息"
        });
        driverMap.myinfo.addEventListener('close', driverMap.rmGrap);

        driverMap.GrapControl.prototype = new BMap.Control();  
        driverMap.GrapControl.prototype.initialize = function(map){
            button = document.createElement("button");
            button.textContent = "抢单";
            button.addEventListener("click", driver.grap);
            button.style.fontSize = "45px"
            map.getContainer().appendChild(button); 
            return button;
        };

        driverMap.grap = new driverMap.GrapControl();
        driverMap.myinfo.point = new BMap.Point(116.404, 39.915);

        map = new BMap.Map(div, opts);
        map.disableDoubleClickZoom();
        map.centerAndZoom(driverMap.myinfo.point, 15);
        map.disableDoubleClickZoom();
        map.addControl(new BMap.NavigationControl());
        map.addControl(driverMap.grap);
        driverMap.grap.hide();
        map.openInfoWindow(driverMap.myinfo, driverMap.myinfo.point);
        position();
        driver.connect(url);
    },

    newInfoWindow: function (info, opts) {
        var infoWindow; 
        if ( opts === undefined ) {
            opts = {
                enableCloseOnClick: false,
                title: "乘客信息"
            };
        }
        infoWindow = new BMap.InfoWindow(info); 
        infoWindow.visible = "show";
        infoWindow.addEventListener('open', driverMap.addGrap);
        infoWindow.addEventListener('close', driverMap.rmGrap);

        return infoWindow;
    },

    GrapControl: function () {
        this.defaultAnchor = BMAP_ANCHOR_TOP_RIGHT;
        this.defaultOffset = new BMap.Size(10, 10); 
    },

    addGrap: function () {
        button.textContent = "抢单";
        var visible = map.getInfoWindow().visible;
        driverMap.grap[visible]();
    },

    rmGrap: function () {
        button.textContent = "定位";
        driverMap.grap.show();
    },

    newMarker: function (point, pid, opts) {
        var marker;

        marker = new BMap.Marker(point, opts);
        marker.info = driverMap.newInfoWindow("乘客：" + pid + "<br/>和我相距x米" + "<br/>目的地： XXX");
        marker.info.pid = pid;
        map.addOverlay(marker);
        marker.openInfoWindow(marker.info);
        marker.info.timer = window.setTimeout(function(){
            driver.reject(pid);
            map.removeOverlay(marker);
        },13000);
        marker.addEventListener("click", function(){  
            marker.openInfoWindow(marker.info);
        });

        return marker;
    }
};
