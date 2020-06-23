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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
     * This is only uses for Converters so far
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);

        return mapper;
    }

}
