package eu.etaxonomy.cdm.api.service.pager.impl;

import java.util.List;

public class DefaultPagerImpl<T> extends AbstractPagerImpl<T> {
	


    public DefaultPagerImpl(Integer currentIndex, Integer count, Integer pageSize,	List<T> records) {
		super(currentIndex, count, pageSize, records);
	}
    
    public DefaultPagerImpl(Integer currentIndex, Integer count, Integer pageSize,	List<T> records, String suggestion) {
		super(currentIndex, count, pageSize, records,suggestion);
	}

	protected String createLabel(String s1, String s2) {
        return s1 + DefaultPagerImpl.LABEL_DIVIDER + s2;
    }
}
