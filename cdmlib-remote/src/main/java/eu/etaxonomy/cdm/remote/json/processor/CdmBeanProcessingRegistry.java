/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.json.processor;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.json.CycleSetAcess;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @date 30.03.2009
 * @deprecated class is not beeing used but kept as reference
 */
@Deprecated
public class CdmBeanProcessingRegistry extends CycleSetAcess {
	
	public static final Logger logger = Logger.getLogger(CdmBeanProcessingRegistry.class);

	protected static ThreadLocal<Set<CdmBase>> processedBeans = new ThreadLocal<Set<CdmBase>>(){
		      protected synchronized Set<CdmBase> initialValue() {
		         return new HashSet<CdmBase>();
		      }
		   };

	protected static boolean isBeingProcessed(Object bean) {
		if(logger.isDebugEnabled()){
			logger.debug("looking for: " + bean.getClass() + " with hash: " + bean.hashCode());
		}
		return processedBeans.get().contains(bean);
	}
	
	protected static void register(CdmBase bean){
		if(logger.isDebugEnabled()){
			logger.debug("registering: " + bean.getClass() + " with hash: " + bean.hashCode());
		}
		processedBeans.get().add(bean);
	}
	
	protected static void unregister(CdmBase bean){
		if(logger.isDebugEnabled()){
			logger.debug("un-registering: " + bean.getClass() + " with hash: " + bean.hashCode());
		}
		processedBeans.get().remove(bean);
	}

}
