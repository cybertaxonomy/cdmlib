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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public class TaxonExcelImportState
            extends ExcelImportState<ExcelImportConfiguratorBase, ExcelRowBase>{

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

    /** Already processed authors */
	private Set<String> authors = new HashSet<>();

	private Map<String, TaxonBase> taxonMap= new HashMap<>();
	/**
     * @return the taxonMap
     */
    public Map<String, TaxonBase> getTaxonMap() {
        return taxonMap;
    }

    /**
     * @param taxonMap the taxonMap to set
     */
    public void setTaxonMap(Map<String, TaxonBase> taxonMap) {
        this.taxonMap = taxonMap;
    }

	private Map<String, TaxonName> nameMap;
	private Map<String, Reference> referenceMap= new HashMap<String, Reference>();
	private Taxon parent;
	private Classification classification;

	public TaxonExcelImportState(ExcelImportConfiguratorBase config) {
		super(config);
	}

	public Set<String> getAuthors() {
		return authors;
	}
	public void setAuthors(Set<String> authors) {
		this.authors = authors;
	}

	public Taxon getParent(){
	    return parent;
	}
	public void setParent(Taxon parent){
	    this.parent = parent;
    }

	public TaxonBase getTaxonBase(String taxonId) {
		return taxonMap.get(taxonId);
	}

	public void putTaxon(String taxonId, TaxonBase taxonBase) {
	    taxonMap.put(taxonId, taxonBase);
	}

    public Classification getClassification() {
        return classification;
    }
    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public Reference getReference(String key) {
        return referenceMap.get(key);
    }

    public void putReference(String key, Reference reference) {
        this.referenceMap.put(key, reference);
    }

    public Map<String, TaxonName> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<String, TaxonName> nameMap) {
        this.nameMap = nameMap;
    }

    public void putName(String titleCache, TaxonName name) {
        if (nameMap == null){
            nameMap = new HashMap<>();
        }
        nameMap.put(titleCache, name);
    }
}
