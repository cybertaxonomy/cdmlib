/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.ConfigFileUtil;

/**
 * @author a.kohlbecker
 * @since Feb 16, 2018
 *
 */
@Component
public class ApplicationConfiguration {

    /**
     * Annotate your integration test class with
     * {@code @TestPropertySource(properties = {"testmode = true"})} to
     * load the configuration files from the test resources.
     * <p>
     * <b>NOTE:</b> This will not work with current implementation of the
     * {@link eu.etaxonomy.cdm.test.integration.CdmIntegrationTest CdmIntegrationTests}.
     * For these tests please use the {@link unitils.AlternativeUnitilsJUnit4TestClassRunner}.
     *
     */
    public static final String TEST_MODE = "testmode";

    @Autowired
    Environment env;

    Map<String, Properties> configurations = new HashMap<>();

    public String getProperty(ApplicationConfigurationFile configFile, String key){
        Properties props = loadPropertiesFile(configFile);
        return props.getProperty(key);
    }

    /**
     * @param currentDataSourceId
     * @throws IOException
     */
    protected Properties loadPropertiesFile(ApplicationConfigurationFile configFile) {

        String currentDataSourceId = env.getProperty(CdmConfigurationKeys.CDM_DATA_SOURCE_ID);
        if(!configurations.containsKey(configFile.getFileName())){
            Properties props;
            try {
                if(env.getProperty(TEST_MODE) == null){
                    // PRODUCTION MODE
                    props = new ConfigFileUtil()
                            .setDefaultContent(configFile.getDefaultContet())
                            .getProperties(currentDataSourceId, configFile.getFileName());
                } else {
                    // TEST MODE
                    InputStream is = ApplicationConfiguration.class.getClassLoader().getResourceAsStream(
                            configFile.getFileName());
                    if (is == null) {
                        throw new RuntimeException("Can't find configuration file '" + configFile.getFileName() + ".properties in the test resoures.");
                    }
                    props = new Properties();
                    props.load(is);
                }
                if(props != null){
                    configurations.put(configFile.getFileName(), props);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading the configuration file '" + configFile.getFileName() + ".properties'. File corrupted?", e);
            }
        }
        return configurations.get(configFile.getFileName());
    }





}
