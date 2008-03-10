package com.rbnb.compat;
/** Replaces the InternalError that J2ME does not have
 */
public class InternalError extends Error
{
	/** Default constructor
	 */
	public InternalError()
	{
		super();
	}

	/** Constructor specifying an error string
	 */
	public InternalError(String s)
	{
		super(s);
	}
}