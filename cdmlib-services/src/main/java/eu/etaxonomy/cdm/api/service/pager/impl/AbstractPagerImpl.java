// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.pager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.etaxonomy.cdm.api.service.pager.Pager;

public abstract class AbstractPagerImpl<T> implements Pager<T>, Serializable {

	protected static Integer MAX_PAGE_LABELS = 3;
	protected static String LABEL_DIVIDER = " - ";
	private static Log log = LogFactory.getLog(DefaultPagerImpl.class);
	protected Integer pagesAvailable;
	protected Integer prevIndex;
	protected Integer nextIndex;
	protected Integer currentIndex;
	protected Map<Integer,String> pageNumbers;
	protected Integer firstRecord;
	protected Integer lastRecord;
	protected Long count;
	protected List<T> records;
	protected String suggestion;
	protected ArrayList<Integer> indices;
	protected Integer pageSize;

	/**
	 * Constructor
	 *
	 * @param currentIndex the page of this result set (0-based), can be null
	 * @param count the total number of results available for this query
	 * @param pageSize The size of pages (can be null if all results should be returned if available)
	 * @param records A list of objects in this page (can be empty if there were no results)
	 * @param suggestion a suggested query that would improve the search (only applicable for free-text / lucene queries)
	 *
	 * @deprecated This constructor only supports total result counts to {@link Integer#MAX_VALUE} u
	 * use {@link AbstractPagerImpl(Integer currentIndex, Long count, Integer pageSize, List<T> records, String suggestion)}
	 * instead
	 */
	@Deprecated
    public AbstractPagerImpl(Integer currentIndex, Integer count, Integer pageSize, List<T> records, String suggestion) {
		this(currentIndex,count.longValue(), pageSize,records);
		this.suggestion = suggestion;
	}

	   /**
     * Constructor
     *
     * @param currentIndex the page of this result set (0-based), can be null
     * @param count the total number of results available for this query
     * @param pageSize The size of pages (can be null if all results should be returned if available)
     * @param records A list of objects in this page (can be empty if there were no results)
     * @param suggestion a suggested query that would improve the search (only applicable for free-text / lucene queries)
     */
    public AbstractPagerImpl(Integer currentIndex, Long count, Integer pageSize, List<T> records, String suggestion) {
        this(currentIndex,count,pageSize,records);
        this.suggestion = suggestion;
    }

    /**
     * Constructor
     *
     * @param currentIndex the page of this result set (0-based), can be null
     * @param count the total number of results available for this query
     * @param pageSize The size of pages (can be null or 0 if all results should be returned if available)
     * @param records A list of objects in this page (can be empty if there were no results)
     *
     * @deprecated This constructor only supports total result counts to {@link Integer#MAX_VALUE} u
     * use {@link AbstractPagerImpl(Integer currentIndex, Long count, Integer pageSize, List<T> records, String suggestion)}
     * instead
     */
    @Deprecated
    public AbstractPagerImpl(Integer currentIndex, Integer count, Integer pageSize, List<T> records) {
        this(currentIndex, count.longValue(), pageSize, records);
    }

