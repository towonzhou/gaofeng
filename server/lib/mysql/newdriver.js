var mysql = require('mysql');
var config = require('./config');
var connection = mysql.createConnection(config.db_options);

//a connection can also be implicitly established by invoking a query
//use database gaofeng
//table drivers('id','name','password','phone')
function newDriver(driver, fn) {
	if (!driver && driver == undefined) {
		console.log("driver is null or undefined");
		return;
	}
	connection.query('insert into gaofeng.drivers set ?', driver,function(err, result) {
		if (err) throw err;
        driver.id = result.insertId;
        fn(driver);
	});
}

//var driver = {id: '', name: 'zhou', password: 'abc'};
//newDriver(driver);

exports.newDriver = newDriver;
