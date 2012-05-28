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

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import orochi.nativeadapter.RequestThread.RequestExecutor;
import orochi.util.ActivityForResultObject;

import android.content.Intent;
import android.util.Log;

public class NativeEngine extends Thread {
    
    private ServerSocket serverSocket;
    private boolean running = true;	
    private RequestHandler baseHandler;
    private HashMap<String, RequestExecutor> requestExecutors; //array of executing request
    private List<ActivityForResultObject> actForResultQ; //List of ActivityForResult request
    
    //file mime types
    public static final Hashtable<String, String> MIME_TYPES = new Hashtable<String, String>();   
    static {
        String image = "image/";
        MIME_TYPES.put(".gif", image + "gif");
        MIME_TYPES.put(".jpg", image + "jpeg");
        MIME_TYPES.put(".jpeg", image + "jpeg");
        MIME_TYPES.put(".png", image + "png");
        String text = "text/";
        MIME_TYPES.put(".html", text + "html");
        MIME_TYPES.put(".htm", text + "html");
        MIME_TYPES.put(".txt", text + "plain");
    }    
	
    public NativeEngine(NativeService nativeService, Class<RequestHandler>[] requestHandlers, int port) throws IOException {
    	baseHandler = initRequestHandlers(nativeService, requestHandlers);
        serverSocket = new ServerSocket(port);        
        start();
    }
    
    //initialize request handlers
    private RequestHandler initRequestHandlers(NativeService nativeService, Class<RequestHandler>[] requestHandlers){
    	RequestHandler baseHandler = null;
    	RequestHandler previousHandler = null;
		for (Class<RequestHandler> requestHandlerCls : requestHandlers) {
			try {
				@SuppressWarnings("rawtypes")
				Class theClass = requestHandlerCls;
				RequestHandler requestHandler = (RequestHandler) theClass.newInstance();
				requestHandler.setNativeService(nativeService);
				if(previousHandler!=null)
					previousHandler.setNextHandler(requestHandler);
				else
					baseHandler = requestHandler;
				previousHandler = requestHandler;
			} catch (InstantiationException e) {
				Log.e("Orochi", e + "NativeEngine: Interpreter class must be concrete.");
			} catch (IllegalAccessException e) {
				Log.e("Orochi", e + "NativeEngine: Interpreter class must have a no-arg constructor.");
			}
		}
		return baseHandler;
    }

	public void run() {
    	ExecutorService executor = Executors.newCachedThreadPool();    	
    	requestExecutors = new HashMap<String, RequestExecutor>();
    	actForResultQ = (List<ActivityForResultObject>)Collections.synchronizedList(new LinkedList<ActivityForResultObject>());
    	Log.d("Orochi", "NativeEngine: Running");
    	
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                String requestID = Long.toString(System.currentTimeMillis());
                RequestThread requestThread = new RequestThread(requestID, requestExecutors, socket, baseHandler);
                executor.execute(requestThread);
            }
            catch (IOException e) {
            	Log.d("Orochi", "NativeEngine: "+e.toString());
            }
        }
        executor.shutdown();
    }
    
	//kill run process
    public void kill(){
    	running = false;
    	try {
    		serverSocket.close();
    		baseHandler.destruct();
    		Log.d("Orochi", "NativeEngine: Stopped");
        }
        catch (IOException e) {
        	Log.e("Orochi", "NativeEngine: "+e);
        }
    }
    
    //add a ActivityForResult request to actForResultQ
	public void addActForResult(ActivityForResultObject actForResObj){
		synchronized(actForResultQ) {
			actForResultQ.add(actForResObj);
			if(actForResultQ.size()==1)
				actForResultQ.get(0).startActForResult();
		}
	}
    
    public void handleOnActResult(int requestCode, int resultCode, Intent data) {
    	//remove the done request from to actForResultQ
    	ActivityForResultObject actForResObj = actForResultQ.remove(actForResultQ.size()-1);
    	if(actForResObj!=null){
    		//send the result to the handler
    		actForResObj.handleOnActResult(requestCode, resultCode, data);
    		if(!actForResultQ.isEmpty()){
    			//start the next ActivityForResult request
    			ActivityForResultObject nextActForResObj = actForResultQ.get(0);
    			nextActForResObj.startActForResult();
    		}
    	}
    }
    
    //static function to create a NativeEngine instance
    public static NativeEngine create(NativeService nativeService, Class<RequestHandler>[] requestHandlers, int port) {
        try {
        	return new NativeEngine(nativeService, requestHandlers, port);
        }
        catch (IOException e) {
        	Log.e("Orochi", "NativeEngine: "+e);
            return null;
        }
    }    

}