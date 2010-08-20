package imageTools;

public interface ProcessingListner<I extends AnnotatedImage> {

	public void doPreProcessing(I image);
	
	public void doPostProcessing(I image);
	
	public boolean filter(I image);
}
