// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import net.sf.json.CycleSetAcess;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * WARNING! The idea i started implementing here will not work at all!!
 * @author a.kohlbecker
 *
 */
public class TaxonBaseBeanProcessor extends CycleSetAcess implements JsonBeanProcessor {
	
	private static ThreadLocal<Set<CdmBase>> processedBeans = new ThreadLocal<Set<CdmBase>>(){
	      protected synchronized Set<CdmBase> initialValue() {
	         return new HashSet<CdmBase>();
	      }
	   };
	    
	    
	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonBeanProcessor#processBean(java.lang.Object, net.sf.json.JsonConfig)
	 */
	public JSONObject processBean(Object bean, JsonConfig jsonConfig) {
		removeFromCycleSet(bean);
		Taxon taxon = (Taxon)bean;
		
		if(!processedBeans.get().contains(taxon)) {
			processedBeans.get().add(taxon);
			JSONObject json = JSONObject.fromObject(bean, jsonConfig);
			if(Hibernate.isInitialized(taxon.getName())){
				json.element("titleCache", taxon.getTitleCache());			
			}
		}
		processedBeans.get().remove(taxon);
		return null;
	}

}
