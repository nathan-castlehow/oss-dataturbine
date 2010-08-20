package imageTools;

public interface Target<R extends Result, I extends AnnotatedImage> {

	public void record(R result);
	public void record(I image);
	public void close();
}
