package com.rbnb.compat;
/** Simple interface replacing the J2SE's version. Not every object has a clone method in J2ME
 */
public interface Cloneable
{
	/** Should copy all the fields over to the new object that is of the same class.
	 */
	public Object clone();
}