package org.mbari.android.ahrs;

/*
Copyright 2010 Monterey Bay Aquarium Research Institute

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing 
permissions and limitations under the License.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.rbnb.sapi.*;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

public class AHRSmain extends Activity {
	Source _source;
	ChannelMap _channelMap;
	String _channelNames[] = {"azimuth", "pitch", "roll"};
	int _channel[] = {0, 0, 0};
	String _msgChannelName = "events";
	int _msgChannel;
	private static final String LOGTAG = "AHRS";
	protected boolean _useSimulatedSensor = true;
	protected boolean _pollSensor = false;
	protected float _latestOrientation[] = new float[3];
	protected float _orientationBuf[] = new float[3];
	protected TextView _statusText;
	protected Random _random = new Random();
	protected Button _runButton;
	protected Timer _pollTimer = null;
	protected boolean _hiPitch = false;
	protected String _pitchEventMsg = "";
	protected static final float PITCH_LIMIT_DEG = 45.f;
	protected final String HI_PITCH_MSG = "Pitch exceeds " + PITCH_LIMIT_DEG + " deg";
	protected final String NORMAL_PITCH_MSG = "Pitch is below " + PITCH_LIMIT_DEG + " deg";
	Handler _handler = new Handler();

	// Indicates when data flow has started
	protected boolean _dataStarted = false;

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.i(LOGTAG, "STARTING onCreate()");
		setContentView(R.layout.main);

		EditText serverNameText = (EditText)this.findViewById(R.id.rbnbServer);
		serverNameText.setText("134.89.8.196", TextView.BufferType.EDITABLE);

		_statusText = (TextView)this.findViewById(R.id.statusMsgs);

		_runButton = (Button)this.findViewById(R.id.run_button);
		_runButton.setOnClickListener(
				new Button.OnClickListener() { 
					public void onClick (View v){ start(); }});


		Button quitButton = (Button)this.findViewById(R.id.quit_button);
		quitButton.setOnClickListener(
				new Button.OnClickListener() { 
					public void onClick (View v){ quit(); }});

	}

	protected void quit() {
		// Clean up resources
		if (_pollTimer != null) {
			_pollTimer.cancel();
		}
		if (_source != null) {
			_source.Detach();
			_source.CloseRBNBConnection();
		}

		System.exit(0);
	}
	
	protected void start() {
		Worker worker = new Worker(this);
		new Thread(worker).start();
	}


	/** Write sensor values to RBNB server */
	void writeSensorData(float[] sensorValues) {
		// Log.d(LOGTAG, "writeSensorData()");

		double value[] = {0.0};

		for (int i = 0; i < 3; i++) {
			value[0] = sensorValues[i];
			try {
				_channelMap.PutDataAsFloat64(_channel[i], value);
				_source.Flush(_channelMap);
			}
			catch (Exception e) {
				Log.e(LOGTAG, "Caught exception putting data in channel " + i + ": " + 
						e.getMessage());

			}
		}  
		

		boolean pitchEvent = false;

		if (sensorValues[1] > 45. || sensorValues[1] < -45.) {
			if (!_hiPitch) {
				// Just went into high pitch angle - send event msg
				pitchEvent = true;
				_hiPitch = true;
				_pitchEventMsg = HI_PITCH_MSG;
				Log.w(LOGTAG, HI_PITCH_MSG);
			}
		}
		else {
			if (_hiPitch) {
				// Just went back to normal pitch
				pitchEvent = true;
				_hiPitch = false;
				_pitchEventMsg = NORMAL_PITCH_MSG;
				Log.w(LOGTAG, NORMAL_PITCH_MSG);
			}
		}

		if (pitchEvent) {
			_channelMap.PutMime(_msgChannel, "text/plain");
			try {
				_channelMap.PutDataAsString(_msgChannel, _pitchEventMsg);
				_source.Flush(_channelMap);
			}
			catch (Exception e) {
				Log.e(LOGTAG, "Exception writing event msg: " + e.getMessage());
			}
		}
	}

	void printStatus(String msg) {
		// Append status message
		Log.d(LOGTAG, msg);
		final String text = new String(msg);
		_handler.post(new Runnable() {
			public void run() { _statusText.append(text); }
		});
	}

	class OrientationListener implements SensorEventListener {
		AHRSmain _ahrs;

		OrientationListener(AHRSmain ahrs) {
			_ahrs = ahrs;
		}
		/** Called when sensor value changes; write values to RBNB */
		public void onSensorChanged(SensorEvent sensorEvent) {
			// Log.d(LOGTAG, "onSensorChanged()");
			if (sensorEvent.sensor.getType() != Sensor.TYPE_ORIENTATION) {
				Log.i(LOGTAG, "onSensorChanged() - sensor is not orientation sensor");
				return;
			}

			// Got some data
			_dataStarted = true;


			if (_pollSensor) {
				// Fill in latest orientation data array. Polling timer task will 
				// write these to DataTurbine server.
				synchronized (this) {
					for (int i = 0; i < 3; i++) {
						_latestOrientation[i] = sensorEvent.values[i];
					}
				}
			}
			else {
				// Write orientation values to DataTurbine server.
				_ahrs.writeSensorData(sensorEvent.values);
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

	}

	protected class PollingTask extends TimerTask {

		public void run() {
			// Log.d(LOGTAG, "PollingTask.run()");
			// Poll orientation sensor for data
			if (_useSimulatedSensor) {
				// Fake some orientation data     	
				_latestOrientation[0] = _random.nextFloat();
				_latestOrientation[1] = _random.nextFloat();
				_latestOrientation[2] = _random.nextFloat();
				_dataStarted = true;
			}

			if (!_dataStarted) {
				Log.d(LOGTAG, "PollingTask.run(): Data not yet started - don't write yet");
				return;
			}

			synchronized (this) {
				for (int i = 0; i < 3; i++) {
					_orientationBuf[i] = _latestOrientation[i];
				}
			}

			// Log.d(LOGTAG, "PollingTask.run(): writeSensorData()");
			writeSensorData(_orientationBuf);

			// Log.d(LOGTAG, "PollingTask.run() complete");
		}
	}

	
	class Worker implements Runnable {
		AHRSmain _activity;
		
		Worker(AHRSmain activity) {
			_activity = activity;
		}
	
		public void run() {

			// Read name of RBNB server host
			EditText serverNameText = (EditText)_activity.findViewById(R.id.rbnbServer);
			String serverName = serverNameText.getText().toString();

			CheckBox checkBox = (CheckBox)_activity.findViewById(R.id.useSimulatedSensor);

			if (checkBox.isChecked()) {
				_useSimulatedSensor = true;
			}
			else {
				_useSimulatedSensor = false;
			}

			checkBox = (CheckBox)_activity.findViewById(R.id.pollSensor);
			if (checkBox.isChecked()) {
				_pollSensor = true;
			}
			else {
				_pollSensor = false;
			}

			if (_useSimulatedSensor) {
				_pollSensor = true;
			}

			Log.d(LOGTAG, "useSimulatedSensor=" + _useSimulatedSensor);

			// Create RBNB source
			int cacheFrames = 1024;
			int archiveFrames = 1024;
			Log.i(LOGTAG, "Create source");
			_source = new Source(cacheFrames, "append", archiveFrames);
			
			// First close the connection, just to eliminate any possible "stranded"
			// connection
			_source.CloseRBNBConnection();
			
			try {
				Log.v(LOGTAG, "Connect source to RBNB server at " + serverName);
				printStatus("Connecting to " + serverName + "...");
				_source.OpenRBNBConnection(serverName, "Nexus-AHRS");
				printStatus("Connected.");
			}
			catch (Exception e) {
				Log.e(LOGTAG, "Exception while connecting to server at " + 
						serverName + ": " + e.getMessage());
				Log.e(LOGTAG, Log.getStackTraceString(e));
				printStatus("Error connecting to " + serverName + ": " + e.getMessage());
				return;
			}

			Log.i(LOGTAG, "Create DataTurbine channel map");
			_channelMap = new ChannelMap();

			String channelName = "";
			try {			
				for (int i = 0; i < _channelNames.length; i++) {
					channelName = _channelNames[i];
					_channel[i] = _channelMap.Add(_channelNames[i]);
					_channelMap.PutUserInfo(_channel[i], "units=degrees, property=value");
					_channelMap.PutMime(_channel[i], "application/octet-stream");
				}

				channelName = _msgChannelName;
				_msgChannel = _channelMap.Add(_msgChannelName);

			}
			catch (Exception e) {
				Log.e(LOGTAG, "Exception while adding " + channelName + " to channel map: " + e);
				Log.e(LOGTAG, Log.getStackTraceString(e));
				return;
			}      
			
			try {
			  _source.Register(_channelMap);
			}
			catch (Exception e) {
				Log.e(LOGTAG, "Error while registering channelMap with source: " + e.getMessage());
				Log.e(LOGTAG, Log.getStackTraceString(e));
			}
			
			// Disable "run" button
			_handler.post(new Runnable() {
				public void run() { _runButton.setEnabled(false); }
			});


			// Get orientation sensor
			SensorManager sensorManager = 
				(SensorManager )_activity.getSystemService(Context.SENSOR_SERVICE);

			List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

			if (sensors.size() > 0) {
				// Read orientation sensor data when it changes
				final SensorEventListener sensorListener = new OrientationListener(_activity);		
				sensorManager.registerListener(sensorListener, 
						sensors.get(0), 
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			else {
				Log.e(LOGTAG, "Orientation sensor not found!");
			}

			if (_pollSensor) {
				// Schedule polling task, which writes orientation to DataTurbine server
				// at specified interval.
				Log.d(LOGTAG, "schedule PollingTask...");
				int pollInterval = 100;
				_pollTimer = new Timer();
				_pollTimer.schedule(new PollingTask(), 0, pollInterval);
			}
		}
		
	}

}
