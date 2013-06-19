(function(global) {
    global.SERVER = 'http://gaofeng-server.nodejitsu.com';
    // global.SERVER = 'http://172.16.82.31:9999';

    function randomPid(length) {
        length = Number(length) || 40;
        var alphabets = "01234567890abcdefghijklmnopqrstuvwxyz";

        var result = "";
        while (length--) {
            var id = Math.floor(Math.random() * 36);
            result += alphabets[id];
        }

        return result;
    }

    var Passenger = global.Passenger = function() {
        EventEmitter.call(this);
        this.pid = randomPid(10);
    };

    Passenger.prototype = EventEmitter.prototype;
    var addons = {
        connect: function(ip, done) {
            var self = this;

            var sock = this.socket = io.connect(ip + '/passengers', {
                reconnect: true
            });
            sock.on('connect', function() {
                sock.emit('id', {pid: self.pid});
                done(null);
            });

            sock.on('connect_failed', function(err) {
                if (!err) {
                    err = {message: 'unknown error'};
                }
                console.log('socket error: ', String(err));
                if (done) {
                    done(String(err));
                    done = null;
                }
            });

            sock.on('error', function(err) {
                if (!err) {
                    err = {message: 'unknown error'};
                }
                console.log('socket error: ', err.message);
                if (done) {
                    done(err);
                    done = null;
                }
            });
        },

        gotACar: function(car) {
            console.log('gotACar: ', car);
            this.socket.removeAllListeners('rejected taxi');
            this.emit('got a car', car);
            if (car && car.dphone) {
                this.taxiphone = car.dphone;
            }
        },

        beingRejected: function() {
            console.log('rejected');
            this.socket.removeAllListeners('provided taxi');
            this.emit('rejected');
        },

        callACar: function() {
            this.socket.emit('want taxi');
            this.socket.once('provided taxi', this.gotACar.bind(this));
            this.socket.once('rejected taxi', this.beingRejected.bind(this));
        },

        confirmTaxi: function() {
            this.socket.emit('confirm taxi');
        },

        changeServer: function(newServer, done) {
            if (this.socket) {
                this.socket.disconnect();
                this.emit('disconnect');
            }

            this.connect(newServer, done);
        }
    };

    for (var p in addons) {
        Passenger.prototype[p] = addons[p];
    }
    Passenger.constructor = Passenger;

}(this));
