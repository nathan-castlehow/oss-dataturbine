package tfri;
import imageTools.AnnotatedImage;
import imageTools.ImageBank;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;


/**
 * TFRIWebArchive
 * Created: Dec 2, 2009
 * @author Michael Nekrasov
 * @version 1.0
 * 
 * Description: Used to connect to online Image Archives at the 
 * 				Taiwan Forestry Research Institute.
 *
 */
public class TFRIWebArchive implements ImageBank<AnnotatedImage>, Iterator<AnnotatedImage> {

	//Variable that define the archive
	private final String archiveName, archivePath, cameraName; 
	private final int startYear, startMonth, startDay, startHour, startMinute;
	private final int endYear, endMonth, endDay, endHour, endMinute;
	
	//Variables that keep track of current Image
	private AnnotatedImage nextImg;
	private int curYear, curMonth, curDay, curHour, curMinute;
	
	/**
	 * Construct an Image Bank for the given Web Archive
	 * 
	 * Assumes Start Date is 01/01/2008 5:00 
	 * Assumes End date is current date/time in local time zone
	 * 
	 * @param archiveName Name of Archive (Used for visual display)
	 * @param archivePath Full URL to archive (ex: http://srb2.tfri.gov.tw): 
	 * @param cameraName Name of Camera as it appears in URLs (ex: SP_CAMERA-2)
	 * 
	 * @see BasicImageBank
	 */
	public TFRIWebArchive(String archiveName, String archivePath, String cameraName){
		this(archiveName, archivePath, cameraName, new GregorianCalendar(2008,1,1,5,0).getTime());
	}
	
	/**
	 * Construct an Image Bank for the given Web Archive
	 * 
	 * Specify start date.
	 * Assumes end date is current date/time in local time zone
	 * 
	 * @param archiveName Name of Archive (Used for visual display)
	 * @param archivePath Full URL to archive (ex: http://srb2.tfri.gov.tw): 
	 * @param cameraName Name of Camera as it appears in URLs (ex: SP_CAMERA-2)
	 * @param startYear 4 digit year
	 * 
	 * @see BasicImageBank
	 */
	public TFRIWebArchive(String archiveName, String archivePath, String cameraName,Date start){
		this(archiveName, archivePath, cameraName,start,Calendar.getInstance().getTime());
	}
	
	/**
	 * Construct an Image Bank for the given Web Archive
	 * 
	 * Specify start and end date to pull images from.
	 * 
	 * @param archiveName Name of Archive (Used for visual display)
	 * @param archivePath Full URL to archive (ex: http://srb2.tfri.gov.tw): 
	 * @param cameraName Name of Camera as it appears in URLs (ex: SP_CAMERA-2)
	 * @param startYear 4 digit year
	 * @param endYear 4 digit year
	 * 
	 * @see BasicImageBank
	 */
	public TFRIWebArchive(String archiveName, String archivePath, String cameraName,
							Date start, Date end){
		
		this.archiveName = archiveName;
		this.archivePath = archivePath;
		this.cameraName = cameraName;
		
		GregorianCalendar sCal = new GregorianCalendar();
		GregorianCalendar eCal = new GregorianCalendar();
		sCal.setTime(start);
		eCal.setTime(end);
		
		
		this.startYear = 	sCal.get(Calendar.YEAR);
		this.startMonth = 	sCal.get(Calendar.MONTH) +1;
		this.startDay = 	sCal.get(Calendar.DATE);
		this.startHour =	sCal.get(Calendar.HOUR_OF_DAY);
		this.startMinute =	sCal.get(Calendar.MINUTE);
		this.endYear = 		eCal.get(Calendar.YEAR);
		this.endMonth = 	eCal.get(Calendar.MONTH) + 1;
		this.endDay = 		eCal.get(Calendar.DATE);
		this.endHour = 		eCal.get(Calendar.HOUR_OF_DAY);
		this.endMinute = 	eCal.get(Calendar.MINUTE);
		
		//Fetch First Image
		iterator();
		next();
	}

	/**
	 * Advances the date to the next valid y/m/d h:m
	 */
	private void nextDate(){
		curMinute++;
		if(curMinute >= 60){
			curMinute = 0;
			curHour++;
			if(curHour > endHour){
				curHour = 5;
				curDay++;
				if(curDay > 31){
					curDay=1;
					curMonth++;
					if(curMonth > 12){
						curMonth = 1;
						curYear++;
					}
				}
			}
		}
	}
	
	/**
	 * Gets the name of the archive
	 */
	public String getName(){ return archiveName;}
	
	/**
	 * Checks if there is another Image in the Archive
	 * @return true if there is
	 * 
	 * @see BasicImageBank
	 */
	@Override
	public boolean hasNext() {
		return nextImg != null;
	}


	/**
	 * Returns the Next Element in the Web Archive
	 * 
	 * Keeps checking the archive for Images at corresponding URLS.
	 * Gives up once END Year/Month/Day Hour:Minute have been reached
	 * 
	 * @return image containing the data or null if no such image was found 
	 * 
	 * @see AnnotatedImage 
	 * @see BasicImageBank
	 */
	@Override
	public AnnotatedImage next() {
		AnnotatedImage currentImg = nextImg;
		nextImg = null;
		while(nextImg == null){
			if(curYear > endYear) break;
			if(curYear == endYear && curMonth > endMonth ) break;
			if(curYear == endYear && curMonth == endMonth && curDay > endDay) break;
			if(curYear == endYear && curMonth == endMonth && curDay == endDay && curMinute > endMinute) break;
			String target = toPath(curYear, curMonth, curDay, curHour, curMinute);
			try {
				URL path = new URL(target);
				nextImg = new AnnotatedImage(path);
				nextImg.setTimestamp((new GregorianCalendar(curYear,curMonth,curDay,curHour,curMinute,0)).getTimeInMillis()/1000);
			} catch (Exception e) {
				System.out.println("Fail to load: " + target);
				nextImg = null;
			}
			nextDate();
		}
		return currentImg;
	}
	
	/**
	 * Helper Function that creates a URL to the archive
	 * @param year 4 digit year
	 * @return the URL to that image
	 */
	private String toPath(int year, int month, int day,int hour, int minute){
		String y = ""+year;
		String m = XX(month);
		String d = XX(day);
		String h = XX(hour);
		String n = XX(minute);
		
		return archivePath + "/images/"+y +"/"+m+"/"+d+"/"+cameraName+"-"+y+m+d+"-"+h+n+"00.jpg";
	}

	/**
	 * Helper Function that returns a two digit representation of 1&2 digit integers
	 * @param num 1 or 2 gigit number to convert
	 * @return two digit String representation
	 */
	private String XX(int num){
		if(num < 10) return "0" + num;
		else return ""+num;
	}

	@Override
	public Iterator<AnnotatedImage> iterator() {
		curYear = startYear;
		curMonth = startMonth;
		curDay = startDay;
		curHour = startHour;
		curMinute = startMinute;
		return this;
	}


	@Override
	public void remove() {
		throw new IllegalArgumentException("Web Arcive does not support removal");
	}
	
	
}