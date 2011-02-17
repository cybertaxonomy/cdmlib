// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.taxa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class TaxonExcelImportState extends ExcelImportState<ExcelImportConfiguratorBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonExcelImportState.class);
	
    /** Already processed authors */
	private Set<String> authors = new HashSet<String>();
    /** Taxon "light" containing all string info from columns */
	private NormalExplicitRow normalExplicitRow = null;

	private Map<Integer, TaxonBase> taxonMap= new HashMap<Integer, TaxonBase>();
	private Map<String, TeamOrPersonBase> authorMap= new HashMap<String, TeamOrPersonBase>();
	
	
	public TaxonExcelImportState(ExcelImportConfiguratorBase config) {
		super(config);
	}
	
	/**
	 * @return the normalExplicitRow
	 */
	public NormalExplicitRow getTaxonLight() {
		return normalExplicitRow;
	}

	/**
	 * @param normalExplicitRow the normalExplicitRow to set
	 */
	public void setTaxonLight(NormalExplicitRow normalExplicitRow) {
		this.normalExplicitRow = normalExplicitRow;
	}

	/**
	 * @return the author
	 */
	public Set<String> getAuthors() {
		return authors;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthors(Set<String> authors) {
		this.authors = authors;
	}


	/**
	 * @param parentId
	 * @return
	 */
	public TaxonBase getTaxonBase(Integer taxonId) {
		return taxonMap.get(taxonId);
	}


	/**
	 * @param parentId
	 * @param taxon
	 */
	public void putTaxon(Integer taxonId, TaxonBase taxonBase) {
		taxonMap.put(taxonId, taxonBase);
	}

	

}
