/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class represent page based subsets of the total set of results returned by a search query.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 15.02.2008 15:26:06
 *
 */
public class ResultSetPageSTO<T extends BaseSTO> {

	/**
	 * total number of results returned
	 */
	private long totalResultsCount;
	/**
	 * Then number of items per page
	 */
	int perPageCount;
	/**
	 * The number of this page. First page has index 1.
	 */
	int pageNumber;
	/**
	 * Total number of pages
	 */
	int totalPageCount;
	/**
	 * A list containing the items for this result page.
	 * The number of items will not exceed the {@link #perPageCount} value.
	 * The last page may contain less items. 
	 */
	private List<T> results = new ArrayList<T>();
	
	
	public long getTotalResultsCount() {
		return totalResultsCount;
	}
	public void setTotalResultsCount(long totalResultsCount) {
		this.totalResultsCount = totalResultsCount;
	}
	public int getPerPageCount() {
		return perPageCount;
	}
	public void setPerPageCount(int perPageCount) {
		this.perPageCount = perPageCount;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public int getTotalPageCount() {
		return totalPageCount;
	}
	public void setTotalPageCount(int totalPageCount) {
		this.totalPageCount = totalPageCount;
	}
	public List<T> getResults() {
		return results;
	}
}
