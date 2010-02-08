// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.config;

import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.babadshanjan
 * @created 03.03.2009
 * @version 1.0
 */
public class IdentifiableServiceConfiguratorBase {
	
	private String titleSearchString = null;
	protected MatchMode matchMode = MatchMode.EXACT;
	private Integer pageSize = null;
	private Integer pageNumber = null;
	
	public static IdentifiableServiceConfiguratorBase NewInstance() {
		return new IdentifiableServiceConfiguratorBase();
	}
	
	public String getTitleSearchString() {
		return titleSearchString;
	}
	
	public void setTitleSearchString(String titleSearchString) {
		this.titleSearchString = titleSearchString;
	}
 
	/**
	 * @return the pageSize
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * Sets the number of results that should be shown on current page
	 * 
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
	 * Sets the number of the page the first result should come from, starting 
	 * with 0 as the first page.
	 * 
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public MatchMode getMatchMode() {
		return matchMode;
	}

	public void setMatchMode(MatchMode matchMode) {
		this.matchMode = matchMode;
	}
}
