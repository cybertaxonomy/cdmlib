/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
*/

package eu.etaxonomy.cdm.api.service.config.impl;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.babadshanjan
 * @created 20.01.2009
 * @version 1.0
 */
public class TaxonServiceConfiguratorImpl implements ITaxonServiceConfigurator {
	
	private boolean doTaxa = true;
	private boolean doSynonyms = false;
	private boolean doNamesWithoutTaxa = false;
	private String searchString;
	private ReferenceBase sec = null;
	private Integer pageSize = null;
	private Integer pageNumber = null;
	
	public static TaxonServiceConfiguratorImpl NewInstance() {
		return new TaxonServiceConfiguratorImpl();
	}
	
	public boolean isDoSynonyms() {
		return doSynonyms;
	}

	public void setDoSynonyms(boolean doSynonyms) {
        this.doSynonyms = doSynonyms;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}

	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}

	public String getSearchString() {
		return searchString;
	}
	
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public ReferenceBase getSec() {
		return sec;
	}
	
	public void setReferenceBase(ReferenceBase sec) {
		this.sec = sec;
	}

	/**
	 * @return the doNamesWithoutTaxa
	 */
	public boolean isDoNamesWithoutTaxa() {
		return doNamesWithoutTaxa;
	}

	/**
	 * @param doNamesWithoutTaxa the doEmptyNames to set
	 */
	public void setDoNamesWithoutTaxa(boolean doNamesWithoutTaxa) {
		this.doNamesWithoutTaxa = doNamesWithoutTaxa;
	}

	/**
	 * @return the pageSize
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the pageNumber
	 */
	public Integer getPageNumber() {
		return pageNumber;
	}

	/**
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
}
