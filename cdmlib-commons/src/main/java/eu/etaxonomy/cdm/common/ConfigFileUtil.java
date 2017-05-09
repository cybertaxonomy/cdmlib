/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 *
 * @author a.kohlbecker
 * @since May 8, 2017
 *
 */
public class ConfigFileUtil {

    private Properties props = null;

    private String defaultContent = "";

    public ConfigFileUtil(){

    }

    public ConfigFileUtil setDefaultContent(String content) {
        if(content != null){
            defaultContent = content;
        }
        return this;
    }

    public File getPropertiesFile(String instanceName, String propertiesSet) {

        if(propertiesSet == null){
            throw new NullPointerException();
        }
        File configFolder = CdmUtils.getCdmInstanceSubDir(CdmUtils.SUBFOLDER_WEBAPP, instanceName);
        return new File(configFolder, propertiesSet + ".properties");
    }

    /**
     *
     * @param instanceName
     * @param propertiesName
     *   Is used to compose the properties filename by adding the file extension <code>.properties</code>
     *
     * @return
     * @throws IOException
     */
    public Properties getProperties(String instanceName, String propertiesName) throws IOException {

        if(props == null){
            props = new Properties();
            File uiPropertiesFile = getPropertiesFile(instanceName, propertiesName);
            if(!uiPropertiesFile.exists()){
                BufferedWriter writer = Files.newBufferedWriter(uiPropertiesFile.toPath());
                writer.write(defaultContent);
                writer.close();
            }
            try {
                props.load(new FileInputStream(uiPropertiesFile));
            } catch (FileNotFoundException e) {
                // must not happen since we checked before
            }

        }
        return props;
    }
}
