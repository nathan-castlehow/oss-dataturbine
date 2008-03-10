package org.nees.buffalo.video;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class JPEGScaler {
	
	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;

	
	private final String rbnbHostName = "neesub4.eng.buffalo.edu";
	private final String rbnbSinkName = "JPEGScalerSink";
	private final String rbnbPluginName = "JPEGScaler";
	
	private final String inputChannelName;
	private final String outputChannelName;
	
	private final int width;
	private final int height;

	public JPEGScaler(String inputChannelName, String outputChannelName, int width, int height) {
		this.inputChannelName = inputChannelName;
		this.outputChannelName = outputChannelName;
		this.width = width;
		this.height = height;
		
		execute();		
	}
	
	private void execute() {
		// open connection to RBNB server as a sink
		Sink sink = new Sink();
		try {
			sink.OpenRBNBConnection(rbnbHostName, rbnbSinkName);
		} catch (SAPIException e) {
			System.err.println("Failed to connect to RBNB server for sink.");
			return;				
		}
				
		// select input channel to monitor
		ChannelMap sinkRequestMap = new ChannelMap();
		int inputChannelIndex = -1;
		try {
			inputChannelIndex = sinkRequestMap.Add(inputChannelName);
		} catch (SAPIException e) {
			System.err.println("Failed to add input channel to channel map.");
			return;				
		}
			
		PlugIn plugin = new PlugIn();
		try {
			plugin.OpenRBNBConnection(rbnbHostName, rbnbPluginName);
		} catch (SAPIException e) {
			System.err.println("Failed to connect to RBNB server for plugin.");
			return;				
		}
		
		// select channel to output data to
		ChannelMap pluginRegisterMap = new ChannelMap();
		int outputChannelIndex = -1;
		try {
			outputChannelIndex = pluginRegisterMap.Add(outputChannelName);
		} catch (SAPIException e) {
			System.err.println("Failed to add output channel to channel map.");
			return;				
		}			
		pluginRegisterMap.PutMime(outputChannelIndex, "image/jpeg");
		
		try {
			plugin.Register(pluginRegisterMap);
		} catch (SAPIException e) {
			System.err.println("Failed to register output channel map.");
			return;				
		}
		
		byte[] imageData;
		double startTime;
		double durationTime;
		String timeReference;
		int imageIndex = 0;
		while (true) {
			
			PlugInChannelMap pluginFetchMap = null;
			try {
				pluginFetchMap = plugin.Fetch(1000);
			} catch (SAPIException e) {
				System.err.println("Failed to fetch input data, retrying.");
				continue;
			}					
				
			// no data received, try again
			if (pluginFetchMap.GetIfFetchTimedOut()) {
				System.err.println("Data request timed out, retrying.");		
				continue;
			}

			startTime = pluginFetchMap.GetRequestStart();
			durationTime = pluginFetchMap.GetRequestDuration();
			timeReference = pluginFetchMap.GetRequestReference();
			
			try {
				sink.Request(sinkRequestMap, startTime, durationTime, timeReference);
			} catch (SAPIException e) {
				System.err.println("Failed to monitor input channel.");
				continue;				
			}
			
			// see if any data is available from source
			ChannelMap sinkFetchMap = null;
			try {
				sinkFetchMap = sink.Fetch(1000);
			} catch (SAPIException e) {
				System.err.println("Failed to fetch input data, retrying.");
				continue;
			}		
			
			// no data received, try again
			if (sinkFetchMap.GetIfFetchTimedOut()) {
				System.err.println("Data request timed out, retrying.");		
				continue;
			}			

			imageData = sinkFetchMap.GetData(inputChannelIndex);
			imageData = scaleImage(imageData, width, height, 0.75f);
			if (imageData == null) {
				continue;
			}
			
			// put data in channel map, preserving original time stamp
			try {
				pluginFetchMap.PutTime(startTime, durationTime);
				pluginFetchMap.PutDataAsByteArray(outputChannelIndex, imageData);
			} catch (SAPIException e) {
				System.err.println("Failed to put output data to channel map, skipping.");
				continue;				
			}	
				
			// send data to RBNB server
			try {
				plugin.Flush(pluginFetchMap);
			} catch (SAPIException e) {
				System.err.println("Failed to flush output data to server, skipping.");
				continue;				
			}	
			
		}
		
	}
	
	private byte[] scaleImage(byte[] imageData, int width, int height, float quality) {
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(imageData));
	
		// decode JPEG image to raw image
		BufferedImage bi;
		try {
			bi = decoder.decodeAsBufferedImage();
		} catch (IOException e){
			System.err.println("Failed to decode input JPEG image, skipping.");
			return null;
		}
	
		// scale both width and height
		float widthScale = width/bi.getWidth();
		float heightScale = height/bi.getHeight();
		AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(widthScale, heightScale), null);
		bi = op.filter(bi, null);
				
		// encode scaled image as JPEG image				
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
		param.setQuality(quality, false);			
		try {
			encoder.encode(bi, param);
		} catch (IOException e) {
			System.err.println("Failed to encode output JPEG image, skipping.");
			return null;
		}				

		// get JPEG image data as byte array
		return out.toByteArray();
	}

	public static void main(String[] args) {
		String inputChannelName = args[0];
		String outputChannelName = args[1];
		int width = Integer.parseInt(args[2]);
		int height = Integer.parseInt(args[2]);
		new JPEGScaler(inputChannelName, outputChannelName, width, height);		
	}
}
