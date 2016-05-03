// $Id$
/**
 * Copyright (C) 2016 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Activate this profile by setting the
 * <code>spring.profiles.active=swagger</code> either as system property as the
 * web application argument
 *
 * @author a.kohlbecker
 * @date Feb 22, 2016
 *
 */
@Profile("swagger")
// @EnableWebMvc // do not add this since we are overriding
// WebMvcConfigurationSupport directly
@Configuration
@Import(value = { PreloadedBeans.class })
// can not be replaced by @DependsOn("...") ?
// @DependsOn("objectMapperConfigurer")
@ComponentScan(basePackages = {
        "springfox.documentation.spring.web", // --> CdmSwaggerConfig
        "eu.etaxonomy.cdm.remote.l10n",
        "eu.etaxonomy.cdm.remote.controller",
        "eu.etaxonomy.cdm.remote.service",
        "eu.etaxonomy.cdm.remote.config" })
public class CdmSpringMVCConfigWithSwagger extends CdmSpringMVCConfig {

    public CdmSpringMVCConfigWithSwagger() {
        super();
        logger.info(" ==========================================");
        logger.info("  Spring Configuration Profile: Swagger");
        logger.info(" ==========================================");
    }

}
