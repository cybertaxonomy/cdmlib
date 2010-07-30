// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.server;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author a.kohlbecker
 * @date 30.07.2010
 *
 */
public class JvmManager {
	
	/**
	 * 
	 */
	private static final String SUFFIX_PERM_GEN = "Perm Gen";
	
	public static final Logger logger = Logger.getLogger(JvmManager.class);
	
	public static MemoryUsage getPermGenSpaceUsage(){
		return getMemoryPoolUsage(SUFFIX_PERM_GEN);
	}

	protected static MemoryUsage getMemoryPoolUsage(String nameSuffix) {
		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    	
    	for(MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans){
			if(memoryPoolMXBean.getName().endsWith(nameSuffix)){
    			logger.debug(memoryPoolMXBean.getName() 
    					+ ": init= " + memoryPoolMXBean.getUsage().getInit()
    					+ ", used= "+memoryPoolMXBean.getUsage().getUsed()
    					+ ", max= "+memoryPoolMXBean.getUsage().getMax()
    					+ ", committed= "+memoryPoolMXBean.getUsage().getCommitted());
    			return memoryPoolMXBean.getUsage(); 
    		}
    	}
    	return null;
	}
	
	public static MemoryUsage getHeapMemoryUsage(){
			
			MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	    	if(memoryMXBean != null){
	    		logger.info("NonHeapMemoryUsage: "+memoryMXBean.getHeapMemoryUsage());
	    		return memoryMXBean.getHeapMemoryUsage();
	    	}
	    	return null;
		}

}
