package org.auscope.portal.server.gridjob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Singleton class that provides utility methods like copying files.
 *
 * @author Cihan Altinay
 */
public class Util
{
    /** Logger for this class and subclasses */
    private static Log logger = LogFactory.getLog(Util.class.getName());

    /**
     * Private constructor to prevent instantiation.
     */
    private Util() { }

    /**
     * Copies a file from source to destination.
     *
     * @return true if file was successfully copied, false otherwise
     */
    public static boolean copyFile(File source, File destination) {
        boolean success = false;
        logger.debug(source.getPath()+" -> "+destination.getPath());
        FileInputStream input = null;
        FileOutputStream output = null;
        byte[] buffer = new byte[8192];
        int bytesRead;

        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(destination);
            while ((bytesRead = input.read(buffer)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
            success = true;

        } catch (IOException e) {
            logger.warn("Could not copy file: "+e.getMessage());

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {}
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {}
            }
        }

        return success;
    }

    /**
     * Moves a file from source to destination.
     *
     * @return true if file was successfully moved, false otherwise
     */
    public static boolean moveFile(File source, File destination) {
        boolean success = copyFile(source, destination);
        if (success) {
            source.delete();
        }
        return success;
    }

    /**
     * Recursively copies the contents of a directory into the destination
     * directory.
     *
     * @return true if contents were successfully copied, false otherwise
     */
    public static boolean copyFilesRecursive(File source, File destination) {
        boolean success = false;

        if (source.isDirectory()) {
            if (!destination.exists()) {
                if (!destination.mkdirs()) {
                    success = false;
                    return false;
                }
            }
            String files[] = source.list();

            for (int i=0; i<files.length; i++) {
                File newSrc = new File(source, files[i]);
                File newDest = new File(destination, files[i]);
                success = copyFilesRecursive(newSrc, newDest);
                if (!success) {
                    break;
                }
            }
        } else {
            success = copyFile(source, destination);
        }

        return success;
    }

}

