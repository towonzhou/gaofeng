var restaurantSql = require('../mongodb/restaurant');
var url = require('url');
exports.index = {
    json: function(req, res, next){
        if (req.isAuthenticated()) { 
            var query = url.parse(req.url,true).query;
            restaurantSql.findAll(query ,function(docs){
                res.send({docs:docs});
            });
        }
        else {
            res.redirect('/login');
        }
    }
}

exports.show = {
  json: function(req, res){
      if (req.isAuthenticated()) { 
          res.send(req.restaurant);
      }
      else {
          res.redirect('/login');
      }
  }
}

exports.create = {
  json: function(req, res){
      if (req.isAuthenticated()) { 
          restaurantSql.newRestaurant(req.body.restaurant,function(docs){
              res.send({docs:docs});
          });
      }
      else {
          res.redirect('/login');
      }
  }
}

exports.load = function(id, fn){
  restaurantSql.findById(id, function(doc){
    if (!doc) return fn(new Error('not found'));
    process.nextTick(function(){
      fn(null, doc);
    });
  }); 
}
