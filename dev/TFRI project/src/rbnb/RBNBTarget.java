package rbnb;

import imageTools.AnnotatedImage;
import imageTools.Result;
import imageTools.Target;

import java.awt.Color;
import java.io.IOException;

import tfri.TFRI_Factory;


import com.rbnb.sapi.SAPIException;

public class RBNBTarget<R extends Result, I extends AnnotatedImage> extends GenericDataSource implements Target<R, I>{
	
	public RBNBTarget(String srcName) throws SAPIException {
		super(srcName);
		addChannel("count", "application/octet-stream");
		addChannel("image", "image/jpeg");
	}

	public RBNBTarget(String srcName, String serverPath, int port,
			int cacheSize, int archiveSize)
			throws SAPIException {
		super(srcName, serverPath, port, cacheSize, archiveSize);
		addChannel("count", "application/octet-stream");
		addChannel("image", "image/jpeg");
	}
	
	public RBNBTarget(String srcName, String serverPath, int port,
			int cacheSize, int archiveSize, String archiveMode)
			throws SAPIException {
		super(srcName, serverPath, port, cacheSize, archiveSize, archiveMode);
		addChannel("count", "application/octet-stream");
		addChannel("image", "image/jpeg");
	}

	@Override
	public void record(R result) {
		try {
			put("count", result.count());
			flush();
		} catch (SAPIException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void record(I image) {
		int count = image.getAnnotation(TFRI_Factory.BEE_RULE).count();
		try {
			put("count", count, image.getTimestamp());
			put("image", image.graphicallyAnnotate(TFRI_Factory.BEE_RULE, Color.RED), image.getTimestamp());
			flush();
		} catch (SAPIException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
