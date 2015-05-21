/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.FileCopy;
import eu.etaxonomy.cdm.config.CdmPersistentXMLSource;

public class CdmApplicationUtils {
    private static final Logger logger = Logger.getLogger(CdmApplicationUtils.class);

    //directory of the resources (configfiles etc.)
    static File fileResourceDir;
    static final String MUST_EXIST_FILE = CdmPersistentXMLSource.CDMSOURCE_PATH + CdmPersistentXMLSource.CDMSOURCE_FILE_NAME;

//	static final String MUST_EXIST_FILE = "persistence.xml";
//	static final String MUST_EXIST_FILE = "applicationContext.xml";

    /**
     * Returns the directory path to the writable resources (cdm.datasources.xml and H2 databases).
     * (Resources must be copied to this directory, this is automatically done for the cdm.datasources.xml)
     * @return
     * @throws IOException if resource directory is not writable
     */
    public static File getWritableResourceDir() throws IOException{
        //compute only once
        if (fileResourceDir == null){
            //workaround to find out in which environment the library is executed
            URL url = CdmUtils.class.getResource(MUST_EXIST_FILE);
            if (url != null){
                String fileName = url.getPath();
                if (fileName.contains("%20")) {
                    fileName = fileName.replaceAll("%20", " ");
                }
                File file = new File(fileName);
                if (file.exists()){
                    fileResourceDir = file.getParentFile();
                }else{
                    file = new File(CdmUtils.getCdmHomeDir(), "writableResources" );

                    file.mkdirs();
                    copyResources(file);
                    fileResourceDir = file;
                }
            }
            logger.info("Resource directory: " + (fileResourceDir == null?"null":fileResourceDir.getAbsolutePath()));
        }
        return fileResourceDir;
    }

    static private void copyResources(File directory){
        copyResource(directory, CdmPersistentXMLSource.CDMSOURCE_FILE_NAME);
    }

    /**
     * Copies a file from the classpath resource (e.g. jar-File) to the resources directory in the file system (get
     * @param directory
     * @param resourceFileName
     */
    static private boolean copyResource(File directory, String resourceFileName){
        try {
            InputStream isDataSource = CdmUtils.class.getResourceAsStream(CdmPersistentXMLSource.CDMSOURCE_PATH + resourceFileName);
            if (isDataSource != null){
                File fileToCopy = new File(directory + File.separator + resourceFileName);
                if (fileToCopy.createNewFile()){
                    FileOutputStream outStream = new FileOutputStream(fileToCopy);
                    FileCopy.copy(isDataSource, outStream);
                    //XmlHelp.saveToXml(XmlHelp.getBeansRoot(isDataSource).getDocument(), outStream, Format.getPrettyFormat());
                }
                return true;
            }else{
                logger.error("Input datasource file "  + resourceFileName + " + could not be found");
            }
        } catch (IOException e) {
            logger.error("File "  + resourceFileName + " + could not be created");
            throw new RuntimeException(e);
        }
        return false;
    }


}
