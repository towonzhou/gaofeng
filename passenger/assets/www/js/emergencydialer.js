cordova.define("cordova/plugin/emergencydialer",
  function(require, exports, module) {
    var exec = require("cordova/exec");
    var EmergencyDialer = function () {};

    var EmergencyDialerError = function(code, message) {
        this.code = code || null;
        this.message = message || '';
    };

    EmergencyDialer.CALL_FAILED = 0;

    EmergencyDialer.prototype.dial = function(telephoneNumber,success,fail) {
        exec(success,fail,"EmergencyDialer", "dial", [telephoneNumber]);
    };

    var emergencyDialer = new EmergencyDialer();
    module.exports = emergencyDialer;
});

window.plugins = window.plugins || {};
window.plugins.emergencyDialer = cordova.require('cordova/plugin/emergencydialer');
