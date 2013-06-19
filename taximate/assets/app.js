var _lastCallback = null;

function doHostCallback(data) {
    console.log(JSON.stringify(data));
    _lastCallback && _lastCallback(data);
}

function callHost(action) {
    _lastCallback = null;
    console.log(action + ' ' + typeof arguments[1]);

    if (action === 'showAlert') {
        host.showAlert(arguments.length > 1 && arguments[1] || "");

    } else if (action === 'loadRestaurant') {
        if ((arguments.length > 1) && typeof arguments[1] === 'function') {
            _lastCallback = arguments[1];
            host.loadRestaurant();  // callback will be handled inside host object
        }
    }
}

var $a = document.getElementById('alert');
$a.addEventListener('click', function() {
    callHost('showAlert', 'loading...');

    callHost('loadRestaurant', function(data) {
        console.log('load finished');
        callHost('showAlert', 'load finished');
    });
});