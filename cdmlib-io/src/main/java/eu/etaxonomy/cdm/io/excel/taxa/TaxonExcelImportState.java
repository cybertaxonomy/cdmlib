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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class TaxonExcelImportState extends ExcelImportState<ExcelImportConfiguratorBase, NormalExplicitRow>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonExcelImportState.class);

    /** Already processed authors */
	private Set<String> authors = new HashSet<String>();

	private Map<String, TaxonBase> taxonMap= new HashMap<String, TaxonBase>();
	private Map<String, TeamOrPersonBase> authorMap= new HashMap<String, TeamOrPersonBase>();
	private Taxon parent;
	private Classification classification;



	public TaxonExcelImportState(ExcelImportConfiguratorBase config) {
		super(config);
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

	public Taxon getParent(){
	    return parent;
	}

	public void setParent(Taxon parent){
	    this.parent = parent;
    }

	/**
	 * @param parentId
	 * @return
	 */
	public TaxonBase getTaxonBase(String taxonId) {
		return taxonMap.get(taxonId);
	}


	/**
	 * @param parentId
	 * @param taxon
	 */
	public void putTaxon(String taxonId, TaxonBase taxonBase) {


	    taxonMap.put(taxonId, taxonBase);
	}

    /**
     * @return the classification
     */
    public Classification getClassification() {
        return classification;
    }

    /**
     * @param classification the classification to set
     */
    public void setClassification(Classification classification) {
        this.classification = classification;
    }



}
