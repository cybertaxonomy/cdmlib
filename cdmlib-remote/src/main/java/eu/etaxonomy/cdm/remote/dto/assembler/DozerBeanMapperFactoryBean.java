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

	  public final void setEventListeners(final List<DozerEventListener> eventListeners) {
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
	  public final Class<Mapper> getObjectType() {
	    return Mapper.class;
	  }
	  public final boolean isSingleton() {
	    return true;
	  }

	  // ==================================================================================================================================
	  // interface 'InitializingBean'
	  // ==================================================================================================================================
	  public final void afterPropertiesSet() throws Exception {
	    this.beanMapper = new DozerBeanMapper();

	    if (this.mappingFiles != null) {
	      final List<String> mappings = new ArrayList<String>(this.mappingFiles.length);
	      for (Resource mappingFile : this.mappingFiles) {
	        mappings.add(mappingFile.getURL().toString());
	      }
	      this.beanMapper.setMappingFiles(mappings);
	    }
	    if (this.customConverters != null) {
	      this.beanMapper.setCustomConverters(this.customConverters);
	    }
	    if (this.eventListeners != null) {
	      this.beanMapper.setEventListeners(this.eventListeners);
	    }
	    if (this.factories != null) {
	      this.beanMapper.setFactories(this.factories);
	    }
	    
	    if(this.customFieldMapper != null) {
	    	this.beanMapper.setCustomFieldMapper(customFieldMapper);
	    }
	    
	    if(this.customConvertersWithId != null) {
	    	this.beanMapper.setCustomConvertersWithId(customConvertersWithId);
	    }
	  }

	}
