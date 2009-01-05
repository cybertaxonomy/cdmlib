package eu.etaxonomy.cdm.api.service.pager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.etaxonomy.cdm.api.service.pager.Pager;

public abstract class AbstractPagerImpl<T> implements Pager<T> {

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
	protected Integer count;
	protected List<T> records;
	protected String suggestion;
	protected ArrayList<Integer> indices;

	public AbstractPagerImpl(Integer currentIndex, Integer count, Integer pageSize, List<T> records, String suggestion) {
		this(currentIndex,count,pageSize,records);
		this.suggestion = suggestion;
	}
	
	public AbstractPagerImpl(Integer currentIndex, Integer count, Integer pageSize, List<T> records) {
        if(currentIndex != null) {
		    this.currentIndex = currentIndex;
        } else {
        	this.currentIndex = 0;
        }
		
		pageNumbers = new HashMap<Integer,String>();
		indices = new ArrayList<Integer>();
		if(count == 0) {
			pagesAvailable = 1;
		} else if(pageSize != null) {
			 if( 0 == count % pageSize) {
				pagesAvailable = count / pageSize;

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
				pagesAvailable = (count / pageSize) + 1; //12
				
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

	public Integer getPagesAvailable() {
		return pagesAvailable;
	}

	public Integer getNextIndex() {
		return nextIndex;
	}

	public Integer getPrevIndex() {
		return prevIndex;
	}

	public Integer getCurrentIndex() {
		return currentIndex;
	}

	public String getPageNumber(int index) {
		return pageNumbers.get(index);
	}

	public List<Integer> getIndices() {
		return indices;
	}

	public Integer getCount() {
		return count;
	}

	public Integer getFirstRecord() {
		return firstRecord;
	}

	public Integer getLastRecord() {
		return lastRecord;
	}

	public List<T> getRecords() {
		return records;
	}

	public String getSuggestion() {
		return suggestion;
	}

}