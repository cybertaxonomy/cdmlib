// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class FaunaEuropaeaImportState extends ImportStateBase<FaunaEuropaeaImportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaImportState.class);

	
	public FaunaEuropaeaImportState(FaunaEuropaeaImportConfigurator config) {
		super(config);
	}
	
	private Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap();
	/* Highest taxon index in the FauEu database */
	private int highestTaxonIndex = 0;


	/**
	 * @return the highestTaxonIndex
	 */
	public int getHighestTaxonIndex() {
		return highestTaxonIndex;
	}

	/**
	 * @param highestTaxonIndex the highestTaxonIndex to set
	 */
	public void setHighestTaxonIndex(int highestTaxonIndex) {
		this.highestTaxonIndex = highestTaxonIndex;
	}

	/**
	 * @return the fauEuTaxonMap
	 */
	public Map<Integer, FaunaEuropaeaTaxon> getFauEuTaxonMap() {
		return fauEuTaxonMap;
	}

	/**
	 * @param fauEuTaxonMap the fauEuTaxonMap to set
	 */
	public void setFauEuTaxonMap(Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {
		this.fauEuTaxonMap = fauEuTaxonMap;
	}
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.IoStateBase#initialize(eu.etaxonomy.cdm.io.common.IoConfiguratorBase)
//	 */
//	@Override
//	public void initialize(ExcelImportConfiguratorBase config) {
//				
//	}

}
