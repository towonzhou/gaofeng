var app = require('./login').app;
var server = require('http').createServer(app)
, io = require('socket.io').listen(server);

server.listen(process.env.PORT || 9999);

var Role = {};
var Drivers = {};
var Passengers = {};

Role['Drivers'] = {
    namespace: '/drivers',
    Sockets: Drivers,
    room: 'Drivers'
};
Role['Passengers'] = {
    namespace: '/passengers',
    Sockets: Passengers,
    room: 'Passengers'
};

/*
var Event = {
    name: name,
    callback: function (data) {
    }
};
*/
var Event_id = {
    name: 'id',
    callback: function(data) {
        if ( this.socket === undefined ) {
            console.log("err:: the socket is undefined (in Event_id)");
            return;
        }
        if (data.pid) {
            console.log("pid:" + data.pid + ' into the /passengers');
            this.socket.pid = data.pid;
            Passengers[data.pid] = this.socket;
            this.socket.join('Passengers');
        }
        if (data.did) {
            console.log("did:" + data.did + " into the /drivers");
            this.socket.did = data.did;
            this.socket.dphone = data.dphone;
            Drivers[data.did] = this.socket;
            waitings_call(this.socket);
            this.socket.join('Drivers');
        }
    }
};

for ( var role in Role ) {
    //create the namespace by Role;
    role_connection(role);
}

function role_connection(role) {
    io.of(Role[role].namespace).on('connection',function (socket) {
        Event_id.socket = socket;
        socket.on(Event_id.name, function (data) {
            Event_id.callback(data);
        });

        if ( role === 'Passengers' ) {
            //the socket has pid attribute
            want_taxi(socket);
            //when emit 'confirm taxi',the socket already has pid attribute
            confirm_taxi(socket);
        } else if ( role === 'Drivers') {
            //the socket has did attribute
            provide_taxi(socket);
            reject_taxi(socket);
        }
    });
}

//passenger emit 'want taxi'
function want_taxi(socket) {
    socket.on('want taxi',function () {
        var clients = io.of('/drivers').clients('Drivers');
        for ( var i in clients ) {
            console.log("pid:" + socket.pid + " emit want taxi to did:" + clients[i].did );
            clients[i].emit('want taxi', {pid: socket.pid});
        }
        //add the passenger to the room of Waitings 
        socket.join('Waitings');
        console.log("Waittings room has " + io.of('/passengers').clients('Waitings').length + " clients");
        console.log("add " + "pid:" + socket.pid + " into waitings room");
    });
}

//waitings emit 'want taxi'
function waitings_call(socket) {
    var clients = io.of('/passengers').clients('Waitings');
        console.log("has " + clients.length + " passengers are waiting");
    for ( var i in clients ) {
        console.log("Waiter pid:" + clients[i].pid + " emit want taxi to did:" + socket.did );
        socket.emit('want taxi', {pid: clients[i].pid});
    }
}

//passenger emit 'confirm taxi'
function confirm_taxi(socket) {
    socket.on('confirm taxi', function () {
        Drivers[socket.did].emit('confirm taxi');
        console.log('pid:' + socket.pid + " confirm taxi of did" + socket.did);
        //leave the Waitings room
        socket.leave('Waitings');
        console.log("pid:" + socket.pid + " leave waitings room");
    });
}

//driver emit 'provide taxi'
function provide_taxi(socket) {
    socket.on('provide taxi', function (data) {
        //add the did to the passenger who the driver provide taxi
        Passengers[data.pid].did = socket.did;
        Passengers[data.pid].emit('provided taxi', { dphone: socket.dphone });
        console.log('did:' + socket.did + " provide taxi to pid:" + data.pid);
    });
}

//driver emit 'reject taxi'
function reject_taxi(socket) {
    socket.on('reject taxi', function (data) {
        Passengers[data.pid].emit('rejected taxi');
        console.log('did:' + socket.did + " reject taxi to pid:" + data.pid);
    });
}

//关闭调试信息
io.set('log level', 1);

