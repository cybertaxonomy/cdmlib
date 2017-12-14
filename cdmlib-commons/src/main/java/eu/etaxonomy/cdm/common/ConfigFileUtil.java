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

    /**
     *
     */
    public static final String CDM_CONFIGFILE_OVERRIDE = "cdm.configfile.override.";

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

    /**
     * Per default the <code>propertiesSet</code> is loaded from a file located in
     * <code>~/.cdmLibrary/remote-webapp/{instanceName}/{propertiesSet}.properties</code>.
     * <p>
     * This behavior can be overwritten by setting the java System property
     * <code>cdm.configfile.override.{propertiesSet}</code> to an alternative file location.
     * This mechanism should only be used for unit and integration tests.
     *
     * @param instanceName the name of the cdm instance. This value can be retrieved from the
     *      Spring environment with the key DataSourceConfigurer.CDM_DATA_SOURCE_ID ("")
     * @param propertiesSet
     *      The base name of the properties file to be loaded. This name is extended with
     *      ".properties" to form the actual filename
     *
     * @return
     *      The file containing the properties
     */
    public File getPropertiesFile(String instanceName, String propertiesSet) {

        if(propertiesSet == null){
            throw new NullPointerException();
        }
        String override = System.getProperty(CDM_CONFIGFILE_OVERRIDE + propertiesSet);
        if(override != null){
            return new File(override);
        } else {
            File configFolder = CdmUtils.getCdmInstanceSubDir(CdmUtils.SUBFOLDER_WEBAPP, instanceName);
            return new File(configFolder, propertiesSet + ".properties");
        }
    }

    /**
     * Per default the <code>propertiesSet</code> is loaded from a file located in
     * <code>~/.cdmLibrary/remote-webapp/{instanceName}/{propertiesSet}.properties</code>.
     * <p>
     * This behavior can be overwritten by setting the java System property
     * <code>cdm.configfile.override.{propertiesSet}</code> to an alternative file location.
     * This mechanism should only be used for unit and integration tests.
     *
     * @param instanceName the name of the cdm instance. This value can be retrieved from the
     *      Spring environment with the key DataSourceConfigurer.CDM_DATA_SOURCE_ID ("")
     * @param propertiesSet
     *      The base name of the properties file to be loaded. This name is extended with
     *      ".properties" to form the actual filename
     *
     * @return
     *      The properties loaded from the file
     */
    public Properties getProperties(String instanceName, String propertiesName) throws IOException {

        if(instanceName == null){
            throw new NullPointerException();
        }
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
