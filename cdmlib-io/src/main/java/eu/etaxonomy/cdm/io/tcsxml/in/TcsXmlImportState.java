/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml.in;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;

/**
 * @author a.mueller
 * @since 11.05.2009
 * @version 1.0
 */
public class TcsXmlImportState extends ImportStateBase<TcsXmlImportConfigurator, TcsXmlImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TcsXmlImportState.class);

	//TODO make it better
	private Map<String, CommonTaxonName> commonNameMap = null;
	
	private List<String> missingConceptLSIDs = new ArrayList<String>();
	
	

	public List<String> getMissingConceptLSIDs() {
		return missingConceptLSIDs;
	}

	public void setmissingConceptLSIDs(List<String> missingConceptLSIDs) {
		this.missingConceptLSIDs = missingConceptLSIDs;
	}

	public TcsXmlImportState(TcsXmlImportConfigurator config) {
		super(config);
	}
	
	public Map<String, CommonTaxonName> getCommonNameMap() {
		return commonNameMap;
	}

	public void setCommonNameMap(Map<String, CommonTaxonName> commonNameMap) {
		this.commonNameMap = commonNameMap;
	}


	
	
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.IoStateBase#initialize(eu.etaxonomy.cdm.io.common.IoConfiguratorBase)
//	 */
//	@Override
//	public void initialize(TcsXmlImportConfigurator config) {
//				
//	}

}
