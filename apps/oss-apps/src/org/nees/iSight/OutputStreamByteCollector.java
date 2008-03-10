/*
 * OutputStreamByteCollector.java
 * Created: Aug 18, 2005
 *
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 */
package org.nees.iSight;

import java.io.IOException;

/**
 * Used as a traget for iSightToByteStream. Collects the image output in a byte
 * array buffer and returns a copy of that buffer.
 * 
 * @author Terry E. Weymouth
 *
 * @see iSightToByteStream
 */
public class OutputStreamByteCollector
    extends java.io.OutputStream
{
    private static final int INC = 10000;
    private static int maxSize = INC;
    
    private byte[] buffer = new byte[maxSize];
    private int count = 0;
    
    public void write(int arg0) throws IOException {
        write((byte)arg0);
    }

    public void write(byte arg0) throws IOException {
        buffer[count] = arg0;
        count++;
        if (count == maxSize)
        {
            maxSize += INC;
            byte[] newBuffer = new byte[maxSize];
            System.arraycopy(buffer,0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }
    
    public void reset()
    {
        count = 0;
    }
    
    public int size()
    {
        return count;
    }
    
    public byte[] getBufferCopy()
    {
        byte[] out = new byte[count];
        System.arraycopy(buffer,0, out, 0, count);
        return out;
    }
}