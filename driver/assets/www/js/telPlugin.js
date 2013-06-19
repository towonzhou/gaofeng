window.TelPlugin = function(number) {
    number = "10010";
    var exec = cordova.require('cordova/exec');
    exec(function(data){
        alert(JSON.stringify(data));
    },function(err){
        alert("Tel error: " + err);
    },"telPlugin", "call" , [number]);
};
