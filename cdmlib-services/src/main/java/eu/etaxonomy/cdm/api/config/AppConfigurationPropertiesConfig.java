/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Centralizes the application configuration properties for all <code>@Configuration</code> beans.
 *
 * The default configuration in {@code classpath:/application.properties} can be overwritten
 * by {@code file:/${user.home}/.cdmLibrary/application-dev.properties} for development purposes.
 * For production environments it is recommended to use system properties instead.
 *
 * Mimics the spring boot auto-configuration capabilities to some very basic extend. Can be replaced once we are using spring boot.
 *
 * spring-boot features that are mimicked:
 *
 * <ul>
 * <li><b>application.properties</b>: The property source file <code>application.properties</code> may be placed in
 * <code>src/test/resources</code> and <code>src/main/resources</code></li>
 * <li><code>${user.home}/.cdmLibrary/application.properties</code>: the default properties can be overwritten by providing
 * this properties file. (this is not a spring boot feature, though!)</li>
 * </ul>
 * @author a.kohlbecker
 * @since Sep 15, 2021
 */
@Configuration
@PropertySources({
    // classpath:/application.properties as first
    @PropertySource("classpath:/application.properties"),
    // allows to use ${user.home}/.cdmLibrary/application.properties to override the default settings !!!
    @PropertySource(value="file:${user.home}/.cdmLibrary/application.properties", ignoreResourceNotFound = true)
})
public class AppConfigurationPropertiesConfig {

}