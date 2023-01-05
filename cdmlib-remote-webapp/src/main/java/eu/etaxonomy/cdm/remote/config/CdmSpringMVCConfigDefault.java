/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author a.kohlbecker
 * @since Feb 22, 2016
 */
//@EnableWebMvc // do not add this since we are overriding WebMvcConfigurationSupport directly
@Profile("!swagger")
@ActiveProfiles("remoting")
@Configuration
@Import(value={PreloadedBeans.class}) // can not be replaced by @DependsOn("...") ?
//@DependsOn("objectMapperConfigurer")
@ComponentScan(basePackages = {
     "eu.etaxonomy.cdm.remote.l10n",
     "eu.etaxonomy.cdm.remote.controller",
     "eu.etaxonomy.cdm.remote.staticSwagger",
     "eu.etaxonomy.cdm.remote.service",
     "eu.etaxonomy.cdm.remote.config"
     }
)
public class CdmSpringMVCConfigDefault extends CdmSpringMVCConfig {

    private static final Logger logger = LogManager.getLogger();

    public CdmSpringMVCConfigDefault() {
        super();
        logger.info(" ==========================================");
        logger.info("  Spring Configuration Profile: Default");
        logger.info(" ==========================================");
    }

}
