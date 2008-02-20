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
 * Instances of this class are subsets of the total set of results returned by a search query.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 15.02.2008 15:26:06
 *
 */
public class ResultSetPageSTO {

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
	private List<BaseSTO> results = new ArrayList<BaseSTO>();
}
