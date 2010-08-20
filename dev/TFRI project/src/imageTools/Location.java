package imageTools;

import java.awt.Polygon;

/**
 * Location.java (  imageTools )
 * Created: Dec 5, 2009
 * @author Michael Nekrasov
 * 
 * Description: A 2D bound where a certain object lies,
 * 				Binding can be a single point or a arbitrary polygon.
 *
 */
public class Location extends Polygon{
	
	private static final long serialVersionUID = -5539800694043410684L;

	/**
	 * Constructs a Location for a single point
	 * @param x
	 * @param y
	 */
	public Location(int x, int y){
		super();
		addPoint(x, y);
	}
	
	/**
	 * Constructs a bound rectangular region
	 * @param minX smallest x position
	 * @param minY smallest y position
	 * @param width of rectangle
	 * @param height of rectangle 
	 */
	public Location(int minX, int minY, int width,  int height){
		super();
		addPoint(minX, minY);
		addPoint(minX + width, minY);
		addPoint(minX + width, minY + height);
		addPoint(minX, minY+ height);
		addPoint(minX,minY);		
	}
	
	
	public String toString(){
		String str = "[ ";
		for(int i=0; i < npoints; i++)
			str+= "("+ xpoints[i] +", "+ ypoints[i]+ ") ";
		return str +"]";
	}
}
