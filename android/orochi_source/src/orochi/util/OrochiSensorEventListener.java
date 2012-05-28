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

package orochi.util;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class OrochiSensorEventListener implements SensorEventListener {

	public static final int STOPPED = 0;
	public static final int STARTING = 1;
	public static final int RUNNING = 2;
	public static final int FAILED_TO_START = 3;
	public static int START_TIMEOUT = 2000;
	
	private int timeout = 30000;
	
	protected long lastAccessTime;
	protected int sensorType;
	protected int sensorRate;
	protected SensorManager sensorManager;	
	protected Sensor sensor;	
	
	private int status;
	
	public OrochiSensorEventListener(Context context, int sensorType, int sensorRate){
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		this.sensorType = sensorType;
		this.sensorRate = sensorRate;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public int getStatus(){
		return status;
	}
	
	public void setTimeout(int timeout){
		this.timeout = timeout;
	}
	
	public int getTimeout(){
		return timeout;
	}
	
	public boolean startIfNotRunning(){
		if (status != RUNNING) {
			int sResult = start();
			if (sResult == FAILED_TO_START) {
				return false;
			}
			// wait until running
			int timeout = START_TIMEOUT;
			while ((status == STARTING) && (timeout > 0)) {
				timeout = timeout - 100;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (timeout == 0) {
				return false;						
			}
		}
		return true;
	}
	
    public int start() {

        if ((status == RUNNING) || (status == STARTING)) {
        	return status;
        }

        List<Sensor> list = sensorManager.getSensorList(sensorType);
        
        if ((list != null) && (list.size() > 0)) {
            sensor = list.get(0);
            sensorManager.registerListener(this, sensor, sensorRate);
            lastAccessTime = System.currentTimeMillis();
            status = STARTING;
        }       
        else {
        	// If accelerometer not found
        	status = FAILED_TO_START;
        }
        
        return status;
    }

    public void stop() {
        if (status != STOPPED) {
        	sensorManager.unregisterListener(this);
        }
        status = STOPPED;
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub		
	}

	protected abstract void onSensorChanged(float[] values);
	
	@Override
	public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != sensorType) {
            return;
        }

        if (status == STOPPED) {
        	return;
        }

        long timestamp = System.currentTimeMillis();
        onSensorChanged(event.values);          

        status = RUNNING;

        // stop accelerometer sensor to save power, if unused for a long time(timeout)
		if ((timestamp - lastAccessTime) > timeout) {
			stop();
		}
		
	}

}
