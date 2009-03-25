// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

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
