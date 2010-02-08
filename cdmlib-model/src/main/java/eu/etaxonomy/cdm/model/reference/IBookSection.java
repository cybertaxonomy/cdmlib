/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy;


public interface IBookSection extends ISectionBase, INomenclaturalReference{

	public IBook getInBook();
	
	public void setInBook (IBook book);
	
	void setCacheStrategy(BookSectionDefaultCacheStrategy cacheStratefy);

}
