package eu.etaxonomy.cdm.api.service.pager;

import java.util.List;

public interface Pager<T> {
    public Integer getPagesAvailable();
	
	public Integer getNextIndex();
	
	public Integer getPrevIndex();
	
	public Integer getCurrentIndex();
	
	public String getPageNumber(int index);

	public List<Integer> getIndices();
	
	public Integer getCount();

	public Integer getFirstRecord();

	public Integer getLastRecord();
	
	public List<T> getRecords();
	
	public String getSuggestion();

}
