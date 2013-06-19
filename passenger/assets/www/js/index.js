var app = {
    // Application Constructor
    initialize: function() {
        $(document).bind("mobileinit", function(){
            console.log('mobileinit');
            $.mobile.defaultPageTransition="none";
        });


        this.logHistory = [];
        $('.dumbModal .close').on('click', function() {
            $('.dumbModal').toggle('visible');
        });

        if ((window.device && window.device.cordova)) {
            this.bindEvents();
        } else {
            this.onDeviceReady();
        }
    },

    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },

    onOffline: function() {
        app.log('offline');
    },

    onOnline: function() {
        app.log('online');
    },

    onPause: function() {
        app.log('app paused');
    },

    onResume: function() {
        app.log('app resumed');
    },

    log: function(msg) {
        console.log(msg);
        var his = app.logHistory;
        his.push( JSON.stringify(msg) );
        document.getElementById('logs').innerHTML = '<li><a>' +
            his.join('</a></li> <li><a>') + '</a></li>';
        if (app.initialized) {
            // $('#logs').listview('refresh');
        }
    },

    // try locPlugin if provided else use phonegap builtin
    geolocate: function(onSuccess, onError) {
        if (window.locPlugin) {
            window.locPlugin(onSuccess, onError);
            return;
        }

        var args = [onSuccess, onError, {
            maximumAge: 60000, timeout: 30000, enableHighAccuracy: true
        }];

        app.log('send location req');
        try {
            if (!(window.device && window.device.cordova)) {
                console.log('web mode');
                navigator.geolocation.getCurrentPosition.apply(
                    navigator.geolocation, args.slice(0, 2));

            } else {
                navigator.geolocation.getCurrentPosition.apply(
                    navigator.geolocation, args);
            }
        } catch(err) {
            app.log('exception: ', err.message);
        }
    },

    locateMap: function() {
        if (!app.map) {
            app.log('map is not loaded');
            return;
        }

        app.geolocate(
            function(data) {
                var pos = data.coords? data.coords : data;
                app.log(pos);

                if (pos) {
                    app.map.clearOverlays();
                    var point = new AMap.LngLat(pos.longitude, pos.latitude);
                    app.map.setCenter(point);
                    var marker = new AMap.Marker({
                        id: 'whereami',
                        position: point,
                        offset:{x:-8, y:-34}
                    });
                    app.map.addOverlays(marker);
                    setTimeout(app.randomMarkers.bind(app, point, 50), 1000);
                }

            }, function(err) {
                app.log(String(err));
            });
    },

    //testing
    randomMarkers: function(center, size) {
        var markers = new Array(size);
        for (var i = 0; i < size; ++i) {
            var p = new AMap.LngLat(
                center.lng + Math.random() * 0.003,
                center.lat + Math.random() * 0.003
            );

            // app.log(String(p));
            app.map.addOverlays( new AMap.Marker({
                id: 'M' + Math.random() * 100,
                position: p,
                offset:{x:-8, y:-34}
            }) );
        }
    },

    loadMap: function(cb) {
        $.getScript('http://api.amap.com/webapi/init?v=1.1', function() {
            cb();
        });
    },

    resizeContent: function() {
        var height = $('body').height() - $('#app .ui-header').height() + $('#app .ui-footer').height();
        app.log('reheight to ' + height);
        $('#app .ui-content').css('height', height + 'px');
    },

    onDeviceReady: function() {
        document.addEventListener('offline', this.onOffline, false);
        document.addEventListener('online', this.onOnline, false);
        document.addEventListener('pause', this.onPause, false);
        document.addEventListener('resume', this.onResume, false);

        app.passenger = new Passenger();

        $(document).delegate('#app', 'pageload', function() {
            console.log("page load");
        });

        $(document).delegate('#app', 'pageinit', function() {
            app.log('page inited');
            app.resizeContent();

            app.initialized = true;
            $('#calldriver').on('click', function() {
                var phone = app.passenger.taxiphone;
                app.log('call driver: ' + phone);
                window.plugins.emergencyDialer.dial(phone);
                if (phone) {
                    $('#taxiphone').css('href', 'tel:' + phone).text(phone);
                }
                return true;
            });

            app.elemPassenger = document.getElementById('passenger');
            app.$elemAct = $('#action');
            app.$elemAct.button('disable');

            app.$elemLoc = $('#locate');
            app.$elemLoc.button('disable');

            app.elemServerip = document.getElementById('serverip');
            app.elemServer = document.getElementById('changeServer');
            app.elemServer.addEventListener('click', app.changeServer);

            app.loadMap(function() {
                app.$elemLoc.button('enable');
                app.$elemLoc.on('click', app.locateMap);

                app.map = new AMap.Map('map', {
                    level: 18,
                    zooms: [10, 19],
                    zoomEnabled: true,
                    doubleClickZoom: true
                });
                app.locateMap();
            });

            if (window.SERVER) {
                app.passenger.connect(window.SERVER, app.onConnectService);
            }

        });

    },

    changeServer: function() {
        var val = String(app.elemServerip.value).trim();
        if (!val.length) {
            app.log('server is empty');
            return;
        }

        if (val.indexOf('http') == -1) {
            val = 'http://' + val;
        }
        app.passenger.changeServer(val, function(err) {
            if (err) {
                app.log(err.message);
            } else {
                app.log('changed server, reconnected');
            }
            window.SERVER = val;
            app.$elemAct.button('disable');
            app.onConnectService();
        });
    },

    playVictory: function() {
        navigator.notification.beep(1);
    },

    onGotACar: function(car) {
        app.log('got car', car);

        var act = app.$elemAct;
        act.button('enable');
        act.val('叫车成功').button('refresh');

        app.passenger.confirmTaxi();
        app.playVictory();
    },

    onBeingRejected: function() {
        //this.state(STATE_RESET);
        var act = app.$elemAct;
        act.button('enable');
        act.val('叫车失败').button('refresh');
        app.log('being rejected');
        app.playVictory();
    },

    // when connect to backend
    onConnectService: function(err) {
        if (err) {
            app.log('onConnectService failed');
            return;
        }

        app.passenger.on('disconnect', app.onDisconnect);
        app.passenger.on('got a car', app.onGotACar);
        app.passenger.on('rejected', app.onBeingRejected);
        app.elemPassenger.innerHTML = app.passenger.pid;

        var act = app.$elemAct;
        act.on('click', app.onClick);
        act.button('enable');
    },

    onDisconnect: function() {
        app.log('disconnect from ' + window.SERVER);

        var act = app.$elemAct;
        act.disabled = true;
        act.removeEventListener('click', app.onClick);

        app.elemPassenger.innerHTML = "";
        app.passenger.off('got a car');
        app.passenger.off('disconnect');
        app.passenger.off('rejected');
    },

    onClick: function(ev) {
        app.$elemAct.button('disable');
        app.$elemAct.val('叫车...').button('refresh');

        app.log('click callACar');
        app.passenger.callACar();
    }
};
