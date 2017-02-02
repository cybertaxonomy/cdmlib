/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;

import java.io.File;

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
public class EhCacheManagerConfiguration {


    @Bean
    public DiskStoreConfiguration diskStoreConfiguration(){

        DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
        File ehcacheFolder = CdmUtils.getCdmSubDir("ehcache");
        diskStoreConfiguration.setPath(ehcacheFolder.getAbsolutePath());

        return diskStoreConfiguration;
    }

}