	/**
	 * Constructor
	 *
	 * @param currentIndex the page of this result set (0-based), can be null
	 * @param count the total number of results available for this query
	 * @param pageSize The size of pages (can be null or 0 if all results should be returned if available)
	 * @param records A list of objects in this page (can be empty if there were no results)
	 */
	public AbstractPagerImpl(Integer currentIndex, Long count, Integer pageSize, List<T> records) {
        if(currentIndex != null) {
		    this.currentIndex = currentIndex;
        } else {
        	this.currentIndex = 0;
        }

        this.pageSize = pageSize;
		this.pageNumbers = new HashMap<Integer,String>();
		indices = new ArrayList<Integer>();
		if(count == 0) {
			pagesAvailable = 1;
		} else if(pageSize != null && pageSize != 0) {
			 if( 0 == count % pageSize) {
				pagesAvailable = (int)( count / pageSize );

                Integer labelsStart = 0;
                if(this.currentIndex > DefaultPagerImpl.MAX_PAGE_LABELS) {
                    labelsStart = this.currentIndex - DefaultPagerImpl.MAX_PAGE_LABELS ;
                }

                Integer labelsEnd = pagesAvailable.intValue();
				if((pagesAvailable - this.currentIndex) > DefaultPagerImpl.MAX_PAGE_LABELS) {
					labelsEnd = this.currentIndex + DefaultPagerImpl.MAX_PAGE_LABELS;
				}

				for(int index = labelsStart; index < labelsEnd; index++) {
					indices.add(index);
					String startLabel = getLabel(index * pageSize);
					String endLabel = getLabel(((index + 1) * pageSize) - 1);
					pageNumbers.put(index,createLabel(startLabel,endLabel));
				}
			} else {
				pagesAvailable = (int)(count / pageSize) + 1; //12

				Integer labelsStart = 0;
                if(this.currentIndex > DefaultPagerImpl.MAX_PAGE_LABELS) {
                    labelsStart = this.currentIndex - DefaultPagerImpl.MAX_PAGE_LABELS;
                }

                Integer labelsEnd = pagesAvailable.intValue();
				if((pagesAvailable - this.currentIndex) > DefaultPagerImpl.MAX_PAGE_LABELS ) {
					labelsEnd = this.currentIndex + DefaultPagerImpl.MAX_PAGE_LABELS;
					for(int index = labelsStart; index < labelsEnd; index++) {
						indices.add(index);

						String startLabel = getLabel(index * pageSize);
						String endLabel = getLabel(((index + 1) * pageSize) - 1);
						pageNumbers.put(index,createLabel(startLabel,endLabel));
					}


				} else {
					for(int index = labelsStart; index < (labelsEnd -1); index++) {
						indices.add(index);
						String startLabel = getLabel(index * pageSize);
						String endLabel = getLabel(((index + 1) * pageSize) - 1);
						pageNumbers.put(index,createLabel(startLabel,endLabel));
					}
					indices.add(pagesAvailable.intValue() - 1);
					String startLabel = getLabel((pagesAvailable.intValue() - 1) * pageSize);
					String endLabel = getLabel(count.intValue() - 1);
					pageNumbers.put(pagesAvailable.intValue() - 1,createLabel(startLabel,endLabel));
				}


			}
		} else {
			pagesAvailable = 1;
		}

		if(pagesAvailable == 1) {
			nextIndex = null;
			prevIndex = null;
		} else {
			if(0 < this.currentIndex) {
				prevIndex = this.currentIndex - 1;
			}
			if(this.currentIndex < (pagesAvailable - 1)) {
				nextIndex = this.currentIndex + 1;
			}
		}
		if(pageSize == null) {
			this.firstRecord = 1;
	    	this.lastRecord = records.size();
		} else {
    		this.firstRecord = (this.currentIndex * pageSize) + 1;
	    	this.lastRecord = (this.currentIndex * pageSize) + records.size();
		}

		this.count = count;
		this.records = records;
	}

	protected abstract String createLabel(String startLabel, String endLabel);

	protected String getLabel(Integer i) {
		Integer label = new Integer(i + 1);
		return label.toString();
	}

	@Override
	public Integer getPagesAvailable() {
		return pagesAvailable;
	}

	@Override
	public Integer getNextIndex() {
		return nextIndex;
	}

	@Override
	public Integer getPrevIndex() {
		return prevIndex;
	}

	@Override
	public Integer getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public String getPageNumber(int index) {
		return pageNumbers.get(index);
	}

	@Override
	public List<Integer> getIndices() {
		return indices;
	}

	@Override
	public Long getCount() {
		return count;
	}

	@Override
	public Integer getFirstRecord() {
		return firstRecord;
	}

	@Override
	public Integer getLastRecord() {
		return lastRecord;
	}

	@Override
	public List<T> getRecords() {
		return records;
	}

	@Override
	public String getSuggestion() {
		return suggestion;
	}

	@Override
	public Integer getPageSize() {
		return pageSize;
	}

	public static boolean hasResultsInRange(Long numberOfResults, Integer pageIndex, Integer pageSize) {
		return numberOfResults > 0 // no results at all
				&& (pageSize == null // page size may be null : return all in this case
				|| pageIndex != null && numberOfResults > (pageIndex * pageSize));
	}

}