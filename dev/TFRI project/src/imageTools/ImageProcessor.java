package imageTools;

/**
 * ImageProcessor.java ( imageTools )
 * Created: Dec 5, 2009
 * @author Michael Nekrasov
 * 
 * Description: Abstract class for an image processor.
 * 
 * @param <I> Type of Images to process
 * @param <R> Type of Rules to use
 */
public abstract class ImageProcessor<I extends AnnotatedImage, R extends Rule>{
	protected RuleSet<R> rules;
	
	/**
	 * Creates a new ImageProcessor with a certain set of rules
	 * @param rules
	 */
	public ImageProcessor(RuleSet<R> rules) {
		this.rules = rules;
	}
	
	/**
	 * Process an Image
	 * @param img to process
	 */
	public abstract void process(I img);
	
	/**
	 * Process an entire ImageBank of Images
	 * @param bank to process
	 */
	public void process(ImageBank<I> bank){
		for(I img : bank){
			process(img);
		}
	}
	
	/**
	 * Get all the rules this processor is using
	 * @return the set of rules
	 */
	public RuleSet<R> getRules(){
		return rules;
	}
	
	public String toString() {
		String str = "";
		for(R rule : rules)
			str += "# "+rule + "\n";
		return str;
	}
}
