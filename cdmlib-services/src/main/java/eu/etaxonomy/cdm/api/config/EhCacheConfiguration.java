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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.api.cache.ehcache.CdmEntityCacheManager;

/**
 * Configures the {@link CdmEntityCacheManager} (Ehcache 3) used for the
 * CDM entity caches. The bean is named <code>cacheManager</code>.
 * <p>
 * The OAI-PMH cache uses its own Spring/JCache cache manager,
 * see <code>OaiPmhCacheConfiguration</code>.
 *
 * @author a.kohlbecker
 * @since Feb 1, 2017
 */
@Configuration
// @EnableCaching // for future use
public class EhCacheConfiguration implements DisposableBean {

    private static final Logger logger = LogManager.getLogger();

    public static final String DISK_STORE_PATH_BEAN = "ehCacheDiskStorePath";

    @Autowired(required = false)
    @Qualifier(DISK_STORE_PATH_BEAN)
    public File diskStorePath = null;

    /**
     * The disk store path can either be autowired (bean named
     * {@link #DISK_STORE_PATH_BEAN}) or set explicitly.
     *
     * @param diskStorePath the directory for the optional disk store
     */
    public void setDiskStorePath(File diskStorePath) {
        this.diskStorePath = diskStorePath;
    }

    private CdmEntityCacheManager cacheManager = null;

    @Bean(name = "cacheManager")
    public CdmEntityCacheManager cacheManager(){

        //the manager is a singleton, the disk store path can only be set
        //as long as the manager is not yet initialized (like in the TaxEditor)
        cacheManager = CdmEntityCacheManager.getInstance();
        if(diskStorePath != null){
            if (logger.isDebugEnabled()) { logger.debug("configuring CdmEntityCacheManager with disk store");}
            cacheManager.setDiskStorePath(diskStorePath);
        }
        if (logger.isDebugEnabled()) { logger.debug("CdmEntityCacheManager created");}
        return cacheManager;
    }

    @Override
    public void destroy() {
        if (cacheManager != null) {
            logger.info("Shutting down CdmEntityCacheManager");
            this.cacheManager.shutdown();
        }
    }
}
