package org.nees.jcamera;


import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * This abstract class defines the actions and interface for a generic still image
 * digital camera or digital camera simulation. It knows how to
 * carry out all supported operations on the camera.
 *
 * <p>
 * <b>To Do:</b><ul>
 * <li> public void changeFocalLength( int delta )    </li>
 * <li> public void setFocus( int focus )             </li>
 * <li> public void changeFocus( int delta )          </li>
 * <li> public void saveLastPicture()                 </li>
 * <li> public void deleteAllPictures()               </li>
 * <li> public void deletePicture( String pic )       </li>
 * <li> public void changeToPreviewMode()             </li>
 * <li> public void getLastPictureThumbnail()         </li>
 * <li> public void getPictureThumbnail( String pic ) </li>
 * <li> public void getTime()                         </li>
 * </ul></p>
 */
public abstract class Camera
{
    //~ Static fields/initializers ---------------------------------------------
    private static Logger log = Logger.getLogger( Camera.class.getName() );
    protected static final Camera[] CAMERA_ARRAY = {
        (new CameraEcho()),
        (new CameraFromScript())
    };

    //~ Instance fields --------------------------------------------------------
    
    private String id;
    private String name;

    /**
     * Denotes whether this camera is busy or not.
     */
    private boolean lock;

    //~ Constructors -----------------------------------------------------------
    
    protected Camera(){} // used to list camera types

    public Camera( String name, String id )
    {
        log.debug( name + " created with id = " + id );
        this.name = name;
        this.id = id;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Is this type of camera available, on this system, to accept requests
     */
    public abstract boolean available();
    
    /**
     * Does this camera have an image available.
     */
    public abstract boolean hasImage();
    
    /**
     * See if camera is being used.
     */
    public boolean locked()
    {
        return lock;
    }

    /**
     * Lock the camera to do some work with it.
     */
    public void lock()
    {
        log.debug( name + " locked" );
        lock = true;
    }

    /**
     * Unlock the camera when you are done with it.
     */
    public void unlock()
    {
        log.debug( name + " unlocked" );
        lock = false;
    }

    /**
     * Open connection to camera.
     */
    public abstract void connect();

    /**
     * Close connection to camera.
     */
    public abstract void disconnect();

    /**
     * Reset the camera.  Do we need to reconnect after we reset it?
     */
    public abstract void reset();

    /**
     * Take a picture and store it in a file or internally.
     * @return a unique image id
     */
    public abstract String capture();

    /**
     * Get the camera's "most recent" image as a byte array.
     * @return a byte array of the image that was last saved into a file
     * @throws IOException 
     */
    public abstract byte[] getRecentImageBuffer() throws IOException;

    /**
     * Get the identified image as a byte array.
     * @return a byte array of the image that was last saved into a file
     * @throws IOException 
     */
    public abstract byte[] getImageBuffer(String imageId) throws IOException;

    /**
     * Delete the camera's "most recent" image from the camera.
     */
    public abstract void deleteRecentImage();

    /**
     * Delete the camera's "most recent" image from the camera.
     */
    public abstract void deleteImage(String imageId);

    /**
     * Set the focal length. Just a call to the external program.
     */
    public abstract void setFocalLength( int fl );

    /**
     * Get the focal length.
     * TODO Figure out if this can just be tracked and not queried.
     */
    public abstract int getFocalLength();
    
    /**
     * Generaic bean method
     * @return Returns the camera id.
     */
    public String getId() {
        return id;
    }
    protected void setId(String id){
        this.id = id;
    }

    /**
     * Generaic bean method
     * @return Returns the camera name.
     */
    public String getName() {
        return name;
    }
    protected void setName(String name){
        this.name = name;
    }

    /**
     * @return Returns the typeDescription.
     */
    public abstract String getTypeDescription();

    /**
     * @return Returns the typeName.
     */
    public abstract String getTypeName();

}
