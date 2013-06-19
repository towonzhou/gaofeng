var mongoose = require('./connect').mongoose;

var food_info = [];

var restaurantSchema = mongoose.Schema({
  name: { type: String, required: true }
, food: {
    kind: { type: String, required: true }
  , food_info: Array
  }
, park: { type: Boolean, default: true } 
, pricePerPerson: Number
, image: String
, latitude: { type: Number, required: true }
, longitude: { type: Number, required: true }
, toilet: Boolean
, hotWater: Boolean
, hours: Date
, phone: String
})

/*
var rest1 = {
    name: '成都小吃'
  , food: { kind: '米饭'
      , food_info: [
            { name: '辣椒', price: 10 }
        ]
    }
  , park: true
  , pricePerPerson: 20
  , latitude: 39.945
  , longitude: 116.404
  , toilet: false
  , hotWater: true
  , phone: '133222'
}
*/

var Restaurant = mongoose.model('Restaurant', restaurantSchema);

function newRestaurant(restaurant, fn) {
    var _restaurant = new Restaurant(restaurant);
    _restaurant.save(function(err, doc){
        if (err) throw err;
        console.log(doc);
        if (doc) {
          fn(doc);
        }
    })
}

function findById(id, fn) {
    Restaurant.find( { _id: id }, function(err, docs){
        if (err) throw err;
        if (!docs[0]) {
            console.log("user is not exist");
            return 
        }
        fn(docs[0]);
    });
}

function findAll(query, fn) {
    if ( query.longitudeSup && query.longitudeInf
        && query.latitudeSup && query.latitudeInf )
    {
        console.log(query);
        var longitudeSup = query.longitudeSup;
        var longitudeInf = query.longitudeInf;
        var latitudeSup = query.latitudeSup;
        var latitudeInf = query.latitudeInf;
        Restaurant.find({ longitude: { $gte: longitudeInf, $lte: longitudeSup },
                        latitude: { $gte: latitudeInf, $lte: latitudeSup } }
                        , function(err, docs){
                            fn(docs);
                        });
    }
    else {
        fn(null);
    }
}

exports.newRestaurant = newRestaurant;
exports.findById = findById;
exports.findAll = findAll;

