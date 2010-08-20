package tfri;
import imageTools.AnnotatedImage;
import imageTools.ImageBank;
import imageTools.ImageProcessor;
import imageTools.blobDetection.BlobRule;

import java.io.IOException;

import com.rbnb.sapi.SAPIException;

public class AutoDetect {

	public static void main(String[] args) {
		ImageBank<AnnotatedImage> bank;
		try {
			bank = TFRI_Factory.generateImageBank("AutoDetect", args);
			ImageProcessor<AnnotatedImage, BlobRule> processor = TFRI_Factory.generateImageProcessor(TFRI_Factory.generateRuleSet());
			
			System.out.println("Processing: "+ bank.getName() +"\n"+processor+"--");

			//Target<Result, AnnotatedImage> target = new RBNBTarget<Result, AnnotatedImage>("Bee Detector" );
			
			long sum =0 ;
			int numFiles =0;
			for(AnnotatedImage img : bank){
				processor.process(img);
				//target.record(img);
				System.out.println("Adding " + img.getAnnotation(TFRI_Factory.BEE_RULE).count() +" to RBNB @ "+ img.getTimestamp());
				sum += img.getAnnotation(TFRI_Factory.BEE_RULE).count();
				numFiles++;
			}
			
			//target.close();
			System.out.println("---\nDone!\t Average Count = " + (sum/numFiles)+"\n");
			
		} catch (IllegalArgumentException e) {}
		catch (IOException e) {
			System.err.println("Error Reading File");
		} catch (SAPIException e) {
			System.err.println("RBNB Error: " + e);
		}
	}

}
