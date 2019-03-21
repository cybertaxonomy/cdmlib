/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * Utility class for consistent access to and creation of per instance application configuration files.
 *
 * @author a.mueller
 * @author a.kohlbecker
 */
public class ConfigFileUtil implements EnvironmentAware {

    private static final Logger logger = Logger.getLogger(ConfigFileUtil.class);

    private static String userHome = null;

    /**
     * The per user cdm folder name: ".cdmLibrary"
     */
    private static final String CDM_FOLDER_NAME = ".cdmLibrary";

    /**
     * The per user cdm folder "~/.cdmLibrary"
     */
    private static File perUserCdmFolder = null;

    public static File perUserCdmFolder() {
        return perUserCdmFolder;
    }

    /**
     * suggested sub folder for web app related data and configurations.
     * Each webapp instance should use a dedicated subfolder or file
     * which is named by the data source bean id.
     */
    public static final String SUBFOLDER_WEBAPP = "remote-webapp";

    protected Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
        if(userHome == null){
            ConfigFileUtil.userHome = env.getRequiredProperty("user.home");
            ConfigFileUtil.perUserCdmFolder = new File(userHome + File.separator + CDM_FOLDER_NAME );
            logger.info("user.home is set to " + ConfigFileUtil.userHome);
        }
    }


    public static File getCdmHomeDir() {
        return new File(perUserCdmFolder + File.separator);
    }

    /**
     * Returns specified the sub folder of  {@link #CDM_FOLDER_NAME}.
     * If the sub folder does not exist it will be created.
     *
     * @param subFolderName
     * @return the sub folder or null in case the folder did not exist ant the attempt to create it has failed.
     *
     * @see {@link #SUBFOLDER_WEBAPP}
     */
    public static File getCdmHomeSubDir(String subFolderName) {

        File parentFolder = getCdmHomeDir();
        return ensureSubfolderExists(parentFolder, subFolderName);
    }

    /**
     * Returns an instance specific folder folder in  {@link #CDM_FOLDER_NAME}/<code>subFolderName</code>
     * Non existing folders will be created.
     *
     * @param subFolderName
     *      The name of a subfolded. In most cases this will be {@link #SUBFOLDER_WEBAPP}
     * @param instanceName
     *      The name of the application instance. The name should be related to the data source id.
     * @return the sub folder or null in case the folder did not exist ant the attempt to create it has failed.
     *
     * @see {@link #SUBFOLDER_WEBAPP}
     */
    public static File getCdmInstanceSubDir(String subFolderName, String instanceName) {

        File subfolder = ensureSubfolderExists(getCdmHomeDir(), subFolderName);
        return ensureSubfolderExists(subfolder, instanceName);
    }

    /**
     * @param subFolderName
     * @param parentFolder
     * @return
     */
    private static File ensureSubfolderExists(File parentFolder, String subFolderName) {
        if (!parentFolder.exists()){
            if (!parentFolder.mkdir()) {
                throw new RuntimeException("Parent folder could not be created: " + parentFolder.getAbsolutePath());
            }
        }

        File subfolder = new File(parentFolder, subFolderName);
        // if the directory does not exist, create it
        if (!subfolder.exists()) {
            if (!subfolder.mkdir()) {
                throw new RuntimeException("Subfolder could not be created: " + subfolder.getAbsolutePath());
            }
        }
        return subfolder;
    }
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
            File configFolder = ConfigFileUtil.getCdmInstanceSubDir(ConfigFileUtil.SUBFOLDER_WEBAPP, instanceName);
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
