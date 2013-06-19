/*
 * =====================================================================================
 *
 *       Filename:  test_sockserver.js
 *
 *    Description:
 *
 *        Version:  1.0
 *        Created:  2013/03/18 16时23分27秒
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Sian Cao (sonald), yinshuiboy@gmail.com
 *        Company:  Red Flag Linux Co. Ltd
 *
 * =====================================================================================
*/

var app = require('http').createServer(listener),
    io = require('socket.io').listen(app);

app.listen(8000);

function listener(req, res) {
    res.end(200);
}


var passengers = io.of('/passengers').on('connection', function(passenger) {
    passenger.join('comm');
    passenger.on('id', function(pid) {
        passenger.pid = pid.pid;
        console.log('caught pid: %s', pid.pid);
    });

    passenger.on('want taxi', function() {
        console.log('P: need car');

        passengers.forward('want taxi', {pid: passenger.pid});
        //simulate driver response
        setTimeout(function() {
            passengers.broadcast('provide taxi', {did: 'Dummy'});
        }, 1000);
    });

    passenger.on('disconnect', function() {
        console.log('passenger %s leaves', passenger.pid);
    });
});

passengers.broadcast = function(signal, data) {
    console.log((passengers.clients('comm')));
    passengers.clients('comm').forEach(function(sock) {
        sock.emit(signal, data);
    });
};

passengers.forward = function(signal, data) {
    console.log((drivers.clients('comm')));
    drivers.clients('comm').forEach(function(sock) {
        sock.emit(signal, data);
    });
};

var drivers = io.of('/drivers').on('connection', function(driver) {
    driver.join('comm');
    driver.on('disconnect', function() {
        driver.get('id', function(err, id) {
            console.log('driver %d leaves', id);
        });
    });
});

console.log('passengers: ', Object.keys(passengers));
