/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import org.springframework.context.annotation.Import;

/**
 * Annotation to make sure the {@link AppConfigurationPropertiesConfig} is
 * already loaded for other configuration classes.
 *
 * @author a.kohlbecker
 * @since Sep 14, 2021
 */
@Import(AppConfigurationPropertiesConfig.class)
public @interface AppConfigurationProperties {

}
