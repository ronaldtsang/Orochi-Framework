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
import orochi.util.json.JSONException;
import orochi.util.json.JSONObject;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;


public class FlashlightHandler extends RequestHandler {
	
	private Camera cam;
	private boolean isLightOn = false;
	
	@Override
	public void handle(HashMap<String,String> getParms, JSONObject jsonObject) throws JSONException {		
		if(getParms.containsKey("flashlightHandler")){
			String requestValue = getParms.get("flashlightHandler");
			HashMap<String, Object> jsonHM = new HashMap<String, Object>();
			if(getNativeService().getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){				
				if (requestValue.equals("turnOn") && !isLightOn ) {
					cam = Camera.open();
					Parameters p = cam.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
					cam.setParameters(p);
					cam.startPreview();
					isLightOn = true;
					
				} else if (requestValue.equals("turnOff") && isLightOn) {
					cam.stopPreview();	
					cam.release();
					isLightOn = false;
				}
				jsonHM.put("flashlight", isLightOn?"on":"off");
			}
			else{
				jsonHM.put("flashlight", "notExist");
				Log.d("Orochi", "FlashlightHandler: Camera FlashLight not found.");
			}
			jsonObject.put("flashlightHandler", jsonHM);
		}
		
		nextHandle(getParms, jsonObject);
	}

	@Override
	public void destruct()
	{
		if(isLightOn){
			cam.stopPreview();	
			cam.release();
		}
		Log.d("Orochi", "FlashlightHandler: destruct()");
		super.destruct();
	} 
	
}
