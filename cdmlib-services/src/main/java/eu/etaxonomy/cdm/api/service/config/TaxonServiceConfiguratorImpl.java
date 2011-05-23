// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */


package eu.etaxonomy.cdm.api.service.config;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.babadshanjan
 * @created 20.01.2009
 * @version 1.0
 */
public class TaxonServiceConfiguratorImpl<T extends TaxonBase<?>> extends IdentifiableServiceConfiguratorImpl<T>
			implements ITaxonServiceConfigurator<T> {


	public static TaxonServiceConfiguratorImpl NewInstance() {
		return new TaxonServiceConfiguratorImpl();
	}

	
	private boolean doTaxa = true;
	private boolean doSynonyms = false;
	private boolean doTaxaByCommonNames = false;
	private boolean doNamesWithoutTaxa = false;
	private Classification classification = null;
	private List<String> taxonPropertyPath;
	private List<String> synonymPropertyPath;
	private List<String> taxonNamePropertyPath;
	private List<String> commonNamePropertyPath;
	private Set<NamedArea> namedAreas;
	
	/**
	 * @return the taxonNamePropertyPath
	 */
	public List<String> getTaxonNamePropertyPath() {
		return taxonNamePropertyPath;
	}

	/**
	 * @param taxonNamePropertyPath the taxonNamePropertyPath to set
	 */
	public void setTaxonNamePropertyPath(List<String> taxonNamePropertyPath) {
		this.taxonNamePropertyPath = taxonNamePropertyPath;
	}
	
	public boolean isDoTaxa() {
		return doTaxa;
	}

	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}

	public boolean isDoSynonyms() {
		return doSynonyms;
	}

	public void setDoSynonyms(boolean doSynonyms) {
        this.doSynonyms = doSynonyms;
	}

	/**
	 * @return doTaxaByCommonNames
	 */
	public boolean isDoTaxaByCommonNames() {
		return doTaxaByCommonNames;
	}

	/**
	 * @param doTaxaByCommonNames
	 */
	public void setDoTaxaByCommonNames(boolean doTaxaByCommonNames) {
		this.doTaxaByCommonNames = doTaxaByCommonNames;
	}
	
	/**
	 * @return doNamesWithoutTaxa
	 */
	public boolean isDoNamesWithoutTaxa() {
		return doNamesWithoutTaxa;
	}

	/**
	 * @param doNamesWithoutTaxa
	 */
	public void setDoNamesWithoutTaxa(boolean doNamesWithoutTaxa) {
		this.doNamesWithoutTaxa = doNamesWithoutTaxa;
	}

	public MatchMode getMatchMode() {
		return matchMode;
	}

	public void setMatchMode(MatchMode matchMode) {
		this.matchMode = matchMode;
	}
	
	public Classification getClassification() {
		return classification;
	}
	
	public void setClassification(Classification classification) {
		this.classification = classification;
	}

	public List<String> getTaxonPropertyPath() {
		return taxonPropertyPath;
	}

	public void setTaxonPropertyPath(List<String> taxonPropertyPath) {
		this.taxonPropertyPath = taxonPropertyPath;
	}

	public List<String> getCommonNamePropertyPath() {
		return commonNamePropertyPath;
	}

	public void setCommonNamePropertyPath(List<String> commonNamePropertyPath) {
		this.commonNamePropertyPath = commonNamePropertyPath;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator#getAreas()
	 */
	public Set<NamedArea> getNamedAreas() {
		return namedAreas;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator#setAreas(java.util.List)
	 */
	public void setNamedAreas(Set<NamedArea> namedAreas) {
		this.namedAreas = namedAreas;
	}

	public List<String> getSynonymPropertyPath() {
		return synonymPropertyPath;
	}
	
	@Override
	public void setSynonymPropertyPath(List<String> synonymPropertyPath){
		this.synonymPropertyPath = synonymPropertyPath;
	}
	
}
