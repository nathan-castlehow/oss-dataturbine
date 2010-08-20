package imageTools.blobDetection;
import imageTools.AnnotatedImage;
import imageTools.ImageProcessor;
import imageTools.Location;
import imageTools.Result;
import imageTools.RuleSet;

public class BlobProcessor<I extends AnnotatedImage> extends ImageProcessor<I, BlobRule>{

	public static boolean VERBOSE = true;
	
	public BlobProcessor(RuleSet<BlobRule> rules) {
		super(rules);
	}
		
	public void process(I img){
		int index = 0;
		
		for(BlobRule rule : rules){
			BlobDetection blobDetector = new BlobDetection(img.getWidth(), img.getHeight());
			blobDetector.setPosDiscrimination(true);
			blobDetector.setThreshold(rule.getBrightness());
			blobDetector.activeCustomFilter(rule.genFilter(img));
			
			blobDetector.computeBlobs(img.getRGB(0, 0, img.getWidth(), img.getHeight(),
					new int[img.getWidth() * (img.getHeight())], 0, img.getWidth()));
			
			
			Result result = new Result(rule);
			//Generate results
			for (int i = 0; i < blobDetector.getBlobNb(); i++){
				Blob blob = blobDetector.getBlob(i);
				if (blob != null){
					result.add( new Location(
									(int) (blob.xMin * img.getWidth()),
									(int) (blob.yMin * img.getHeight()),
									(int) (blob.w * img.getWidth()),
									(int) (blob.h * img.getHeight())
									)
						  		);
				}
			}
			
			//Annotate Image
			img.addAnnotation(result);
			
			if(VERBOSE)
				System.out.println("> "+ img);
			
			index++;
		}
	}
}
