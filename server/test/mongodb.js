var assert = require('assert')
  , mongoose = require('../lib/mongodb/connect').mongoose
  , database = require('../lib/mongodb/database')
  , driverSchema = database.driverSchema
  , Schema = mongoose.Schema; 

var Driver = mongoose.model('Driver', driverSchema);

  describe('connections', function(){
      describe('connect the mongodb', function(){
          it('should connecting state', function(done){
              var db = mongoose.connection;

              assert.equal(2, db.readyState);
              db.close(done);
          })
          it('should accept mongodb://localhost/test', function(done) { 
              var db = mongoose.connection;
              db.on('error', function(err){});
              assert.equal('object', typeof db.options);
              assert.equal('object', typeof db.options.server);
              assert.equal(true, db.options.server.auto_reconnect);
              assert.equal('object', typeof db.options.db);
              assert.equal(false, db.options.db.forceServerObjectId);
              assert.equal('primary', db.options.db.read_preference);
              assert.equal(undefined, db.pass);
              assert.equal(undefined, db.user);
              assert.equal('test', db.name);
              assert.equal('localhost', db.host);
              assert.equal(27017, db.port);
              db.close(done);
          });
      })

      describe('.model()', function(){
          it('allows passing a schema', function(done){
              var db = mongoose.connection;
              db.close();

              assert.ok(Driver.schema instanceof Schema);
              assert.ok(Driver.prototype.schema instanceof Schema);

              var d = new Driver({name:'abc', password:'cba'});
              assert.equal('abc', d.name);
              assert.equal('cba', d.password);
              done();
          })
      })
  })
