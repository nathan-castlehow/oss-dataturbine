package imageTools;

/**
 * ImageBank.java ( ImageProcessing )
 * Created: Nov 15, 2009
 * @author Michael Nekrasov
 * @version 1.0
 * 
 * Description: Defines an Image Bank as something that produces images and 
 * 				can be iterated over.
 * 
 * @param <I> Type of Image to store in Image Bank
 */
public interface ImageBank<I extends AnnotatedImage> extends Iterable<I>{
	
	/**
	 * The name of the ImageBank
	 * @return the name
	 */
	public String getName();

}
