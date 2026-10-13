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

/**
 * Activate with {@code spring.profiles.active=swagger} (optionally together with {@code remoting}).
 * Uses live springdoc-openapi instead of {@link eu.etaxonomy.cdm.remote.staticSwagger.StaticSwaggerApiDoc}.
 * <p>
 * Swagger UI assets are served by {@link SwaggerUiWebjarController} (not springdoc resource handlers).
 *
 * @author a.kohlbecker
 * @since Feb 22, 2016
 */
@Profile("swagger")
@Configuration
@Import(value = { PreloadedBeans.class })
@ComponentScan(basePackages = {
        "eu.etaxonomy.cdm.api.service.l10n",
        "eu.etaxonomy.cdm.remote.controller",
        "eu.etaxonomy.cdm.remote.service",
        "eu.etaxonomy.cdm.remote.config" }) // includes CdmOpenApiConfig, SwaggerUiWebjarController
public class CdmSpringMVCConfigWithSwagger extends CdmSpringMVCConfig {

    private static final Logger logger = LogManager.getLogger();

    public CdmSpringMVCConfigWithSwagger() {
        super();
        logger.info(" ==========================================");
        logger.info("  Spring Configuration Profile: Swagger (springdoc-openapi)");
        logger.info(" ==========================================");
    }
}
