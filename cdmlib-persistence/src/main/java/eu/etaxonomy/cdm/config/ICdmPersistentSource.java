/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.config;



/**
 * Interface which represents any CDM Source
 *
 */
public interface ICdmPersistentSource extends ICdmSource {	
	
	/**
	 * Returns the name of bean representing this CDM persistent source
	 * 
	 * @return bean name
	 */
	public String getBeanName();

}
