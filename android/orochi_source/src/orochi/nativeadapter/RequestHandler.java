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

package orochi.nativeadapter;

import java.util.HashMap;

import orochi.util.json.JSONException;
import orochi.util.json.JSONObject;

import android.content.Intent;
import android.util.Log;

public abstract class RequestHandler {
	
	private RequestHandler nextHandler = null;
	private NativeService nativeService;
	
	public void setNextHandler(RequestHandler nextHandler){
		this.nextHandler = nextHandler;
	}
	
	public RequestHandler getNextHandler(){
		return nextHandler;
	}
	
	public void setNativeService(NativeService nativeService){
		this.nativeService = nativeService;
	}
	
	public NativeService getNativeService(){
		return nativeService;
	}
	
	public void nextHandle(HashMap<String,String> getParms, JSONObject jsonObject)throws JSONException{
		if(nextHandler!=null)
			nextHandler.handle(getParms, jsonObject);
	}	
	
	public void handle(HashMap<String,String> getParms, JSONObject jsonObject)throws JSONException{
		//job done for this handler, pass to the next Hander
		nextHandle(getParms, jsonObject);
	}
	
	public void destruct(){
		if(nextHandler!=null)
			nextHandler.destruct();
	};
	
	//open the mainActivity if needed
	public boolean openActivity(final String activityKey){
		boolean activityInFront = false;
		if(!nativeService.getActivities().containsKey(activityKey)){
			//start a new activity if OrochiActivity is not found
			Intent dialogIntent = new Intent(nativeService.getBaseContext(), nativeService.mainActivityClass);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			nativeService.getApplication().startActivity(dialogIntent);
			
			while(!nativeService.getActivities().containsKey(activityKey)){
				try {
					Log.d("Orochi", "RequestHandler: Waiting for '"+activityKey+"' Activity...");
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Log.e("Orochi", "RequestHandler: "+e);
				}
			}
			Log.d("Orochi", "RequestHandler: '"+activityKey+"' Activity is Ready");	
		}
		else{
			//restart OrochiActivity
			OrochiActivity activity = getNativeService().getActivities().get(activityKey);
			if(!activity.isInFront()){ //not in foreground
				Log.d("Orochi", "RequestHandler: OrochiActivity already exist, restart now...");
				Intent intent = activity.getIntent();
				getNativeService().getApplication().startActivity(intent);				
			}
			else //already in foreground
				activityInFront = true;
			Log.d("Orochi", "RequestHandler: OrochiActivity is Ready");	
		}
		
		//return the pre display status of the activity
		return activityInFront;
	}
}
