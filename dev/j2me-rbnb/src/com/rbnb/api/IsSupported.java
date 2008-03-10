package com.rbnb.api;

final class IsSupported 
{

	/**
	 * use internal (rather than Java) serialization of regular Java objects
	 * in <code>Ask</code> messages?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.4.3
	 * @version 07/30/2004
	 */
	public final static int FEATURE_ASK_NO_JAVA_SERIALIZE = 10;

	/**
	 * are <code>ClearCache</code> commands supported?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 01/08/2004
	 */
	public final static int FEATURE_CLEAR_CACHE = 9;

	/**
	 * are <code>DeleteChannels</code> supported?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 07/30/2003
	 */
	public final static int FEATURE_DELETE_CHANNELS = 6;

	/**
	 * are <code>RequestOptions.extendStart</code> settings supported?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/08/2003
	 */
	public final static int FEATURE_OPTION_EXTEND_START = 8;

	/**
	 * are <code>Pings</code> with data supported?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.1
	 * @version 02/25/2003
	 */
	public final static int FEATURE_PINGS_WITH_DATA = 4;

	/**
	 * aligned requests allowed?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/13/2003
	 */
	public final static int FEATURE_REQUEST_ALIGNED = 2;

	/**
	 * leaf nodes in requests are marked?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/20/2002
	 */
	public final static int FEATURE_REQUEST_LEAF_NODES = 1;

	/**
	 * are <code>RequestOptions</code> supported?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 06/11/2003
	 */
	public final static int FEATURE_REQUEST_OPTIONS = 5;

	/**
	 * are time-relative requests supported?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 11/06/2003
	 */
	public final static int FEATURE_REQUEST_TIME_RELATIVE = 7;

	/**
	 * <code>TimeRanges</code> can be inclusive of end time?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/20/2002
	 */
	public final static int FEATURE_TIME_RANGE_INCLUSIVE = 0;

	/**
	 * <code>Usernames</code> supported?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/14/2003
	 */
	public final static int FEATURE_USERNAMES = 3;

	/**
	 * dates that the various features came into being.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 07/30/2004
	 */
	private static java.util.Date[] Feature_Dates = new java.util.Date[11];

	/**
	 * Determines if the specified feature is supported.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param featureI	    the feature code.
	 * @param buildVersionI the version to check.
	 * @param buildDateI    the date to check.
	 * @since V2.0
	 * @version 07/30/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 07/30/2004  INB	FEATURE_ASK_NO_JAVA_SERIALIZE is not supported prior to
	 *			V2.4.3.
	 * 01/08/2004  INB	FEATURE_CLEAR_CACHE is not supported prior to V2.2.
	 * 12/08/2003  INB	FEATURE_OPTION_EXTEND_START is not supported prior to
	 *			V2.2.
	 * 11/06/2003  INB	FEATURE_REQUEST_TIME_RELATIVE is not supported prior to
	 *			V2.2
	 * 09/26/2003  INB	The <code>Feature_Dates</code> object is created at
	 *			initialization of this class.  We can now synchronize
	 *			on it at startup to ensure that all of the dates have
	 *			been filled in before we check for support.
	 * 07/30/2003  INB	FEATURE_DELETE_CHANNELS is not supported prior to V2.2.
	 * 06/11/2003  INB	FEATURE_REQUEST_OPTIONS is not supported priot to V2.2.
	 * 03/04/2003  INB	FEATURE_PINGS_WITH_DATA is not supported prior to V2.1.
	 * 02/25/2003  INB	Added FEATURE_PINGS_WITH_DATA.
	 * 11/20/2002  IMB	Created.
	 *
	 */
	final static boolean isSupported(int featureI,
		String buildVersionI,
		java.util.Date buildDateI)
	{
		return true;
	}
}