/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import eu.etaxonomy.cdm.remote.controller.interceptor.LocaleContextHandlerInterceptor;

/**
 * @author a.kohlbecker
 * @since Jul 28, 2014
 */
@Configuration
public class PreloadedBeans {

    @Bean
    public LocaleContextHandlerInterceptor getlocaleContextHandlerInterceptor(){
        return new LocaleContextHandlerInterceptor();
    }

    /**
     * This is only used for Converters so far
     */
    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .build();
    }

}
