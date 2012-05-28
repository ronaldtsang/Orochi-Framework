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

package orochi.nativeadapter.requesthandler;

import java.util.HashMap;

import orochi.nativeadapter.RequestHandler;
import orochi.util.NetworkUtils;
import orochi.util.json.JSONException;
import orochi.util.json.JSONObject;

public class NetworkHandler extends RequestHandler {
	
	@Override
	public void handle(HashMap<String,String> getParms, JSONObject jsonObject) throws JSONException {		
		if(getParms.containsKey("networkHandler")){
			String requestValue = getParms.get("networkHandler");
			HashMap<String, Object> jsonHM = new HashMap<String, Object>();
			
			if (requestValue.equals("getConnType"))				
				jsonHM.put("connType", NetworkUtils.getConnectionInfo(getNativeService()));
		

			jsonObject.put("networkHandler", jsonHM);
		}
		
		nextHandle(getParms, jsonObject);
	}
	
}
