package org.nees.jcamera;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class CameraFromScript extends Camera {

    private final String dummyScript =
        "echo dummy.jpg";

    private File mostRecentFile = null;
    
    public String getTypeDescription() {
        return "Get Image from running a dummy script";
    }

    public String getTypeName() {
        return "Script";
    }

    public boolean available() {
        // aways available, but may fail to deliver a picture
        return true;
    }

    public boolean hasImage() {
        File f = getImageFile();
        if (f == null) return false;
        if (!f.exists()) return false;
        return true;
    }

    private File getImageFile() {
        String fileName = null;
//        System.out.println("Starting process with: " + dummyScript);
        try {
            Process scriptProc = Runtime.getRuntime().exec(dummyScript);

            // capture the output
            BufferedReader in = new BufferedReader( new InputStreamReader(
                    scriptProc.getInputStream() ) );

            String line = in.readLine();
//            System.out.println("Read from process: " + line);
            fileName = line;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
//        System.out.println("Done with running process.");

        if (fileName == null) return null;
        if (fileName.equals("")) return null;

        File file = new File(fileName);
//        System.out.println("Expected file path = " + file.getAbsolutePath());
        if (file.exists())
        {
            mostRecentFile = file;
            return mostRecentFile;
        }
        return null;
    }

    public void connect() {
        // in this case, a no-op
    }

    public void disconnect() {
        // in this case, a no-op
    }

    public void reset() {
        // in this case, a no-op
    }

    public String capture() {
        return (getImageFile()).getName();
    }

    public byte[] getRecentImageBuffer() throws IOException {
        byte[] buf = null;
        if (mostRecentFile.exists() 
                && mostRecentFile.isFile() 
                && mostRecentFile.canRead()){
            RandomAccessFile image = new RandomAccessFile(mostRecentFile,"r");
            long len = image.length();
            buf = new byte[(int) len]; // well, it's ok in this case!
            image.read(buf);
        }
        return buf;
    }

    public byte[] getImageBuffer(String imageId) throws IOException {
        return getRecentImageBuffer();
    }

    public void deleteRecentImage() {
        // in this case, a no-op
    }

    public void deleteImage(String imageId) {
        // in this case, a no-op
    }

    public void setFocalLength(int fl) {
        // in this case, a no-op
    }

    public int getFocalLength() {
        // in this case, a no-op
        return 0;
    }

}
