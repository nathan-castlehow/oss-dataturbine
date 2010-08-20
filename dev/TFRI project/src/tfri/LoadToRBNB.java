package tfri;
import rbnb.Util;
import imageTools.AnnotatedImage;
import imageTools.ImageBank;

public class LoadToRBNB {

	private static String rbnbHostName = "localhost";
	private static int cacheSize = 10240;
	private static int archiveSize = cacheSize * 10;
	
	public static void main(String[] args) {
		try {
			ImageBank<AnnotatedImage> bank = TFRI_Factory.generateImageBank("ImageBankToRBNB", args);
			Util.loadBankIntoRBNB(bank,cacheSize,archiveSize,rbnbHostName,bank.getName());
		} catch (Exception e) {
			System.err.println("Error Loading Bank " );
			 e.printStackTrace();
		}
	}
	
	

}
