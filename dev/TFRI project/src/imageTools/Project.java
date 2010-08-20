package imageTools;

public class Project<R extends Rule, I extends AnnotatedImage> implements Runnable{

	private ImageBank<I> images;
	private ImageProcessor<I, R> processor;
	private ProcessingListner<I> listener;
	
	public Project(ImageProcessor<I, R> processor, ImageBank<I> images){
		this(processor, images,
			new ProcessingListner<I>(){
				public void doPostProcessing(I image) {}	
				public void doPreProcessing(I image){}
				public boolean filter(I image) {return true;} 
			}
		);
	}
	
	public Project(ImageProcessor<I, R> processor, ImageBank<I> images, ProcessingListner<I> listener){
		this.processor = processor;
		this.images = images;
		this.listener = listener;
	}

	@Override
	public void run() {
		for(I image: images){
			if(!listener.filter(image)) continue;
			listener.doPreProcessing(image);
			processor.process(image);
			listener.doPostProcessing(image);
		}
	}
	
}
