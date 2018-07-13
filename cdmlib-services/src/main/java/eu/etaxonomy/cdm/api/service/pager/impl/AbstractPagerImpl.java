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
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;

public abstract class AbstractPagerImpl<T> implements Pager<T>, Serializable {

    private static final long serialVersionUID = -1869488482336439083L;

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

        if(records == null) {
            records = new ArrayList<T>(0);
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

	/**
	 * Test if the the <code>numberOfResults</code> in the range of the page specified by <code>pageIndex</code> and <code>pageSize</code>.
	 * <p>
	 * When using this method in a service layer class you will also need to provide the according <code>limit</code> and <code>start</code>
	 * parameters for dao list methods. The PagerUtil class provides the according methods for the required calculation:
	 * {@link PagerUtils#limitFor(Integer)} and {@link PagerUtils#startFor(Integer, Integer)}.<br/>
     * <b>NOTE:</b> To calculate <code>limit</code> and <code>start</code> for dao methods is highly recommended to use
     * the {@link #limitStartforRange(Long, Integer, Integer)} method instead which already includes the calculation of
     * <code>limit</code> and <code>start</code>.
	 *
	 * @param numberOfResults
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public static boolean hasResultsInRange(Long numberOfResults, Integer pageIndex, Integer pageSize) {
		return numberOfResults > 0 // no results at all
				&& (pageSize == null // page size may be null : return all in this case
				|| pageIndex != null && numberOfResults > (pageIndex * pageSize));
	}

	/**
     * Test if the the <code>numberOfResults</code> in the range of the page specified by <code>pageIndex</code> and <code>pageSize</code>.
     * <p>
     * When using this method in a service layer class you will also need to provide the according <code>limit</code> and <code>start</code>
     * parameters for dao list methods. The PagerUtil class provides the according methods for the required calculation:
     * {@link PagerUtils#limitFor(Integer)} and {@link PagerUtils#startFor(Integer, Integer)}.<br/>
     * <b>NOTE:</b> To calculate <code>limit</code> and <code>start</code> for dao methods is highly recommended to use
     * the {@link #limitStartforRange(Long, Integer, Integer)} method instead which already includes the calculation of
     * <code>limit</code> and <code>start</code>.
     *
     * @param numberOfResults
     * @param pageIndex
     * @param pageSize
     * @return
     *
     * @deprecated The Hibernate <code>count</code> function returns <code>Long</code> values!
     *  Use {@link #hasResultsInRange(Long, Integer, Integer)} instead.
     */
    @Deprecated
    public static boolean hasResultsInRange(Integer numberOfResults, Integer pageIndex, Integer pageSize) {
        return numberOfResults > 0 // no results at all
                && (pageSize == null // page size may be null : return all in this case
                || pageIndex != null && numberOfResults > (pageIndex * pageSize));
    }

	/**
     * Test if the the <code>numberOfResults</code> in the range of the page specified by <code>pageIndex</code> and <code>pageSize</code>.
     * And returns the according <code>limit</code> and <code>start</code> values as an array in case the test is successful. If there is no
     * result in the specified range the return value will be <code>null</code>.
     * <p>
     * When using this method in a service layer class you will also need to provide the according <code>limit</code> and <code>start</code>
     * parameters for dao list methods. The PagerUtil class provides the according methods for the required calculation:
     * {@link PagerUtils#limitFor(Integer)} and {@link PagerUtils#startFor(Integer, Integer)}
     *
     * @param numberOfResults
     * @param pageIndex
     *  will be set to <code>0</code> if <code>null</code>
     * @param pageSize
     * @return An <code>Integer</code> array containing limit and start: <code>new int[]{limit, start}</code> or null if there is no result in the range of
     *  <code>pageIndex</code> and <code>pageSize</code>.
     *
     */
    public static Integer[] limitStartforRange(Long numberOfResults, Integer pageIndex, Integer pageSize) {
        if(pageIndex == null){
            pageIndex = 0;
        }
        if(hasResultsInRange(numberOfResults, pageIndex, pageSize)){
            return  new Integer[]{PagerUtils.limitFor(pageSize), PagerUtils.startFor(pageSize, pageIndex)};
        }
        return null;
    }

    /**
     * Test if the the <code>numberOfResults</code> in the range of the page specified by <code>pageIndex</code> and <code>pageSize</code>.
     * And returns the according <code>limit</code> and <code>start</code> values as an array in case the test is successful. If there is no
     * result in the specified range the return value will be <code>null</code>.
     * <p>
     * When using this method in a service layer class you will also need to provide the according <code>limit</code> and <code>start</code>
     * parameters for dao list methods. The PagerUtil class provides the according methods for the required calculation:
     * {@link PagerUtils#limitFor(Integer)} and {@link PagerUtils#startFor(Integer, Integer)}
     *
     * @param numberOfResults
     * @param pageIndex
     * @param pageSize
     * @return An <code>Integer</code> array containing limit and start: <code>new int[]{limit, start}</code> or null if there is no result in the range of
     *  <code>pageIndex</code> and <code>pageSize</code>.
     *
     * @deprecated The Hibernate <code>count</code> function returns <code>Long</code> values! Use {@link #limitStartforRange(Long, Integer, Integer)} instead.
     */
    @Deprecated
    public static Integer[] limitStartforRange(Integer numberOfResults, Integer pageIndex, Integer pageSize) {
        if(hasResultsInRange(new Long(numberOfResults), pageIndex, pageSize)){
            return  new Integer[]{PagerUtils.limitFor(pageSize), PagerUtils.startFor(pageSize, pageIndex)};
        }
        return null;
    }

}
