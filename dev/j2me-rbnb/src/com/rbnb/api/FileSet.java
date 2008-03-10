package com.rbnb.api;

class FileSet extends FrameManager
{


	FileSet()
		throws com.rbnb.api.SerializeException,
		java.io.IOException,
		java.lang.InterruptedException
	{
		super();
		
	}
	/**
	 * Class constructor to build a <code>FrameManager</code> from an
	 * identification index.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param idIndexI  the identification index.
	 * @exception com.rbnb.api.SerializeException
	 *		  thrown if there is a problem with the serialization.
	 * @exception java.io.IOException
	 *		  thrown if there is an error during I/O.
	 * @exception java.lang.InterruptedException
	 *		  thrown if the operation is interrupted.
	 * @see #FrameManager()
	 * @since V2.0
	 * @version 03/08/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/09/2001  INB	Created.
	 *
	 */
	FileSet(long idIndexI)
		throws com.rbnb.api.SerializeException,
		java.io.IOException,
		java.lang.InterruptedException
	{
			
	}

	/**
	 * Adds an element <code>Rmap</code> to this <code>FrameManager</code>
	 * object.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param elementI  the element <code>Rmap</code>.
	 * @exception com.rbnb.api.AddressException
	 *		  thrown if there is a problem with an address.
	 * @exception com.rbnb.api.SerializeException
	 *		  thrown if there is a problem with the serialization.
	 * @exception java.io.EOFException
	 *		  thrown if the end of the input stream is reached.
	 * @exception java.io.IOException
	 *		  thrown if there is an error during I/O.
	 * @exception java.lang.IllegalArgumentException
	 *		  thrown if the input <code>Rmap</code> is already a child of
	 *		  another <code>Rmap</code> or if the input is null.
	 * @exception java.lang.InterruptedException
	 *		  thrown if the operation is interrupted.
	 * @since V2.0
	 * @version 11/14/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/14/2003  INB	Added identification to the <code>Door</code> and
	 *			locations to the <code>Locks</code>.
	 * 02/09/2001  INB	Created.
	 *
	 */
		
	boolean buildRegistration()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.IOException,
		java.lang.InterruptedException { return true; }

	/**
	 * Clears this <code>FrameManager's</code> contents.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @exception com.rbnb.api.AddressException
	 *		  thrown if there is a problem with an address.
	 * @exception com.rbnb.api.SerializeException
	 *		  thrown if there is a problem with serialization.
	 * @exception java.io.InterruptedIOException
	 *		  thrown if this operation is interrupted during I/O.
	 * @exception java.io.IOException
	 *		  thrown if there is a problem with I/O.
	 * @exception java.lang.InterruptedException
	 *		  thrown if this operation is interrupted.
	 * @since V2.0
	 * @version 01/06/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/06/2004  INB	Throw <code>AddressExceptions</code> up to our caller.
	 * 06/05/2001  INB	Created.
	 *
	 */
	void clear()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.InterruptedIOException,
		java.io.IOException,
		java.lang.InterruptedException {}

	/**
	 * Closes this <code>FrameManager</code> to further additions.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @exception com.rbnb.api.AddressException
	 *		  thrown if there is a problem with an address.
	 * @exception com.rbnb.api.SerializeException
	 *		  thrown if there is a problem with serialization.
	 * @exception java.io.InterruptedIOException
	 *		  thrown if this operation is interrupted during I/O.
	 * @exception java.io.IOException
	 *		  thrown if there is a problem with I/O.
	 * @exception java.lang.InterruptedException
	 *		  thrown if this operation is interrupted.
	 * @since V2.0
	 * @version 06/05/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 06/05/2001  INB	Created.
	 *
	 */
	void close()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.InterruptedIOException,
		java.io.IOException,
		java.lang.InterruptedException {}

	/**
	 * Compares the sorting value of this <code>FrameManager</code> to the
	 * input sorting value according to the type sort specified by the sort
	 * identifier.
	 * <p>
	 * The sorting value for an <code>FrameManager</code> is always itself.
	 * <p>
	 * If the input is also a <code>FrameManager</code>, then the comparison is
	 * by identification index.
	 * <p>
	 * If the input is not a <code>FrameManagaer</code>, then the
	 * <code>Rmap</code> comparison method is used.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param sidI    the sort type identifier -- must be null.
	 * @param otherI  the other sorting value.
	 * @return the results of the comparison:
	 *	       <p><0 if this <code>FrameManager</code> compares less than the
	 *		   input,
	 *	       <p> 0 if this <code>FrameManager</code> compares equal to the
	 *		   input, and
	 *	       <p>>0 if this <code>FrameManager</code> compares greater than
	 *		   the input.
	 * @exception com.rbnb.utility.SortException
	 *		  thrown if the input sort identifier is non-null.
	 * @exception java.lang.IllegalStateException
	 *		  thrown if both the this <code>FrameManager</code> and the
	 *		  input <code>Rmap</code> are nameless and timeless.
	 * @see #compareTo(com.rbnb.api.Rmap)
	 * @since V2.0
	 * @version 08/30/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/09/2001  INB	Created.
	 *
	 */
		
	boolean readFromArchive()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.EOFException,
		java.io.InterruptedIOException,
		java.io.IOException,
		java.lang.InterruptedException { return true; }

	
	void readOffsetsFromArchive()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.EOFException,
		java.io.IOException,
		java.lang.InterruptedException {}

		
	void readSkeletonFromArchive()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.EOFException,
		java.io.InterruptedIOException,
		java.io.IOException,
		java.lang.InterruptedException {}

		
		 
	void storeElement(Rmap elementI)
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.EOFException,
		java.io.IOException,
		java.lang.InterruptedException {}

		
	void writeToArchive()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.EOFException,
		java.io.IOException,
		java.lang.InterruptedException {}

	final void releaseFiles()
		throws java.io.InterruptedIOException,
		java.io.IOException,
		java.lang.InterruptedException
	{}

	boolean deleted = false;
	final Door getFileDoor() { return null; }
	final InputStream getHIS() { return null; }
	final DataInputStream getDIS() { return null; }
	final InputStream getRHIS() { return null; }
	final DataOutputStream getODOS() { return null; }
	final OutputStream getRHOS() { return null; }
	final void markOutOfDate() {}
	final DataOutputStream getDOS() { return null; }
	final DataOutputStream getRDOS() { return null; }
	final DataInputStream getRDIS() { return null; }
	final OutputStream getHOS() { return null; }
	final DataInputStream getODIS() { return null; }
	final String getArchiveDirectory()
		throws com.rbnb.api.SerializeException,
		java.io.EOFException,
		java.io.IOException,
		java.lang.InterruptedException
	{return "";
	}

	final void accessFiles()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.IOException,
		java.lang.InterruptedException
	{}
	final Seal recoverFromDataFiles()
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.EOFException,
		java.io.InterruptedIOException,
		java.io.IOException,
		java.lang.InterruptedException
	{return null;}

	final void accessFiles(boolean forceReadWriteI)
		throws com.rbnb.api.AddressException,
		com.rbnb.api.SerializeException,
		java.io.IOException,
		java.lang.InterruptedException
	{}

	final public Object clone()
	{
		try 
		{
			Object o = new FileSet();
			cloned(o);
			return o;
		}
		catch(Exception e) { return null; }
	}
}

