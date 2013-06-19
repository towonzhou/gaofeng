var mongoose = require('mongoose');

//  process.env.MONGODB_URL = 
//    "mongodb://nodejitsu:1b5b97ff885be7198583571df459b9a1@alex.mongohq.com:10028/nodejitsudb3866922929";
var mongodb_url = process.env.MONGODB_URL || "mongodb://localhost/test";

mongoose.connect(mongodb_url);

var test_db = mongoose.connection;
test_db.on('error', console.error.bind(console, 'connection error:'));

exports.mongoose = mongoose;
