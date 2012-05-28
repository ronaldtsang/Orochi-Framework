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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import orochi.util.FileUtils;
import orochi.util.json.JSONException;
import orochi.util.json.JSONObject;

import android.util.Log;

public class RequestThread extends Thread {
    
	private String requestID;
	private HashMap<String, RequestExecutor> requestExecutors;
    private Socket socket;
    private RequestHandler baseHandler;
    private static int handlerTimeout = 18*1000; //18s
	
	
    public RequestThread(String requestID, HashMap<String, RequestExecutor> requestExecutors, Socket socket, RequestHandler baseHandler) {
    	this.requestID = requestID;
    	this.requestExecutors = requestExecutors;
        this.socket = socket;
        this.baseHandler = baseHandler;
    }
    
    private static void writeHeader(BufferedOutputStream out, int code, String contentType, String filename, long contentLength, long lastModified) throws IOException {
        out.write(("HTTP/1.0 " + code + " OK\r\n" + 
                   "Date: " + new Date().toString() + "\r\n" +
                   "Server: OrochiNativeEngine/1.0\r\n" +
                   "Content-Type: " + contentType + "\r\n" +
                   (filename!=null?("Content-Disposition: attachment; filename="+filename + "\r\n"):"") +
                   "Expires: Sat, 01 Jan 2000 12:00:00 GMT\r\n" +
                   ((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "") +
                   "Last-modified: " + new Date(lastModified).toString() + "\r\n" +
                   "\r\n").getBytes());
    }
    
    private static void writeError(BufferedOutputStream out, int code, String message) throws IOException {
        message = message + "<hr>";
        out.write(message.getBytes());
        out.flush();
        out.close();
    }
    
    //return a HashMap of the get parameters
    private static HashMap<String,String> hashGetParms(String getParmString){
        StringTokenizer getParmsTokenizer = new StringTokenizer(getParmString, "&");       
        
        Log.d("Orochi", "RequestThead Parmaeters:");
        HashMap<String,String> getParms = new HashMap<String,String>();
        while(getParmsTokenizer.hasMoreTokens()){
        	String parmString = getParmsTokenizer.nextToken();
        	parmString = URLDecoder.decode(parmString);
        	String parmKey = parmString.substring(0, parmString.indexOf("="));
        	String parmValue = parmString.substring(parmString.indexOf("=")+1);
        	
        	getParms.put(parmKey, parmValue); 	            	
        	Log.d("Orochi", "    Key:"+parmKey+"  Value:"+parmValue);
        }
        return getParms;
    }
    
    private static void handleFileRequest(BufferedOutputStream out, String filePath){
    	InputStream reader = null;
    	
    	try {
	    	File file = new File(filePath);
	        
	        if (file.isDirectory() || !file.exists()) {
	            // The file was not found.
	            writeError(out, 404, "File Not Found.");
	        }
	        else {
	        	reader = new BufferedInputStream(new FileInputStream(file));
	        
	            String contentType = (String)NativeEngine.MIME_TYPES.get(FileUtils.getExtension(file));
	            String encodeFilename = null;
	            if (contentType == null) { //if unknown file type
	                contentType = "application/octet-stream";
	                encodeFilename=URLEncoder.encode(file.getName());
	            }
	            
	            writeHeader(out, 200, contentType, encodeFilename, file.length(), file.lastModified());
	            
	            byte[] buffer = new byte[4096];
	            int bytesRead;
	            while ((bytesRead = reader.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
	            reader.close();
	        }
	        out.flush();
	        out.close();    	
    	}
        catch (IOException e) {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception anye) {
                    // Do nothing.
                }
            }
        }        
    }
    
    
    public void run() {
        try {                   	
            socket.setSoTimeout(30000);
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream()); 
            
            //reject invalid ip
            if ( !NativeService.isValidIP(socket.getInetAddress().toString()) ){
            	writeHeader(out, 200, "text/html", null, -1, System.currentTimeMillis());
                writeError(out, 500, "Access Denied.");
            	return;
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String request = in.readLine();
            
            if (request == null || !request.startsWith("GET /?") || !(request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
                // Invalid request type (no "GET")
            	writeHeader(out, 200, "text/html", null, -1, System.currentTimeMillis());
                writeError(out, 500, "Invalid Method.");
                return;
            }             
            
            Log.d("Orochi", "RequestThead: ["+(new Date().toString())+"]Receive Request from: "+socket.getInetAddress().toString());
            final HashMap<String,String> getParms = hashGetParms(request.substring(6, request.lastIndexOf(" HTTP/1.")));
            
            if(getParms.containsKey("fileRequestHandler")){
            	//file request, instead of native request
            	handleFileRequest(out, getParms.get("fileRequestHandler"));
            	return;
            }
            
            //send html header
            writeHeader(out, 200, "text/html", null, -1, System.currentTimeMillis());
            
            //send jsonp header if it is jsonp request
            boolean isJsonpRequest = getParms.containsKey("callback");
            if(isJsonpRequest)
            	out.write((getParms.get("callback")+"(").getBytes());
            
            if (baseHandler != null) {
            	RequestExecutor requestExecutor;
            	if(getParms.containsKey("requestID")){ //handle back request
            		requestExecutor = requestExecutors.get(getParms.get("requestID"));
            		if(requestExecutor==null){
                        writeError(out, 500, "Invalid Method.");
                        return;
            		}            			
            	}
            	else //handle new request
            		requestExecutor = new RequestExecutor(getParms);
            	
            	//send request result
				out.write((requestExecutor.getResult().toString()).getBytes()); 
            }
            
            //send jsonp footer if it is jsonp request
            if(isJsonpRequest)
            	out.write((");").getBytes());            
                     
            out.flush();
            out.close();            
        }
        catch (IOException e) {
            Log.e("Orochi", "RequestThead: "+e.toString());
        }
    }
    
    protected class RequestExecutor {

		private FutureTask<JSONObject> futureTask;
		private ExecutorService executor = Executors.newSingleThreadExecutor();
		
    	public RequestExecutor(final HashMap<String,String> getParms){
    		futureTask = new FutureTask<JSONObject>(
    				new Callable<JSONObject>(){
    					public JSONObject call() {
    			        	JSONObject respJsonObj = new JSONObject();
    			        	try {
    							baseHandler.handle(getParms, respJsonObj);
    						} catch (JSONException e) {
    							Log.e("Orochi", "RequestThead: "+e.toString());
    						}
    			        	return respJsonObj;
    					}
    				}
    		);
    		executor.execute(futureTask);
    	}
    	
    	public JSONObject getResult(){
    		JSONObject respJsonObj = new JSONObject();
			try {
				respJsonObj = futureTask.get(handlerTimeout, TimeUnit.MILLISECONDS);
				
				//if request done within the handlerTimeout (default 18s)
				executor.shutdown();
				//remove this object from the executing request pool
				requestExecutors.remove(requestID);
				try {
					respJsonObj.put("requestDone", true);
					respJsonObj.put("serviceName", NativeService.serviceName);
					respJsonObj.put("requestID", requestID);
				} catch (JSONException e) {
					Log.e("Orochi", "RequestThead: "+e.toString());
				}
				
			} catch (InterruptedException e) {
				Log.e("Orochi", "RequestThead: "+e.toString());
			} catch (ExecutionException e) {
				Log.e("Orochi", "RequestThead: "+e.toString());
			} catch (TimeoutException e) {
				//request is not done within the handlerTimeout (default 18s)
				//change to long request				
				Log.d("Orochi", "RequestThead: handler timeout, change to long request.");
				if(!requestExecutors.containsKey(requestID)){
					//put this object into the executing request pool
					requestExecutors.put(requestID, this);
				}
				try {
					//return the requestID to the client side for back request
					respJsonObj.put("requestDone", false);
					respJsonObj.put("serviceName", NativeService.serviceName);
					respJsonObj.put("requestID", requestID);
				} catch (JSONException e1) {
					Log.e("Orochi", "RequestThead: "+e1.toString());
				}					
			}
			return respJsonObj;
    	}

	}
    
}