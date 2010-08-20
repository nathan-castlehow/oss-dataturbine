package imageTools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;

public class AnnotatedImage extends BufferedImage implements Iterable<Result>
 {
	private String name;
	private long timestamp;
	private HashMap<String, Result> results = new HashMap<String, Result>();
	
	/**
	 * Constructs an image capable of being annotated
	 * @param input File to load from
	 * @throws IOException
	 */
	public AnnotatedImage(File input) throws IOException{
		this(ImageIO.read(input), input.getName());
	}
	
	/**
	 * Constructs an image capable of being annotated
	 * @param input URL to load from
	 * @throws IOException
	 */
	public AnnotatedImage(URL input) throws IOException{
		this(ImageIO.read(input), "");
		int lastSlash = input.getFile().lastIndexOf('/');
		name = input.getFile().substring(lastSlash >=0? lastSlash + 1: 0);
	}
	
	/**
	 * Constructs an image capable of being annotated
	 * @param img
	 */
	public AnnotatedImage(AnnotatedImage img){
		this(img, img.name, img.timestamp);
	}
	
	/**
	 * Constructs an image capable of being annotated
	 * @param img
	 * @param name
	 */
	public AnnotatedImage(BufferedImage img, String name){
		this(img, name, System.currentTimeMillis());
	}
	
	/**
	 * Constructs an image capable of being annotated
	 * @param img
	 * @param name 
	 * @param timestamp
	 */
	public AnnotatedImage(BufferedImage img, String name, long timestamp){
		super(img.getWidth(), img.getHeight(), img.getType());
		this.setData(img.getData());
		this.timestamp = System.currentTimeMillis();
		this.name = name;
	}


	/**
	 * Creates an iterator over the results associated with this image
	 */
	@Override
	public Iterator<Result> iterator() {
		return results.values().iterator();
	}
	
	/**
	 * Sets the image's timestamp
	 * @param timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the timestamp associated with this image
	 * @return timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	public void crop(Rectangle rect) {
		setData(getData(rect));
	}
	
	public String toString(){
		String str = timestamp + "\t"+ getName();
		for(Result result : results.values() )
			str+= "\t" + result;
		return str;
	}

	/**
	 * Gets the name of the image
	 * @return the name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Adds a result to the image annotation
	 * @param result the result to add
	 */
	public void addAnnotation(Result result){
		results.put(result.getName(), result);
	}
	
	/**
	 * Gets the Result for a particular rule
	 * @param rule whose result to get
	 * @return the result
	 */
	public Result getAnnotation(Rule rule){
		return results.get(rule.getName());
	}
	
	
	/**
	 * Draws bounding polygons around the objects over all the results
	 * Uses random colors
	 * @return the resulting image
	 */
	public AnnotatedImage graphicallyAnnotate(){
		AnnotatedImage img = this;
		Random rnd = new Random();
		for(Result result: results.values()){
			img = img.graphicallyAnnotate(result.getCorrespondingRule(), new Color(rnd.nextInt()));
		}
		return img;
	}
	
	/**
	 * Draws bounding polygons around the objects specified by rule
	 * @param rule whose result to graphically annotate
	 * @param color of bounding box
	 * @return the resulting image
	 */
	public AnnotatedImage graphicallyAnnotate(Rule rule, Color color){
		return graphicallyAnnotate( results.get(rule.getName()), color);
	}
	
	/**
	 * Draws bounding polygons around the objects 
	 * @param rules array of rules to graphically annotate
	 * @param colors a matching array of colors to annotate them with
	 * @return the resulting image
	 * @throws IllegalArgumentException if the number of rules and colors do not match
	 */
	public AnnotatedImage graphicallyAnnotate(Rule[] rules, Color[] colors) throws IllegalArgumentException{
		if(rules.length != colors.length)
			throw new IllegalArgumentException("Number of Rules and Colors must match");
		
		AnnotatedImage img = this;
		for(int i=0; i< rules.length; i++){
			img = img.graphicallyAnnotate(rules[i], colors[i]);
		}
		return img;
	}
	
	/**
	 * Draws bounding polygons around the objects 
	 * @param result to annotate
	 * @param color to annotate it with
	 * @return the resulting image
	 */
	public AnnotatedImage graphicallyAnnotate(Result result, Color color){
		AnnotatedImage img = new AnnotatedImage(this);
		for(Location location : result){
			Graphics2D graphic = img.createGraphics();
			graphic.setColor(color);
			graphic.drawPolygon(location);
		}
		return img;
	}
	
	/**
	 * Draws bounding polygons around the objects 
	 * @param results to annotate
	 * @param colors corresponding colors to annotate them with
	 * @return the resulting image
	 * @throws IllegalArgumentException if the number of rules and colors do not match
	 */
	public AnnotatedImage graphicallyAnnotate(Result[] results, Color[] colors) throws IllegalArgumentException{
		if(results.length != colors.length)
			throw new IllegalArgumentException("Number of Results and Colors must match");
		
		AnnotatedImage img = this;
		for(int i=0; i< results.length; i++){
			img = img.graphicallyAnnotate(results[i], colors[i]);
		}
		return img;
	}
}
