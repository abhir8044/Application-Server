package utils;

import java.util.*;
import java.io.*;

import utils.FileUtils;

public class PropertyHandler extends Properties {

    File propertyFile = null;

    /**
     * constructor
     */
    public PropertyHandler(Properties defaultProperties, String propertyFileString) throws FileNotFoundException, IOException {

        super(defaultProperties);
        propertyFile = getPropertyFile(propertyFileString);

        InputStream is = new BufferedInputStream(new FileInputStream(propertyFile));
        this.load(is);
        is.close();
    }

    public PropertyHandler(String propertyFileString)
            throws FileNotFoundException, IOException {
        this(null, propertyFileString);
    }

    /**
     * This is the important method reading the properties.
     */
    @Override
	public String getProperty(String key) {
        String value = super.getProperty(key);
        return value;
    }

    /**
     * Looks for a valid properties file ...
     */
    private File getPropertyFile(String propertyFileString)
            throws FileNotFoundException, IOException {

        // ... in the current directory
        if ((propertyFile = new File(propertyFileString)).exists()) {
            return propertyFile;
        }

        // ... in the directory, where the program was started
        String dirString = System.getProperty("user.dir");
        String completeString = dirString + File.separator + propertyFileString;
        if ((propertyFile = new File(completeString)).exists()) {
            return propertyFile;
        }

        // ... in Home-directory of the user
        dirString = System.getProperty("user.home");
        completeString = dirString + File.separator + propertyFileString;
        if ((propertyFile = new File(completeString)).exists()) {
            return propertyFile;
        }

        // ... in the directory where Java keeps its own property files
        dirString = System.getProperty("java.home") + File.separator + "lib";
        completeString = dirString + File.separator + propertyFileString;
        if ((propertyFile = new File(completeString)).exists()) {
            return propertyFile;
        }

        // ... in all directories specified by the CLASSPATH
        String[] classPathes = FileUtils.getClassPathes();
        for (int i = 0; i < classPathes.length; i++) {
            completeString = classPathes[i] + File.separator + propertyFileString;
            if ((propertyFile = new File(completeString)).exists()) {
                return propertyFile;
            }
        }

        throw new FileNotFoundException("[PropertyHandler.PropertyHandler]Configuration file \"" + propertyFileString + "\" not found!");
    }
}
