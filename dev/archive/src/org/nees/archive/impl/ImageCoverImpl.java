/**
 * 
 */
package org.nees.archive.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.nees.archive.inter.ArchiveException;
import org.nees.archive.inter.ArchiveImageInterface;
import org.nees.archive.inter.ArchiveItemInterface;
import org.nees.archive.inter.ArchiveItemInterface.TYPE;

class ImageCoverImpl implements ArchiveImageInterface {
    /**
     * 
     */
    private final Archive archive;
    private String mimeType;
    private File file;
    double time;

    ImageCoverImpl(Archive archive, File f) throws ArchiveException {
        this.archive = archive;
        file = f;
        try {
            // convert milliseconds to seconds
            time =
                ((double) (ArchiveUtility.makeTimeFromFilename(file)))
                    / 1000.0;
            if (time == 0.0)
                throw new ArchiveException(
                    "Corupt Archive? - can not get time from file name = "
                        + file.toString());
        } catch (Exception e) {
            throw new ArchiveException(
                "Corupt Archive? - non-archvive name = " + file.toString());
        }
    }

    /**
     * @see org.nees.tivo.ArchiveImageCover#compareTo(org.nees.tivo.ArchiveImageCover)
     */
    public int compareTo(ArchiveImageInterface test) {
        double testTime = test.getTimeAsDouble();
        if (testTime > time)
            return -1; // test before this
        if (time > testTime)
            return 1; // test after this
        return 0; // equal
    }

    /**
     * @see org.nees.tivo.ArchiveImageCover#compareTo(java.lang.Object)
     */
    public int compareTo(Object t) {
        if (!(t instanceof ArchiveImageInterface))
            throw new ClassCastException(
                "Test object is not of class "
                    + "org.nees.tivo.ArchiveImageCover; found "
                    + t.getClass().getName()
                    + " instead.");
        return compareTo((ArchiveImageInterface)t);
    }

    public long getTime() {
        return (long) (time * 1000.0);
    }

    public InputStream getImageInputStream() {
        InputStream ret = null;
        try {
            ret = (InputStream) (new FileInputStream(file));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file.getAbsolutePath());
        }
        return ret;
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }

    /**
     * The MIME type of this object is always "image/jpg".
     * 
     * @see getMime
     * @see org.nees.tivo.ArchiveItemInterface#setMime(java.lang.String)
     */
    public String getMime() {
        return "image/jpg";
    }

    /**
     * @see org.nees.tivo.ArchiveItemInterface#equals(org.nees.tivo.ArchiveItemInterface)
     */
    public boolean equals(ArchiveItemInterface test) {
        if (!(test instanceof ImageCoverImpl))
            return false;
        ImageCoverImpl t = (ImageCoverImpl)test;
        return equals(t);
    }

    /**
     * @see org.nees.tivo.ArchiveItemInterface#equals(org.nees.tivo.ArchiveItemInterface)
     * @see #eqauls(ArchiveItemInterface)
     */
    public boolean equals(ImageCoverImpl test) {
        return file.getAbsolutePath().equals(test.file.getAbsolutePath());
    }

    /* (non-Javadoc)
     * @see org.nees.tivo.ArchiveItemInterface#getTimeAsDouble()
     */
    public double getTimeAsDouble() {
        return time;
    }

    /* (non-Javadoc)
     * @see org.nees.tivo.ArchiveItemInterface#getDuration()
     */
    public long getDuration() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.nees.tivo.ArchiveItemInterface#getDurationAsDouble()
     */
    public double getDurationAsDouble() {
        return 0.0;
    }

    /* (non-Javadoc)
     * @see org.nees.tivo.ArchiveItemInterface#hasMultipleItems()
     */
    public boolean hasMultipleItems() {
        return false;
    }

    public TYPE getType() {
        // TODO Auto-generated method stub
        return null;
    }

}