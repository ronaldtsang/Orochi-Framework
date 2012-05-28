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

_ = new _WebOO($);
Orochi = _.Class({
        className: 'com.orochis-den.Orochi',
        body:new function(){
            //private
            var me = this;
            var nativeServiceDomain = 'localhost';
            var nativeServicePort = 8181;
            var nativeServiceAddress = 'http://'+nativeServiceDomain+':'+nativeServicePort;
            var debugMode;   
            
            var DEBUG_MODE_OFF = 0;
            var DEBUG_ALERT_MODE = 1;
            var DEBUG_CONSOLE_MODE = 2;            
            
            var updateNativeServiceAddress = function(){
                nativeServiceAddress = 'http://'+nativeServiceDomain+':'+nativeServicePort
            }
            
            var setNativeServiceDomain = function(domain){
                nativeServiceDomain = domain;
                updateNativeServiceAddress();
            }
            
            var setNativeServicePort = function(port){
                nativeServicePort = port;
                updateNativeServiceAddress();
            }            
            
            //public
            var nativeServiceName = me.nativeServiceName = "Orochi";
            me.hasNativeSupport = false;
            me.isInWebView = /OrochiNative\/[0-9\.]+$/.test(navigator.userAgent);            
            
            me.getNativeServiceAddress = function(){
                return nativeServiceAddress;
            }              
            
            me.getNativeServiceDomain = function(){
                return nativeServiceDomain;
            }            
            
            //Constructor (only called if args is not undefined)
            me._const = function(args){
                if(isset(args.nativeServiceName)){
                    nativeServiceName = args.nativeServiceName;
                }                
                if(isset(args.nativeServicePort)){
                    setNativeServicePort(args.nativeServicePort);
                }
                if(isset(args.nativeServiceDomain)){
                    setNativeServiceDomain(args.nativeServiceDomain);
                }
                debugMode = init_args(args.debugMode, DEBUG_MODE_OFF);                
                
                //A function to be called when orochi native service is ready.
                var onReady = init_args(args.onReady, function(){});

                var initRequestDone = false;
                var initRequests = function(startPort, endPort){
                    var port;
                    var groupRequests = 5;
                    for (port=startPort;port<startPort+groupRequests&&port<=endPort;port++){
                        (function(port){
                            nativeRequest({  
                                url: 'http://'+nativeServiceDomain+':'+port+'?callback=?', 
                                data: {basicHandler:'getDeviceInfo'},
                                requestTimeout: 1000,
                                success: function(json, textStatus, jqXHR){
                                    if(initRequestDone)
                                        return;
                                    if(json.requestDone && json.serviceName==nativeServiceName){
                                        me.hasNativeSupport = true;
                                        setNativeServicePort(port);
                                        initRequestDone = true;
                                        me.log('orochi initRequest', 'success on port: '+port);
                                        onReady();
                                    }
                                },
                                error: function(jqXHR, textStatus, errorThrown){
                                    me.log('orochi initRequest', 'fail on port: '+port);
                                }                            
                            });
                        })(port);
                    }
                    if(startPort+groupRequests<=endPort)
                        setTimeout(function(){
                            if(initRequestDone)
                                return;                            
                            initRequests(startPort+groupRequests, endPort);
                        }, 3000);
                }

                //send a init request to the orochi native service.
                var portRange = 20; //8181-8200
                initRequests(nativeServicePort, nativeServicePort+portRange-1);
                setTimeout(function(){
                    if(!initRequestDone){
                        //connection fail
                        me.hasNativeSupport = false;
                        me.log('orochi initRequest', 'port '+nativeServicePort+'-'+(nativeServicePort+portRange-1)+' is already in use');
                        onReady();
                    }
                }, 12000);
            }
            
            //display log message according to debugMode
            me.log = function(tag, msg){
                if(debugMode == DEBUG_ALERT_MODE)
                    alert(tag+': '+msg);
                else if(debugMode == DEBUG_CONSOLE_MODE)
                    console.log(tag+': '+msg);
            }
            
            var nativeRequest = me.nativeRequest = function(args){
                var requestDone = false;
                //identical to jquery ajax() call, 
                //except: requestTimeout, onSuccess, onError
                var requestArgs = {  
                    url: nativeServiceAddress+'?callback=?',  
                    dataType: 'jsonp',
                    cache: false,
                    data: {basicHandler:'getDeviceInfo'},
                    requestTimeout: 30000, //default timeout for nativeRequest
                    beforeSend: function(jqXHR, settings){
                        setTimeout(function(){
                            if(!requestDone)
                                requestArgs.error(jqXHR, null, null);
                        }, requestArgs.requestTimeout);
                    },
                    success: function(json, textStatus, jqXHR){
                        //me.log('orochi nativeRequest', 'success');
                        if(json.requestDone){
                            requestArgs.onSuccess(json, textStatus, jqXHR);
                        }
                        else if(json.requestDone===false && isset(json.requestID)){
                            requestArgs.data = {requestID: json.requestID};
                            $.ajax(requestArgs);
                        }                            
                        else
                            requestArgs.onError(jqXHR, textStatus, null, json);
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        me.log('orochi nativeRequest', 'error');
                        requestArgs.onError(jqXHR, textStatus, errorThrown);
                    },
                    complete: function(jqXHR, textStatus){
                        me.log('orochi nativeRequest', 'complete');
                        requestDone = true;                        
                    },
                    //A function to be called when nativeRequest is done
                    onSuccess: function(json, textStatus, jqXHR){},
                    //A function to be called when nativeRequest is not success
                    onError: function(jqXHR, textStatus, errorThrown, json){}                    
                };
                $.extend(requestArgs, args);
                $.ajax(requestArgs);
            }
        }			
});


