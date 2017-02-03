/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.common.CdmUtils;
import net.sf.ehcache.config.DiskStoreConfiguration;

/**
 * @author a.kohlbecker
 * @date Feb 1, 2017
 *
 */
@Configuration
public class EhCacheDiskStoreConfiguration {

    public static final Logger logger = Logger.getLogger(EhCacheDiskStoreConfiguration.class);

    @Autowired
    private DataSourceProperties dataSourceProperties = null;

    @Bean
    public DiskStoreConfiguration diskStoreConfiguration(){

        DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
        File ehcacheFolder = CdmUtils.getCdmSubDir("ehcache");
        String instanceName = dataSourceProperties.getCurrentDataSourceId();
        File instanceCacheFolder = new File(ehcacheFolder, instanceName);
        logger.debug("Setting ehcache diskstore location to " + instanceCacheFolder.getAbsolutePath());
        diskStoreConfiguration.setPath(instanceCacheFolder.getAbsolutePath());

        return diskStoreConfiguration;
    }

}
