/*
Copyright (c) 2012 Ronald Tsang, ronaldtsang@orochis-den.com

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

Orochi.include(
    _.Class({
        className: 'com.orochis-den.orochi-ext.accelerometer',
        body:new function(){
            var me = Orochi.prototype;
            me.accelerometer = {
                getAcceleration: function(_args){
                    var args = init_args(_args, {});
                    var onSuccess = init_args(args.onSuccess, function(){});
                    var onError = init_args(args.onError, function(){});

                    var requestArgs = {
                        data: {accelerometerHandler:'getAcceleration'},
                        onSuccess: function(json, textStatus, jqXHR){
                            if(isset(json.accelerometerHandler)){
                                if(isset(json.accelerometerHandler.acceleration)){
                                    me.log("orochi.accelerometer", "getAcceleration onSuccess()");
                                    var acc = json.accelerometerHandler.acceleration;
                                    onSuccess({x:acc[0], y:acc[1], z:acc[2]});
                                    return;
                                }                       
                            }

                            //if response not match
                            requestArgs.onError(jqXHR, textStatus, null, json);
                        },
                        onError: function(jqXHR, textStatus, errorThrown, json){
                            me.log("orochi.accelerometer", "getAcceleration onError()");
                            onError();
                        }                
                    };
                    me.nativeRequest(requestArgs);
                },
                listen: function(_args){
                    var args = init_args(_args, {});
                    var interval = init_args(args.interval, 1000);
                    var listen = true;

                    var onSuccess = init_args(args.onSuccess, function(){});
                    var onError = init_args(args.onError, function(){});

                    var lisFunction = function(onSuccess, onError){
                        me.accelerometer.getAcceleration({
                            onSuccess: function(acceleration){
                                onSuccess(acceleration);
                                if(listen)
                                    setTimeout(function(){lisFunction(onSuccess, onError)}, interval);
                            },
                            onError: function(){
                                onError();
                            } 
                        })
                    }

                    setTimeout(function(){lisFunction(onSuccess, onError)}, interval);

                    return {clear: function(){
                        listen = false;
                    }}
                }       
            }
        }
    })
);
