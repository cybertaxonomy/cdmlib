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

public class StringLabelPagerImpl<T> extends DefaultPagerImpl<T> {

    private static final long serialVersionUID = -6353299287388177858L;

    public StringLabelPagerImpl(Integer currentIndex, Integer count,Integer pageSize, List<T> records) {
		super(currentIndex, count, pageSize, records);
	}

	public StringLabelPagerImpl(Integer currentIndex, Integer count,Integer pageSize, List<T> records,String suggestion) {
		super(currentIndex, count, pageSize, records, suggestion);
	}

	@Override
	protected String createLabel(String s1, String s2) {
        for(int i = 0; i < Math.min(s1.length(),s2.length()); i++) {
          if(!s1.substring(0,i).equals(s2.substring(0,i))) {
            return super.createLabel(s1.substring(0,i), s2.substring(0,i));
          }
        }

        return super.createLabel(s1, s2);
    }

}
