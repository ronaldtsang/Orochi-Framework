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

//to check weather the obj is defined
var isset = function(obj){
    return typeof obj != 'undefined';		
} 

//return obj if it is defined, else return defaultVal(for init argurments)
var init_args = function(obj, defaultVal){
    return typeof obj != 'undefined'?obj:defaultVal;
}

var _WebOO = function(_$){
    
    var $ = _$; //mirror of jQuery Object $
    
    var classList = this.classList = [];
    
    var getClass = this.getClass = function(obj){
        return (typeof(obj)=='string')?classList[obj]:obj;      
    }
        
    //a function to create a class
    this.Class = function(obj){

        //default class body
        var _class = function(args){   
            var me = this;

            //call this._const if args is set
            if(isset(args) && typeof(me._const)=='function')
                me._const(args);

        };

        if(isset(obj.extend)){ //extend super class
            var superClass = getClass(obj.extend);
            _class.prototype = new superClass();                
            $.extend(_class.prototype, obj.body, {parent: superClass.prototype});                
        }
        else //no super class
            _class.prototype = obj.body;
        
        //a function to extends or override the class 
        _class.include = function(_includeClass){
            var includeClass = getClass(_includeClass);
            $.extend(_class.prototype, new includeClass());            
        }

        //save the class into classList
        var className = isset(obj.className)?obj.className:'Class_'+new Date().getTime();
        classList[className] = _class;

        return _class;
    }             

}