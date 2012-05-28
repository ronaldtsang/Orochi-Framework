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

package orochi.example;

import orochi.nativeadapter.NativeService;
import orochi.nativeadapter.OrochiActivity;
import orochi.example.R;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.webkit.WebSettings;
import android.webkit.WebView;
public class MainActivity extends OrochiActivity {
	// This is a sample Activity Class that make use of OrochiActivity

	private WebView myWebView;
	//default url address
	private String urlAddress = "http://orochis-den.com/orochi_jqmdemo/app.html?serviceName=Orochi-example";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//setup custom service connection handler
    	super.nativeServiceConnection = new MyNativeServiceConnection();
        super.onCreate(savedInstanceState);        
        
        //set content view
        setContentView(R.layout.main);		          
        
        //initialize the webview
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);         
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);  
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true); //enable html5 localStorage
        webSettings.setAppCacheMaxSize(8*1024*1024);// set maximum cache to 8M
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());        
        webSettings.setAllowFileAccess(true);// active manifest
        webSettings.setAppCacheEnabled(true);
        webSettings.setUserAgentString(webSettings.getUserAgentString()+" OrochiNative/1.0");
        
        if(isNetworkAvailable()){
        	myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
        	myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }	       

		myWebView.loadUrl(urlAddress);
		
    }
    
	@Override
	protected void onDestroy(){
    	stopNativeService(); //stop the native service
    	super.onDestroy();    	
    }
    
    //custom service connection handler
	protected class MyNativeServiceConnection extends NativeServiceConnection {
		@SuppressWarnings("unchecked")
		public void onServiceConnected(ComponentName name,  
                IBinder iBinder) {  
        	super.onServiceConnected(name, iBinder);
        	//initialize the requestHandlers
        	getNativeService().setRequestHandlers(new Class[]{
        			orochi.nativeadapter.requesthandler.BasicHandler.class,
        			orochi.nativeadapter.requesthandler.MediaHandler.class,
        			orochi.nativeadapter.requesthandler.AccelerometerHandler.class,
        			orochi.nativeadapter.requesthandler.NotificationHandler.class,
        			orochi.nativeadapter.requesthandler.CompassHandler.class,
        			orochi.nativeadapter.requesthandler.NetworkHandler.class,
        	}); 
        	getNativeService().port = 8181; //optional, but better to setup your own port
        	NativeService.serviceName = "Orochi-example"; //optional, but better to setup your own service name
        	
        	//optional
        	//only allow localhost connections:
        	NativeService.allowIPs = new String[] {"/127.0.0.1"};
        	//allow all connections
        	//NativeService.allowIPs = new String[] {"*"};
        	startNativeService();
        };    
    }; 

}