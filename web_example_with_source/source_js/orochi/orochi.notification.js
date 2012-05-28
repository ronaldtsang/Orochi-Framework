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
        className: 'com.orochis-den.orochi-ext.notification',
        body:new function(){
            var me = Orochi.prototype;
            me.notification = {
                alert: function(_args){
                    var args = init_args(_args, {});
                    var isConfirmAlert = init_args(args.isConfirmAlert, false);
                    var title = init_args(args.title, isConfirmAlert?'Confirm Alert':'Alert');
                    var msg = init_args(args.msg, '');
                    var okBtnLabel = init_args(args.okBtnLabel, 'OK');
                    var cancelBtnLabel = init_args(args.cancelBtnLabel, 'Cancel');
                    var btnLabels = okBtnLabel+','+cancelBtnLabel;


                    var onSuccess = init_args(args.onSuccess, function(buttonPressed){});
                    var onError = init_args(args.onError, function(){});            

                    if(!me.isInWebView){
                        var buttonPressed = 0;
                        if(isConfirmAlert){
                            if(!confirm(msg))
                                buttonPressed=1;
                        }
                        else
                            alert(msg);
                        onSuccess(buttonPressed);
                        return;
                    }

                    var requestArgs = {
                        data: {
                            notificationHandler:'alert',
                            alertTitle:title,
                            alertMsg:msg,
                            alertBtnLabels:btnLabels,
                            isConfirmAlert:isConfirmAlert
                        },
                        onSuccess: function(json, textStatus, jqXHR){
                            if(isset(json.notificationHandler)){
                                if(json.notificationHandler.alertDone==true){
                                     me.log("orochi.notification", "alert onSuccess()");
                                    onSuccess(json.notificationHandler.buttonPressed);
                                    return;
                                }
                            }

                            //if response not match
                            requestArgs.onError(jqXHR, textStatus, null, json);
                        },
                        onError: function(jqXHR, textStatus, errorThrown, json){
                            me.log("orochi.notification", "alert onError()");
                            onError();
                        }                
                    };
                    me.nativeRequest(requestArgs);

                },
                beep:function(_args){
                    var args = init_args(_args, {});
                    var beepTimes = init_args(args.beepTimes, 1);
                    var onSuccess = init_args(args.onSuccess, function(){});
                    var onError = init_args(args.onError, function(){});     
                    var requestArgs = {
                        data: {
                            notificationHandler:'beep',
                            beepTimes:beepTimes
                        },
                        onSuccess: function(json, textStatus, jqXHR){
                            if(isset(json.notificationHandler)){
                                if(json.notificationHandler.beepDone==true){
                                     me.log("orochi.notification", "beep onSuccess()");
                                    onSuccess();
                                    return;
                                }
                            }

                            //if response not match
                            requestArgs.onError(jqXHR, textStatus, null, json);
                        },
                        onError: function(jqXHR, textStatus, errorThrown, json){
                            me.log("orochi.notification", "beep onError()");
                            onError();
                        }                
                    };
                    me.nativeRequest(requestArgs);
                },
                vibrate:function(_args){
                    var args = init_args(_args, {});
                    var vibrateLength = init_args(args.vibrateLength, 500);
                    var onSuccess = init_args(args.onSuccess, function(){});
                    var onError = init_args(args.onError, function(){});     
                    var requestArgs = {
                        data: {
                            notificationHandler:'vibrate',
                            vibrateLength:vibrateLength
                        },
                        onSuccess: function(json, textStatus, jqXHR){
                            if(isset(json.notificationHandler)){
                                if(json.notificationHandler.vibrateDone==true){
                                     me.log("orochi.notification", "vibrate onSuccess()");
                                    onSuccess();
                                    return;
                                }
                            }

                            //if response not match
                            requestArgs.onError(jqXHR, textStatus, null, json);
                        },
                        onError: function(jqXHR, textStatus, errorThrown, json){
                            me.log("orochi.notification", "vibrate onError()");
                            onError();
                        }                
                    };
                    me.nativeRequest(requestArgs);
                }      
            }
        }
    })
);