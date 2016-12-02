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
import eu.etaxonomy.cdm.persistence.query.NameSearchOrder;

/**
 * @author a.babadshanjan
 * @created 20.01.2009
 * @version 1.0
 */
public class FindTaxaAndNamesConfiguratorImpl<T extends TaxonBase<?>> extends IdentifiableServiceConfiguratorImpl<T>
			implements IFindTaxaAndNamesConfigurator<T> {

    private static final long serialVersionUID = -8510776848175860267L;


    public static FindTaxaAndNamesConfiguratorImpl<?> NewInstance() {
		return new FindTaxaAndNamesConfiguratorImpl<>();
	}


	private boolean doTaxa = true;
	private boolean doSynonyms = false;
	private boolean doTaxaByCommonNames = false;
	private boolean doNamesWithoutTaxa = false;
	private boolean doMisappliedNames = false;
	private boolean doIncludeAuthors = false;
	private Classification classification = null;
	private List<String> taxonPropertyPath;
	private List<String> synonymPropertyPath;
	private List<String> taxonNamePropertyPath;
	private List<String> commonNamePropertyPath;
	private Set<NamedArea> namedAreas;
	private NameSearchOrder order;

    /**
	 * @return the taxonNamePropertyPath
	 */
	@Override
    public List<String> getTaxonNamePropertyPath() {
		return taxonNamePropertyPath;
	}

	/**
	 * @param taxonNamePropertyPath the taxonNamePropertyPath to set
	 */
	@Override
    public void setTaxonNamePropertyPath(List<String> taxonNamePropertyPath) {
		this.taxonNamePropertyPath = taxonNamePropertyPath;
	}

	@Override
    public boolean isDoTaxa() {
		return doTaxa;
	}

	@Override
    public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}

	@Override
    public boolean isDoSynonyms() {
		return doSynonyms;
	}

	@Override
    public void setDoSynonyms(boolean doSynonyms) {
        this.doSynonyms = doSynonyms;
	}

	/**
	 * @return doTaxaByCommonNames
	 */
	@Override
    public boolean isDoTaxaByCommonNames() {
		return doTaxaByCommonNames;
	}

	/**
	 * @param doTaxaByCommonNames
	 */
	@Override
    public void setDoTaxaByCommonNames(boolean doTaxaByCommonNames) {
		this.doTaxaByCommonNames = doTaxaByCommonNames;
	}

	/**
	 * @return doNamesWithoutTaxa
	 */
	@Override
    public boolean isDoNamesWithoutTaxa() {
		return doNamesWithoutTaxa;
	}

	/**
	 * @param doNamesWithoutTaxa
	 */
	@Override
    public void setDoNamesWithoutTaxa(boolean doNamesWithoutTaxa) {
		this.doNamesWithoutTaxa = doNamesWithoutTaxa;
	}

	@Override
    public MatchMode getMatchMode() {
		return matchMode;
	}

	@Override
    public void setMatchMode(MatchMode matchMode) {
		this.matchMode = matchMode;
	}

	@Override
    public Classification getClassification() {
		return classification;
	}

	@Override
    public void setClassification(Classification classification) {
		this.classification = classification;
	}

	@Override
    public List<String> getTaxonPropertyPath() {
		return taxonPropertyPath;
	}

	@Override
    public void setTaxonPropertyPath(List<String> taxonPropertyPath) {
		this.taxonPropertyPath = taxonPropertyPath;
	}

	@Override
    public List<String> getCommonNamePropertyPath() {
		return commonNamePropertyPath;
	}

	@Override
    public void setCommonNamePropertyPath(List<String> commonNamePropertyPath) {
		this.commonNamePropertyPath = commonNamePropertyPath;
	}

	@Override
    public Set<NamedArea> getNamedAreas() {
		return namedAreas;
	}

	@Override
    public void setNamedAreas(Set<NamedArea> namedAreas) {
		this.namedAreas = namedAreas;
	}

	@Override
    public List<String> getSynonymPropertyPath() {
		return synonymPropertyPath;
	}

	@Override
	public void setSynonymPropertyPath(List<String> synonymPropertyPath){
		this.synonymPropertyPath = synonymPropertyPath;
	}

	@Override
	public boolean isDoMisappliedNames() {
		return this.doMisappliedNames;
	}

	@Override
	public void setDoMisappliedNames(boolean doMisappliedNames) {
				this.doMisappliedNames = doMisappliedNames;
	}

	@Override
    public boolean isDoIncludeAuthors() {
        return doIncludeAuthors;
    }

	@Override
    public void setDoIncludeAuthors(boolean doIncludeAuthors) {
        this.doIncludeAuthors = doIncludeAuthors;
    }


    @Override
    public NameSearchOrder getOrder() {
        return order;
    }
    @Override
    public void setOrder(NameSearchOrder order) {
        this.order = order;
    }

}
