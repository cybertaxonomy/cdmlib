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
