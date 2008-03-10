/*
 * Created on Feb 7, 2004
 *
 * A source of numbers in a bounded random walk.
 */
package org.nees.rbnb;

import java.util.Random;

/**
 * @author terry
 *
 * Generates the next number (double) in a bounded random walk;
 * the default interval is (-10,10), the start value is the
 * mid point and the step value is a uniform 
 * random distribution on (-1.0, 1.0). Steps that cross the boundries
 * are either wrapped (default) or reflected.
 */

public class SimpleRandomWalk {
	
	private static final double MIN_VALUE = -10.0;
	private static final double MAX_VALUE = 10.0;
	private static final boolean WRAP = true;
	
	private double minValue;
	private double maxValue;
	private double current;
	private boolean wrap;
	
	private Random source;
	
	public SimpleRandomWalk()
	{
		init(MIN_VALUE,MAX_VALUE,WRAP);
	}
	
	public SimpleRandomWalk(double min, double max, boolean wrap)
	{
		init(min,max,wrap);
	}
	
	private void init(double min, double max, boolean flag) {
		minValue = min;
		maxValue = max;
		current = (min + max)/2.0;
		wrap = flag;
		source = new Random();
	}

	public double next()
	{
		double step = (2.0 * source.nextDouble()) - 1.0;
		if ((current+step) > maxValue)
		{
			if (wrap) current = minValue + ((current+step)-maxValue);
			else // reflect
				current = maxValue + (maxValue-(current+step));
		}
		else if ((current+step) < minValue)
		{
			if (wrap) current = maxValue + ((current+step)-minValue);
			else // reflect
				current = minValue + (minValue - (current+step));
		}
		else current += step;
		
		return current;
	}
	
	public static void main (String[] args)
	{
		SimpleRandomWalk w = new SimpleRandomWalk();
		
		for (int i = 0; i < 10000; i++)
		{
			System.out.println(i + ": " + w.next());
		}
	}
}
