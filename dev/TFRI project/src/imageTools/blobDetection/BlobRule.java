package imageTools.blobDetection;

import imageTools.AnnotatedImage;
import imageTools.BasicRule;


public class BlobRule extends BasicRule{
	
	private static final long serialVersionUID = -4840712816112273630L;
	
	protected int minHeight, minWidth , maxHeight , maxWidth;
	protected float brightness;
	
	public BlobRule(String name, String description, int minWidth, int minHeight,int maxWidth,
						   int maxHeight, float brightness){
			super(name, description);
			this.minWidth = minWidth;
			this.minHeight = minHeight;
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
			this.brightness = brightness;
			   
		}
	
	public BlobListener genFilter(AnnotatedImage img){
		return new BoundFilter(img.getWidth(), img.getHeight());
	}

	private class BoundFilter implements BlobListener{
		
		private int imgWidth, imgHeight;
		
		public BoundFilter(int imgWidth, int imgHeight){
			this.imgHeight = imgHeight;
			this.imgWidth = imgWidth;
		}
		
		public boolean newBlobDetectedEvent(Blob b) {
			// reject blobs greater size
			if (b.h * imgHeight > maxHeight || b.w * imgWidth > maxWidth)
				return false;
			else if (b.h * imgHeight < minHeight|| b.w * imgWidth < minWidth) 
				return false;
			else
				return true;
		}
	}
	
	public void modify(	int minWidth, int minHeight,int maxWidth,
						int maxHeight, float brightness){
		this.minHeight = minHeight;
		this.minWidth = minWidth;
		this.maxHeight = maxHeight;
		this.maxWidth = maxWidth;
		this.brightness = brightness;
	}
	
	public int getMinWidth(){return minWidth;}
	public int getMinHeight(){return minHeight;}
	public int getMaxWidth(){return maxWidth;}
	public int getMaxHeight(){return maxHeight;}
	public float getBrightness(){return brightness;}
	
	public String toString(){ 
		return super.toString()+
				"[ " + brightness +", "+ minWidth +" x "+ minHeight +
				", " + maxWidth +" x "+ maxHeight + "]";
	}

}
