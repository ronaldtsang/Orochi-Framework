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
        className: 'com.orochis-den.orochi-ext.network',
        body:new function(){
            var me = Orochi.prototype;
            me.network = {
                getConnType: function(_args){
                    var args = init_args(_args, {});
                    var onSuccess = init_args(args.onSuccess, function(){});
                    var onError = init_args(args.onError, function(){});

                    var requestArgs = {
                        data: {networkHandler:'getConnType'},
                        onSuccess: function(json, textStatus, jqXHR){
                            if(isset(json.networkHandler)){
                                if(isset(json.networkHandler.connType)){
                                    me.log("orochi.network", "getConnType onSuccess()");
                                    onSuccess(json.networkHandler.connType);
                                    return;
                                }                       
                            }

                            //if response not match
                            requestArgs.onError(jqXHR, textStatus, null, json);
                        },
                        onError: function(jqXHR, textStatus, errorThrown, json){
                            me.log("orochi.network", "getConnType onError()");
                            onError();
                        }                
                    };
                    me.nativeRequest(requestArgs);
                }      
            }
        }
    })
);