/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.opt.config;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.config.ConfigFileUtil;
import net.sf.ehcache.config.DiskStoreConfiguration;

/**
 * @author a.kohlbecker
 * @since Feb 1, 2017
 */
@Configuration
public class EhCacheDiskStoreConfiguration {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private DataSourceProperties dataSourceProperties = null;

    @Autowired
    private ConfigFileUtil configFileUtil;

    @Bean
    public DiskStoreConfiguration diskStoreConfiguration(){

        DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
        File ehcacheFolder = configFileUtil.getCdmHomeSubDir("ehcache");
        String instanceName = dataSourceProperties.getCurrentDataSourceId();
        File instanceCacheFolder = new File(ehcacheFolder, instanceName);
        logger.debug("Setting ehcache diskstore location to " + instanceCacheFolder.getAbsolutePath());
        diskStoreConfiguration.setPath(instanceCacheFolder.getAbsolutePath());

        return diskStoreConfiguration;
    }
}