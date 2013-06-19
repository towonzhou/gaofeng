var bcrypt = require('bcrypt');
var flash = require('connect-flash');
var express = require('express')
, passport = require('passport')
, database = require('./mongodb/database')
, url = require("url")
. resource = require('express-resource')
, LocalStrategy = require('passport-local').Strategy;

passport.serializeUser(function(user, done) {
  done(null, user.id);
});

passport.deserializeUser(function(id, done) {
  database.findById(id, function (err, user) {
    done(err, user);
  });
});

var app = express();

passport.use(new LocalStrategy(
	function(username, password, done) {
		database.findByName(username, function(err, user) {
            bcrypt.compare(password, user.password, function(err, res) {
                if ( username == user.name && res === true) {
                    return done(null, user);
                } else {
                    return done(err);
                }
            });
		});
	}
));

// configure Express
app.configure(function() {
  app.set('views', __dirname + '/views');
  app.set('view engine', 'ejs');
  app.use(express.logger());
  app.use(express.cookieParser());
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(express.session({ secret: 'keyboard cat' }));
  // Initialize Passport!  Also use passport.session() middleware, to support
  // persistent login sessions (recommended).
  app.use(flash());
  app.use(passport.initialize());
  app.use(passport.session());
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
});


app.get('/', function(req, res){
  res.render('index', { user: req.user });
});

app.get('/sign_up', function(req, res){
	res.render('sign_up', { user: req.user });
});

//sign_up post registerted and redirect login
app.post('/sign_up', 
  function(req, res, next) {
    var user = {};
    user.name = req.body.username;
    user.password = req.body.password;
    database.newDriver(user, function(user) {
        if (req.login) {
            req.logIn(user, function(err) {
                if (err) { return next(err); }
                return res.redirect('/');
            });
        }
    });
  });

// POST /login
//   Use passport.authenticate() as route middleware to authenticate the
//   request.  If authentication fails, the user will be redirected back to the
//   login page.  Otherwise, the primary route function function will be called,
//   which, in this example, will redirect the user to the home page.
//
//   curl -v -d "username=bob&password=secret" http://127.0.0.1:3000/login

app.get('/login', function(req, res){
	res.render('login', { user: req.user });
});

app.post('/login', 
  passport.authenticate('local', { failureRedirect: '/login', failureFlash: true }),
  function(req, res) {
    res.redirect('/');
  });

app.get('/logout', function(req, res){
  req.logout();
  res.redirect('/');
});

app.resource('restaurants', require('./controller/restaurant'));

exports.app = app;
