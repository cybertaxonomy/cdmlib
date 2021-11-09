/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is to replace the xml configuration in src/main/resources/eu/etaxonomy/cdm/services.xml
 *
 * @author a.kohlbecker
 * @since Nov 5, 2021
 */
@Configuration
@ComponentScans({
    @ComponentScan(basePackages = {
            "eu.etaxonomy.cdm.api.security",
            "eu.etaxonomy.cdm.api.service.security"
            })
})
@EnableAsync // required for eu.etaxonomy.cdm.api.service.security.PasswordResetService @Async
public class AppConfiguration {

}
