package org.mbari.android.camera;
/*
Copyright 2010 Monterey Bay Aquarium Research Institute

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing 
permissions and limitations under the License.
 */

import java.io.IOException;

import org.mbari.android.camera.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.rbnb.sapi.*;

public class CameraTest extends Activity {

	Preview _preview;
	Button _shutterButton;
	static final String LOGTAG = "CameraTest";
	EditText _serverNameText;
	boolean _rbnbOpened = false;
	String _prevServerName = "";
	Source _source;
	ChannelMap _channelMap;
	int _cameraChannel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		_preview = new Preview(this);
		((FrameLayout) findViewById(R.id.preview)).addView(_preview);

		_shutterButton = (Button) findViewById(R.id.shutter_button);
		_shutterButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				_preview.camera.takePicture(shutterCallback, rawCallback,
						jpegCallback);
			}
		});

		_serverNameText = (EditText)this.findViewById(R.id.rbnbServer);
		_serverNameText.setText("134.89.8.196", TextView.BufferType.EDITABLE);

		Log.d(LOGTAG, "onCreate'd");
	}


	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(LOGTAG, "onShutter'd");
		}
	};

	/** Handles data for raw picture */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(LOGTAG, "onPictureTaken - raw");
		}
	};

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			
			Worker worker = new Worker(data);
			new Thread(worker).start();
		
			Log.d(LOGTAG, "onPictureTaken - jpeg");
			camera.startPreview();
		}
	};

	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		private static final String TAG = "Preview";

		SurfaceHolder mHolder;
		public Camera camera;

		Preview(Context context) {
			super(context);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, acquire the camera and tell it where
			// to draw.
			camera = Camera.open();
			try {
				camera.setPreviewDisplay(holder);

				camera.setPreviewCallback(new PreviewCallback() {

					public void onPreviewFrame(byte[] data, Camera arg1) {
						Preview.this.invalidate();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// Surface will be destroyed when we return, so stop the preview.
			// Because the CameraDevice object is not a shared resource, it's very
			// important to release it when the activity is paused.
			camera.stopPreview();
			camera.release();
			camera = null;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			// Now that the size is known, set up the camera parameters and begin
			// the preview.
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(w, h);
			try {
				camera.setParameters(parameters);
			}
			catch (Exception e) {
				Log.e(LOGTAG, "Exception from camera.setParameters(): " + e.getMessage());
			}
			camera.startPreview();
		}

		@Override
		public void draw(Canvas canvas) {
			super.draw(canvas);
			Paint p = new Paint(Color.RED);
			Log.d(TAG, "draw");
			canvas.drawText("PREVIEW", canvas.getWidth() / 2,
					canvas.getHeight() / 2, p);
		}
	}
	
	class Worker implements Runnable {
		byte[] _data;
		
		Worker(byte[] data) {
			_data = data;
		}
		
		public void run() {
			// Publish data to RBNB
			String serverName = _serverNameText.getText().toString();
			if (!serverName.equals(_prevServerName)) {
				// New server name - we'll need to (re-)connect source
				_rbnbOpened = false;
				_prevServerName = new String(serverName);
			}

			if (!_rbnbOpened) {
				// Create RBNB source
				int cacheFrames = 10;
				int archiveFrames = 1024;
				Log.i(LOGTAG, "Create source");
				_source = new Source(cacheFrames, "create", archiveFrames);
				
				// Close first, just to eliminate any "stranded" connection
				_source.CloseRBNBConnection();
				
				try {
					Log.i(LOGTAG, "Connect source to RBNB server at " + serverName);
					_source.OpenRBNBConnection(serverName, "AndroidCamera");
					Log.i(LOGTAG, "Connected.");
				}
				catch (Exception e) {
					Log.e(LOGTAG, "Exception while connecting to server at " + 
							serverName + ": " + e.getMessage());
					Log.e(LOGTAG, Log.getStackTraceString(e));
					return;
				}

				try {
					Log.i(LOGTAG, "Create DataTurbine channel map");
					_channelMap = new ChannelMap();

					_cameraChannel = _channelMap.Add("Camera");
					_channelMap.PutUserInfo(_cameraChannel, "phone camera");
					_channelMap.PutMime(_cameraChannel, "image/jpeg");

					_source.Register(_channelMap);
				}
				catch (Exception e) {
					Log.e(LOGTAG, "Exception while populating channel map: " + e);
					Log.e(LOGTAG, Log.getStackTraceString(e));
					return;
				}      
				_rbnbOpened = true;
			}
			// Write the image to RBNB server
			try {
				_channelMap.PutMime(_cameraChannel, "image/jpeg");
				_channelMap.PutDataAsByteArray(_cameraChannel, _data);
				_source.Flush(_channelMap);
			}
			catch (Exception e) {
				Log.e(LOGTAG, "Exception while putting jpeg image to channel map");
				Log.e(LOGTAG, Log.getStackTraceString(e));
			}


		}
	}
}