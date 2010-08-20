package tfri;
import imageTools.AnnotatedImage;
import imageTools.ImageBank;
import imageTools.ImageProcessor;
import imageTools.blobDetection.BlobRule;

import java.io.IOException;
import java.util.Iterator;


public class Calibrate implements ControlListener {

	private GUI gui;
	private AnnotatedImage img;
	private ImageProcessor<AnnotatedImage, BlobRule> processor;
	private Iterator<AnnotatedImage> bankIterator;
	
	public Calibrate(ImageBank<AnnotatedImage> bank) throws IOException{
		this.bankIterator = bank.iterator();
		
		processor = TFRI_Factory.generateImageProcessor(TFRI_Factory.generateRuleSet());		
		
		if(bankIterator.hasNext()){
			img = bankIterator.next();
			gui = new GUI(this);
			update();
		}
		else{
			System.err.println("No Image to display");
		}
	}

	//GUI Controls
	public boolean hasNext() {
		return bankIterator.hasNext();
	}

	public void update(){
		
		//modify rules
		processor.getRules().get(TFRI_Factory.BEE_RULE.getName()).modify(gui.getMinWidth(), gui.getMinHeight(),
				gui.getMaxHeight(), gui.getMaxWidth(), gui.getBrightness());
		
		processor.process(img);
		gui.update(img);
	}
	
	public void nextImage(){
		img = bankIterator.next();
		update();
	}
	
	public static void main(String[] args){
		try {
			new Calibrate(TFRI_Factory.generateImageBank("Calibrate", args));
		} catch (IllegalArgumentException e) {}
		catch (Exception e) {
			System.err.println("Error Loading Archive");
			e.printStackTrace();
		}
		
	}
}
