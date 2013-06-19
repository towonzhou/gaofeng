var bcrypt = require('bcrypt');
var mongoose = require('./connect').mongoose;

var driverSchema = mongoose.Schema({
    name: { type: String, unique: true },
    password: String,
    phone: { type: String, default: '0' }
});

var Driver = mongoose.model('Driver', driverSchema);

function newDriver(driver, fn) {
    bcrypt.hash(driver.password, 8, function(err, hash){
        driver.password = hash;
        var _driver = new Driver(driver);
        _driver.save(function(err, driver){
            if (err) throw err;
            console.log(driver);
            fn(driver);
        })
    });
}

function findById(id, fn) {
    Driver.find( { _id: id }, function(err, docs){
        if (err) throw err;
        if (!docs[0]) {
            console.log("user is not exist");
            return 
        }
        fn(null, docs[0]);
    });
}

function findByName(username, fn) {
    Driver.find( { name: username }, function(err, docs){
        if (err) throw err;
        if (!docs[0]) {
            console.log("user is not exist");
            return 
        }
        fn(null, docs[0]);
    });
}

exports.newDriver = newDriver;
exports.findById = findById;
exports.findByName = findByName;

exports.driverSchema = driverSchema; 
