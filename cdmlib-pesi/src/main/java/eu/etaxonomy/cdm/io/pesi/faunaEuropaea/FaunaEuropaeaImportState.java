// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.faunaEuropaea;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class FaunaEuropaeaImportState extends ImportStateBase<FaunaEuropaeaImportConfigurator, FaunaEuropaeaImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaImportState.class);

	
	public FaunaEuropaeaImportState(FaunaEuropaeaImportConfigurator config) {
		super(config);
	}
	
	private Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap();
	private Map<UUID, UUID> childParentMap = new HashMap();
	
	/* Highest taxon index in the FauEu database */
//	private int highestTaxonIndex = 305755;
	/* Max number of taxa to be saved with one service call */
//	private int limit = 20000;


//	/**
//	 * @return the limit
//	 */
//	public int getLimit() {
//		return limit;
//	}
//
//	/**
//	 * @param limit the limit to set
//	 */
//	public void setLimit(int limit) {
//		this.limit = limit;
//	}


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

	/**
	 * @return the childParentMap
	 */
	public Map<UUID, UUID> getChildParentMap() {
		return childParentMap;
	}

	/**
	 * @param childParentMap the childParentMap to set
	 */
	public void setChildParentMap(Map<UUID, UUID> childParentMap) {
		this.childParentMap = childParentMap;
	}
	
}
