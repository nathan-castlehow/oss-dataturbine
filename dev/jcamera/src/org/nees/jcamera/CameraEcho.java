package org.nees.jcamera;

import java.io.IOException;

import org.apache.log4j.Logger;

public class CameraEcho extends Camera {
    
    //~ Static fields/initializers ---------------------------------------------

    private static Logger log = Logger.getLogger( CameraEcho.class.getName() );

    //~ Instance fields --------------------------------------------------------

    private final String typeName = "Echo Camera";
    private final String typeDescription = 
        "Simple test camera. Echos commands to the logger and returns no image";

    private int focalLength;
    
    //~ Constructors -----------------------------------------------------------

    protected CameraEcho(){
        setName("Echo Camera");
        setId("0");
    }
    
    public CameraEcho( String name, String id )
    {
        super(name, id);
    }

    //~ Methods ----------------------------------------------------------------

    public boolean available()
    {
        return true;         
    }
    
    public boolean hasImage()
    {
        return false;
    }
    
    public void connect() {
        toLog("connect");        
    }

    public void disconnect() {
        toLog("disconnect");        
    }

    public void reset() {
        toLog("reset");        
    }

    public String capture() {
        toLog("capture: no image!");
        return null;
    }

    public byte[] getRecentImageBuffer() throws IOException {
        toLog("getRecentImageBuffer: no image!");
        return null;
    }

    public byte[] getImageBuffer(String imageId) throws IOException {
        toLog("getImageBuffer: no image!");
        return null;
    }

    public void deleteRecentImage() {
        toLog("deleteRecentImage");
    }

    public void deleteImage(String imageId) {
        toLog("deleteImage: " + imageId);
    }

    public void setFocalLength(int fl) {
        toLog("setFocalLength: " + fl);
        focalLength = fl;
    }

    public int getFocalLength() {
        toLog("getFocalLength: " + focalLength);
        return focalLength;
    }

    public String getTypeDescription() {
        toLog("getTypeDescription: " + typeDescription);
        return typeDescription;
    }

    public String getTypeName() {
        toLog("getTypeName: " + typeName);
        return typeName;
    }

    private void toLog(String message){
        log.debug(message);
    }
}
