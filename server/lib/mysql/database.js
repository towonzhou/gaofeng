var mysql = require('mysql');
var connection = mysql.createConnection({
    host : 'localhost',
    user : 'root',
    password : 'root',
    });
/*
 *a connection can also be implicitly established by invoking a query
function connectSql(connection) {
	connection.connect(function(err) {
		console.log("connect sql error");
	});
}
*/	

function findById(id, fn) {
	connection.query("select * from gaofeng.drivers where id=" + id, function(err, rows, fields) {
		if (err) throw err;
		fn(null, rows[0]);
	});
}

function findByName(username, fn) {
	/*
	connection.query('use test', function(err, rows, fields) {
		if (err) throw err;
	});
	*/

	connection.query("select * from gaofeng.drivers where name=\'" + username + "\'", function(err, rows, fields) {
		//console.log(rows, fields);
		if (err) throw err;
        if (!rows[0]) {
            console.log("user is not exist");
            return 
        }
		fn(null, rows[0]);
	});
}

//connection.end();

exports.findById = findById;
exports.findByName = findByName;
