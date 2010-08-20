package tfri;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;

import rbnb.GenericDataSource;

import com.rbnb.sapi.SAPIException;

public class LoadImages {

	private static final int FRAME_RATE = 1000; //in milliseconds
	private static GenericDataSource src;
	private static long timestamp = 0;
	
	public static void main(String[] args) throws SAPIException, IOException, InterruptedException {
		src = new GenericDataSource("Images");
		src.addChannel("img");
		
		
		while(true){
			try{
				GregorianCalendar cTime = new GregorianCalendar();
				cTime.setTimeInMillis(timestamp);
				String sTime = "";
				sTime += cTime.get(Calendar.YEAR);
				sTime += cTime.get(Calendar.MONTH);
				sTime += cTime.get(Calendar.DATE);
				sTime += cTime.get(Calendar.HOUR_OF_DAY); //24 hour clock use HOUR for 12 hr clock
				sTime += cTime.get(Calendar.MINUTE);
				sTime += cTime.get(Calendar.SECOND);
				sTime += cTime.get(Calendar.MILLISECOND);
				
				BufferedImage image = ImageIO.read(new File("FOLDER/"+sTime + ".jpg"));	
				src.put("img", image);
				src.flush();
			}catch(Exception e){ continue;}
			Thread.sleep(FRAME_RATE);				//wait
			timestamp +=FRAME_RATE; 				//adjust timestamp for next image
		}
	}
}
