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

import orochi.nativeadapter.NativeService;
import orochi.nativeadapter.RequestHandler;
import orochi.nativeadapter.OrochiActivity;
import orochi.util.ActivityForResultObject;
import orochi.util.FileUtils;
import orochi.util.json.JSONException;
import orochi.util.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class MediaHandler extends RequestHandler {
	
	private static final int UNKNOWN=0;
	private static final int GET_IMAGE=1;
	private static final int GET_AUDIO=2;
	private static final int GET_VIDEO=3;
	private static final int CAPTURE_IMAGE=4;
	private static final int CAPTURE_AUDIO=5;
	private static final int CAPTURE_VIDEO=6;	
	
	@Override
	public void setNativeService(NativeService nativeService){
		super.setNativeService(nativeService);
		FileUtils.mkdirIfNeeded(nativeService.appDirectory + "/Captured Media/");
	}	
	
	@Override
	public void handle(HashMap<String, String> getParms, JSONObject jsonObject) throws JSONException{
		HashMap<String, Object> jsonHM = new HashMap<String, Object>();
		
		int requestCode = UNKNOWN;
		
		if(getParms.containsKey("mediaHandler") && getParms.containsKey("mediaType")){
			String requestValue = getParms.get("mediaHandler");
			String mediaType = getParms.get("mediaType");
			
			if(requestValue.equals("get")){				
				if(mediaType.equals("image"))
					requestCode = GET_IMAGE;
				else if(mediaType.equals("audio"))
					requestCode = GET_AUDIO;
				else if(mediaType.equals("video"))
					requestCode = GET_VIDEO;				
			}			
			else if(requestValue.equals("capture")){
				if(mediaType.equals("image"))
					requestCode = CAPTURE_IMAGE;
				else if(mediaType.equals("audio"))
					requestCode = CAPTURE_AUDIO;
				else if(mediaType.equals("video"))
					requestCode = CAPTURE_VIDEO;				
			}
			MediaActForResult mediaAFR = new MediaActForResult(requestCode);
			getNativeService().addActForResult(mediaAFR);
			mediaAFR.waitOnActResultDone();
			
			jsonHM.put("mediaPath", mediaAFR.mediaPath);
			jsonObject.put("mediaHandler", jsonHM);
		}	
		
		nextHandle(getParms, jsonObject);

	}	
	
	
	protected class MediaActForResult extends ActivityForResultObject{
		
		
		protected String mediaPath = "";
		private int requestCode;
		protected int actResultCode;
		private boolean activityInFront;
		
		public MediaActForResult(int requestCode){
			this.requestCode = requestCode;
		}
		
		@Override
		public void startActForResult(){
			activityInFront = openActivity(OrochiActivity.mainActivity);
			
			Intent intent = new Intent();
			//String chooserTitle;
			
			if(requestCode == GET_IMAGE || requestCode == GET_AUDIO || requestCode == GET_VIDEO){
				if(requestCode == GET_IMAGE){
					intent.setType("image/*");
				}
				else if(requestCode == GET_AUDIO){
					intent.setType("audio/*");
				}
				else if(requestCode == GET_VIDEO){
					intent.setType("video/*");
				}
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				//intent = Intent.createChooser(intent,"Get Image");
			}
			else{
				if(requestCode == CAPTURE_IMAGE){					
					intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				}
				else if(requestCode == CAPTURE_AUDIO){	
					//intent.setAction(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);				
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.setType("audio/amr");
					intent.setClassName("com.android.soundrecorder",
					"com.android.soundrecorder.SoundRecorder");
				}
				else if(requestCode == CAPTURE_VIDEO){	
					//int durationLimit = ;
					intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
					//intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
					//intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, sizeLimit);
					//intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit);
				}
			}
			getNativeService().getActivities().get(OrochiActivity.mainActivity).startActivityForResult(
					intent, requestCode);
		}
		
		@Override
		public void handleOnActResult(int requestCode, int resultCode,Intent data) {
			actResultCode = resultCode;		
			Context context = getNativeService();
			if (resultCode == Activity.RESULT_OK) {;
				if (requestCode == GET_IMAGE || requestCode == GET_AUDIO
						|| requestCode == GET_VIDEO) {

					Uri selectedImage = data.getData();
					mediaPath = FileUtils.getRealPathFromMediaURI(context,selectedImage);
					Log.d("Orochi", "Media Path: " + mediaPath);

				} else {

		            Uri capturedImageUri = data.getData();
		            String filePath = FileUtils.getRealPathFromMediaURI(context, capturedImageUri);          
		            int mid= filePath.lastIndexOf(".");
		            String ext = filePath.substring(mid+1,filePath.length());
					String savePath = getNativeService().appDirectory + "/Captured Media/"
						+ String.valueOf(System.currentTimeMillis())
						+ "." + ext;
		            if (FileUtils.moveFile(context, filePath, savePath))
		            	mediaPath = savePath;
		            else
		            	mediaPath = "";
				}
			} else {
				mediaPath = "";
			}
			if(!activityInFront){
				getNativeService().getActivities().get(OrochiActivity.mainActivity).moveTaskToBack(true);
			}
			onActResultDone();
		}	
	}
	


}
