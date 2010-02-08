/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml.in;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;

/**
 * This class was created just as a test class to implement the according integration test. 
 * But it can also be used on its own for importing tcs-xml data 
 * 
 * @author a.mueller
 * @created 28.01.2009
 * @version 1.0
 */
@Component
public class CdmTcsXmlImport extends CdmApplicationAwareDefaultImport<TcsXmlImportConfigurator> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmTcsXmlImport.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmImport#invoke(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	public boolean invoke(TcsXmlImportConfigurator tcsiConfig) {
		return super.invoke(tcsiConfig);
	}


	


}
