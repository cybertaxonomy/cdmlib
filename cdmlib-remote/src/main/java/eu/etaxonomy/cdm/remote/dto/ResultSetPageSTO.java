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
public class ResultSetPageSTO<T extends IBaseSTO> {

	/**
	 * Total number of matching records. Maybe distributed across several pages
	 */
	private int totalResultsCount=0;
	/**
	 * Total number of pages
	 */
	private int totalPageCount=0;
	/**
	 * Then number of items per page. Defaults to 25
	 */
	private int pageSize=25;
	/**
	 * The number of this page. First default page has index 1.
	 */
	private int pageNumber=1;
	/**
	 * The number of records on this page
	 */
	private int resultsOnPage=0;
	/**
	 * A list containing the items for this result page.
	 * The number of items will not exceed the {@link #pageSize} value.
	 * The last page may contain less items. 
	 */
	private List<T> results = new ArrayList<T>();

	
	public int getTotalResultsCount() {
		return totalResultsCount;
	}
	public void setTotalResultsCount(int totalResultsCount) {
		this.totalResultsCount = totalResultsCount;
		this.totalPageCount = (int) Math.ceil((double) totalResultsCount / (double) pageSize);
		this.resultsOnPage = Math.max(0, Math.min(pageSize, totalResultsCount - pageSize*(pageNumber-1)));
		this.pageSize=25;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		// recalc other properties via totalResults setter
		setTotalResultsCount(getTotalResultsCount());
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
		// recalc other properties via totalResults setter
		setTotalResultsCount(getTotalResultsCount());
	}
	public int getTotalPageCount() {
		return totalPageCount;
	}
	public List<T> getResults() {
		return results;
	}
	/**
	 * add a new result to the resultset.
	 * automatically increase totalResultCount and extend pagesize if needed
	 * @param result
	 */
	public void addResultToFirstPage(T result){
		pageNumber = 1;
		results.add(result);
		totalResultsCount++;
		if (totalResultsCount > pageSize){
			pageSize++;
		}
		// recalc
		setTotalResultsCount(this.totalResultsCount);
	}
	public int getResultsOnPage() {
		return resultsOnPage;
	}
	private void setTotalPageCount(int totalPageCount) {
		this.totalPageCount = totalPageCount;
	}
	public void setResultsOnPage(int resultsOnPage) {
		this.resultsOnPage = resultsOnPage;
	}
	private void setResults(List<T> results) {
		this.results = results;
	}
}
