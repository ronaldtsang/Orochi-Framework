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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import orochi.nativeadapter.OrochiActivity;
import orochi.nativeadapter.RequestHandler;
import orochi.util.json.JSONException;
import orochi.util.json.JSONObject;

public class NotificationHandler extends RequestHandler {
	public static final int ALERT_ERROR = -1;
	public static final int OK_BTN_PRESSED = 0;
	public static final int CANCEL_BTN_PRESSED = 1;
	public static int defaultBeepTimes = 1;
	public static int defaultVibrateLenght = 500; //half a second
	
	private synchronized int alert(HashMap<String, String> getParms) {
		if (!getParms.containsKey("alertMsg"))
			return ALERT_ERROR;
		
		return (new Alert(getParms)).getResult();
	}
	
	private void beep(int times) {
		Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone ringtone = RingtoneManager.getRingtone(getNativeService(), ringtoneUri);

		if (ringtone != null) {
			for (long i = 0; i < times; ++i) {
				ringtone.play();
				long timeout = 5000;
				while (ringtone.isPlaying() && (timeout > 0)) {
					timeout = timeout - 100;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private void vibrate(int length) {
		Vibrator vibrator = (Vibrator) getNativeService().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(length);
	}

	@Override
	public void handle(HashMap<String,String> getParms, JSONObject jsonObject) throws JSONException {		
		if(getParms.containsKey("notificationHandler")){
			String requestValue = getParms.get("notificationHandler");
			HashMap<String, Object> jsonHM = new HashMap<String, Object>();
			
			if (requestValue.equals("alert")) {
				int alertRes = alert(getParms);
				if(alertRes!=ALERT_ERROR){
					jsonHM.put("alertDone", true);
					jsonHM.put("buttonPressed", alertRes);
				}
				else
					jsonHM.put("exception", "invalid method");
			}	
			else if (requestValue.equals("beep")) {
				int beepTimes = defaultBeepTimes;
				if(getParms.containsKey("beepTimes"))
					beepTimes = Integer.valueOf(getParms.get("beepTimes"));
				
				beep(beepTimes);
				jsonHM.put("beepDone", true);
	
			}
			else if (requestValue.equals("vibrate")) {
				int vibrateLength = defaultVibrateLenght;
				if(getParms.containsKey("vibrateLength"))
					vibrateLength = Integer.valueOf(getParms.get("vibrateLength"));
				
				vibrate(vibrateLength);
				jsonHM.put("vibrateDone", true);	
			}

			jsonObject.put("notificationHandler", jsonHM);
		}
		
		nextHandle(getParms, jsonObject);
	}
	
	protected class Alert{	
		
		private boolean isConfirmAlert = false;
		private String msg, title;
		private String[] buttonLabels;
		private boolean alertDone = false;
		private int buttonPressed = OK_BTN_PRESSED;
		
		public Alert(HashMap<String, String> getParms){			
			this.msg = getParms.get("alertMsg");

			this.title = getParms.containsKey("alertTitle") ? getParms.get("alertTitle") : "Alert";
			if(getParms.containsKey("isConfirmAlert"))
				isConfirmAlert = getParms.get("isConfirmAlert").equals("true")?true:false;
			this.buttonLabels = new String[] {"OK", "Cancel"};
			String[] labels;
			if(getParms.containsKey("alertBtnLabels")){
				labels = getParms.get("alertBtnLabels").split(",");
				if(labels.length>1)
					buttonLabels = labels;
				else if(labels.length>0)
					buttonLabels[0] = labels[0];
			}
			
		}
		
		public int getResult() {
			openActivity(OrochiActivity.mainActivity);
			
			final OrochiActivity activity = getNativeService().getActivities().get(OrochiActivity.mainActivity);
			
			Runnable runnable = new Runnable() {
				public void run() {					
					AlertDialog.Builder alert = new AlertDialog.Builder(activity);
					alert.setMessage(msg);
					alert.setTitle(title);
					alert.setCancelable(false);
					if(!isConfirmAlert)
						alert.setPositiveButton(buttonLabels[0], new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog,int which) {
								alertDone = true;
							}
						});
					else{
						alert.setPositiveButton(buttonLabels[0],
			                  new AlertDialog.OnClickListener() {
			                public void onClick(DialogInterface dialog, int which) {
			                	alertDone = true;
			                	buttonPressed = OK_BTN_PRESSED;
			                }
			              });
						alert.setNegativeButton(buttonLabels[1],
				                  new AlertDialog.OnClickListener() {
				                public void onClick(DialogInterface dialog, int which) {
				                	alertDone = true;
				                	buttonPressed = CANCEL_BTN_PRESSED;
				                }
				              });
					}
					alert.create();
					alert.show();
				};
			};
			
			activity.runOnUiThread(runnable);
			while(!alertDone){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Log.e("Orochi", "RequestHandler: "+e);
				}
			}
			return buttonPressed;
		};
	}
}
