/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dozer.BeanFactory;
import org.dozer.CustomConverter;
import org.dozer.CustomFieldMapper;
import org.dozer.DozerBeanMapper;
import org.dozer.DozerEventListener;
import org.dozer.Mapper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * extended version of Sören Chittka's DozerBeanMapperFactoryBean, allowing other
 * properties to be set.
 * @author Sören Chittka
 */
public class DozerBeanMapperFactoryBean implements FactoryBean, InitializingBean {

	  private DozerBeanMapper beanMapper;
	  private Resource[] mappingFiles;
	  private List<CustomConverter> customConverters;
	  private Map<String,CustomConverter> customConvertersWithId;
	  private List<DozerEventListener> eventListeners;
	  private Map<String, BeanFactory> factories;
	  private CustomFieldMapper customFieldMapper;
	  

	  public final void setMappingFiles(final Resource[] mappingFiles) {
	    this.mappingFiles = mappingFiles;
	}

	public final void setCustomConverters(final List<CustomConverter> customConverters) {
	    this.customConverters = customConverters;
	}

	public final void setEventListeners(final List<EventListener> eventListeners) {
	    this.eventListeners = eventListeners;
	}

	public final void setFactories(final Map<String, BeanFactory> factories) {
	    this.factories = factories;
	  }
	  
	  public final void setCustomFieldMapper(final CustomFieldMapper customFieldMapper) {
		  this.customFieldMapper = customFieldMapper;
	  }
	  
	  public final void setCustomConvertersWithId(final Map<String,CustomConverter> customConvertersWithId) {
		  this.customConvertersWithId = customConvertersWithId;
	  }

	  // ==================================================================================================================================
	  // interface 'FactoryBean'
	  // ==================================================================================================================================
	  public final Object getObject() throws Exception {
	    return this.beanMapper;
	}

	@Override
    public final Class<Mapper> getObjectType() {
	    return Mapper.class;
	}

	@Override
    public final boolean isSingleton() {
	    return true;
	}

	// ==================================================================================================================================
	// interface 'InitializingBean'
	// ==================================================================================================================================
	@Override
    public final void afterPropertiesSet() throws Exception {

        DozerBeanMapperBuilder beanMapperBuilder = DozerBeanMapperBuilder.create();

        final List<String> mappings = new ArrayList<>(this.mappingFiles.length);
        if (this.mappingFiles != null) {
            for (Resource mappingFile : this.mappingFiles) {
                mappings.add(mappingFile.getURL().toString());
            }
            beanMapperBuilder.withMappingFiles(mappings);
        }

	    if (this.customConverters != null) {
	        beanMapperBuilder.withCustomConverters(customConverters);
	    }
	    if (this.eventListeners != null) {
	        beanMapperBuilder.withEventListeners(eventListeners);
	    }
	    if (this.factories != null) {
	        beanMapperBuilder.withBeanFactorys(factories);
	    }

	    if(this.customFieldMapper != null) {
	        beanMapperBuilder.withCustomFieldMapper(customFieldMapper);
	    }

	    if(this.customConvertersWithId != null) {
	        beanMapperBuilder.withCustomConvertersWithIds(customConvertersWithId);
	    }
	}
}