package rbnb;

import imageTools.AnnotatedImage;
import imageTools.ImageBank;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

public class RBNB_ImageBank implements ImageBank<AnnotatedImage>, Iterator<AnnotatedImage>{
	
	private String name;
	private Sink sink;
	private ChannelMap cmap;
	private AnnotatedImage next;
	private Date start, end;
	

	public RBNB_ImageBank(String name, String rbnbHostName) throws SAPIException{
		this(name, rbnbHostName, null, null);
	}
	
	public RBNB_ImageBank(String name, String rbnbHostName, Date start) throws SAPIException{
		this(name, rbnbHostName, start, null);
	}
	
	public RBNB_ImageBank(String name, String rbnbHostName, Date start, Date end) throws SAPIException{	
		this.name = name;
		this.start = start;
		this.end = end;
		
		sink = new Sink();
		sink.OpenRBNBConnection(rbnbHostName, name+" Reader");
		
		iterator();
		//TODO: ADD DISCONECT LOGIC
		
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public AnnotatedImage next() {
		AnnotatedImage cur =  next;
		loadNextImage();
		return cur;
	}
	
	private void loadNextImage(){
		
		try {
			ChannelMap lmap = sink.Fetch(1000000);
			byte bytes[] = lmap.GetDataAsByteArray(0)[0];

			BufferedImage orig = ImageIO.read( new ByteArrayInputStream(bytes) );
			next = new AnnotatedImage(orig, "RBNB Image");
			//TODO: timestamp image
		} catch (Exception e) {
			next = null;
		}		
	}
	
	public String getName(){ return name;}

	@Override
	public Iterator<AnnotatedImage> iterator() {
		try{
			cmap = new ChannelMap();
			cmap.Add(name+"/Image");
			
			if(start != null)
				sink.Subscribe(cmap, start.getTime()/1000, 1, "next");
			else
				sink.Subscribe(cmap);
			
			loadNextImage();
		}catch(Exception e){
			next = null;
		}
		return this;
	}


	@Override
	public void remove() {
		throw new IllegalArgumentException("Web Arcive does not support removal");		
	}

}
