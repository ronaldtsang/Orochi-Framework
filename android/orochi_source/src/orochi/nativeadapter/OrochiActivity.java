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

import orochi.nativeadapter.NativeService;
import orochi.nativeadapter.NativeService.NativeIBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class OrochiActivity extends Activity {
	
	//the name of the major activity (you may have multiple Activity, but there is only one mainActivity)
	public static final String mainActivity = "main";
	
    protected ServiceConnection nativeServiceConnection = new NativeServiceConnection(); 
	
	private OrochiActivity me = this;
	private NativeService nativeService = null;
	private Intent nativeServiceIntent = null;	
	private boolean isInFront = true; //if the Activity is in foreground
	
	//Name of this activity
	protected String activityName = mainActivity;
	
	
	public NativeService getNativeService(){
		return nativeService;
	}
	
	public boolean isInFront(){
		return isInFront;
	}	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);		
        
        //initialize the native service connection
        nativeServiceIntent = new Intent(me, NativeService.class);
        bindService(nativeServiceIntent, nativeServiceConnection, Context.BIND_AUTO_CREATE);

		Log.d("Orochi", "OrochiActivity: onCreate()");
    }
    
    protected void startNativeService(){
		startService(nativeServiceIntent); 
    }
    
    protected void stopNativeService(){
		nativeService.stop();
    	stopService(nativeServiceIntent);  
    }
    
    protected boolean isNativeServiceRunning(){
    	return nativeService.isRunning();
    }

	@Override
	protected void onStart() {
		super.onStart();	
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		//remove the Activity's reference from nativeService
		nativeService.getActivities().remove(activityName);
		//unbind the service
		unbindService(nativeServiceConnection);
		Log.d("Orochi", "OrochiActivity: onDestroy()");
		
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		isInFront = false;
		Log.d("Orochi", "OrochiActivity: onPause()");
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		isInFront = true;
		Log.d("Orochi", "OrochiActivity: onResume()");
	}
	
	protected boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	} 
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data); 
    	//pass result to nativeService
    	nativeService.handleOnActResult(requestCode, resultCode, data);
    }
    
	protected class NativeServiceConnection implements ServiceConnection { 
    	
        @SuppressWarnings({ "unchecked", "rawtypes" })
		public void onServiceConnected(ComponentName name,  
                IBinder iBinder) {  
        	nativeService = ((NativeIBinder)iBinder).getService();
        	//put this Activity into the nativeService's activities hash table
        	nativeService.getActivities().put(activityName, me);
        	//initialize the requestHandlers
        	nativeService.setRequestHandlers(new Class[]{
        			orochi.nativeadapter.requesthandler.BasicHandler.class,
        			orochi.nativeadapter.requesthandler.MediaHandler.class,
        			orochi.nativeadapter.requesthandler.AccelerometerHandler.class,
        			orochi.nativeadapter.requesthandler.NotificationHandler.class,
        			orochi.nativeadapter.requesthandler.CompassHandler.class,
        			orochi.nativeadapter.requesthandler.NetworkHandler.class,
        	});  
        	//set the mainActivityClass
        	getNativeService().mainActivityClass = (Class)me.getClass();
        	Log.d("Orochi", "OrochiActivity: Native Service connected");
        };  
  
        public void onServiceDisconnected(ComponentName name) {
        	Log.d("Orochi", "OrochiActivity: Native Service disconnected");  
        };  
    }; 

}