// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.cyprus;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class CyprusImportState extends ExcelImportState<CyprusImportConfigurator, ExcelRowBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CyprusImportState.class);

	private Map<String, Taxon> higherTaxonTaxonMap = new HashMap<String, Taxon>();
	
	private CyprusRow cyprusRow;
	private CyprusDistributionRow cyprusDistributionRow;


	public CyprusImportState(CyprusImportConfigurator config) {
		super(config);
	}

	
	public boolean containsHigherTaxon(String higherName) {
		return higherTaxonTaxonMap.containsKey(higherName);
	}

	public Taxon putHigherTaxon(String higherName, Taxon taxon) {
		return higherTaxonTaxonMap.put(higherName, taxon);
	}

	public Taxon removeHigherTaxon(String higherName) {
		return higherTaxonTaxonMap.remove(higherName);
	}

	public Taxon getHigherTaxon(String higherName) {
		return higherTaxonTaxonMap.get(higherName);
	}


	

//	public boolean containsHigherTaxonUuid(String higherName) {
//		return higherTaxonUuidMap.containsKey(higherName);
//	}
//
//	public UUID putHigherTaxon(String higherName, UUID uuid) {
//		return higherTaxonUuidMap.put(higherName, uuid);
//	}
//
//	public UUID removeHigherTaxon(String higherName) {
//		return higherTaxonUuidMap.remove(higherName);
//	}
//
//	public UUID getHigherTaxon(String higherName) {
//		return higherTaxonUuidMap.get(higherName);
//	}

	
	
	/**
	 * @return the cyprusRow
	 */
	public CyprusRow getCyprusRow() {
		return cyprusRow;
	}

	/**
	 * @param cyprusRow the normalExplicitRow to set
	 */
	public void setCyprusRow(CyprusRow cyprusRow) {
		this.cyprusRow = cyprusRow;
	}

	
	/**
	 * @return the cyprusRow
	 */
	public CyprusDistributionRow getCyprusDistributionRow() {
		return cyprusDistributionRow;
	}

	/**
	 * @param cyprusRow the normalExplicitRow to set
	 */
	public void setCyprusDistributionRow(CyprusDistributionRow cyprusRow) {
		this.cyprusDistributionRow = cyprusRow;
	}

	

    
}
